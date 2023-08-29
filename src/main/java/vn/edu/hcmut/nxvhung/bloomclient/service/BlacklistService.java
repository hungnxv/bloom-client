package vn.edu.hcmut.nxvhung.bloomclient.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import vn.edu.hcmut.nxvhung.bloomclient.dto.BlacklistDto;
import vn.edu.hcmut.nxvhung.bloomclient.entity.PhoneBlacklist;
import vn.edu.hcmut.nxvhung.bloomclient.repository.PhoneBlacklistJpaRepository;
import vn.edu.hcmut.nxvhung.bloomclient.sender.BloomSender;
import vn.edu.hcmut.nxvhung.bloomfilter.Filterable;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;
import vn.edu.hcmut.nxvhung.bloomfilter.hash.Hash;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.Key;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.MergeableCountingBloomFilter;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistService {

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  private Filterable<Key> blacklist;
  private Filterable<Key> mergedBlacklist;

  private final BloomSender bloomSender;
  private Integer timestamp;

  @Value("${bloom.vector-size}")
  private int vectorSize;
  @Value("${company.name}")
  private String companyName;
  private final PhoneBlacklistJpaRepository phoneBlacklistJpaRepository;


  public void loadBlacklist() throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(ResourceUtils.getFile("classpath:companyE.csv").toPath())) {
      List<BlacklistDto> blacklistSource = reader.lines().skip(1).map(this::toBlacklistDto).toList();
      for(int i = 0 ; i < blacklistSource.size() - 1; i++){
        String phone = blacklistSource.get(i).getPhone();
        blacklist.add(Key.of(phone));
        phoneBlacklistJpaRepository.save(
            PhoneBlacklist.builder().addedTime(LocalDateTime.now()).deleted(false).phoneNumber(phone).expiredTime(blacklistSource.get(i).getExpiredDate()).build());
        log.info("{}: {} added: ", i, phone);
      }
    }

}

  private BlacklistDto toBlacklistDto(String line) {
    String[] row = line.split("[;,]");

    return new BlacklistDto(row[0].replaceAll("\\W", ""), LocalDate.parse(row[1], formatter).atStartOfDay());
  }

  @PostConstruct
  public void init() {
    timestamp = 0;
    blacklist = new MergeableCountingBloomFilter(vectorSize, 10, Hash.MURMUR_HASH, 4);
    initFromDatabase();
  }
  @Async
  @Transactional
  public void initFromFile() throws IOException {
    loadBlacklist();
  }

  @Async
  @Transactional
  public void initFromDatabase()  {
    phoneBlacklistJpaRepository.findActiveBlacklists().forEach(phoneBlacklist -> blacklist.add(Key.of(phoneBlacklist.getPhoneNumber())));
    log.info("Blacklist are imported");
  }

  public boolean existInBlacklist(String phone) {
    Key key = Key.of(phone);
    return blacklist.mayExists(key) && blacklist.mayExists(key);
  }

  @Synchronized
  public void updateMergedBlacklist(Filterable<Key> filterable) {
    this.mergedBlacklist = filterable;
  }

  public Filterable<Key> getBlacklist() {
    return blacklist;
  }

  public PhoneBlacklist add(String phone, LocalDateTime expiredTime) {
    PhoneBlacklist phoneBlacklist = new PhoneBlacklist();
    phoneBlacklist.setPhoneNumber(phone);
    phoneBlacklist.setAddedTime(LocalDateTime.now());
    phoneBlacklist.setExpiredTime(expiredTime);
    blacklist.add(Key.of(phone));
    log.info("{}: add phone {} to blacklist.", companyName, phone);
    return phoneBlacklistJpaRepository.save(phoneBlacklist);
  }

  public boolean remove(String phone) {
    List<PhoneBlacklist> phoneBlacklists = phoneBlacklistJpaRepository.findByPhoneNumberAndDeletedIsFalse(phone);
    if(CollectionUtils.isEmpty(phoneBlacklists)) {
      return false;
    }
    for(PhoneBlacklist phoneBlacklist : phoneBlacklists) {
      phoneBlacklist.setExpiredTime(LocalDateTime.now());
      phoneBlacklist.setDeleted(true);
      phoneBlacklistJpaRepository.save(phoneBlacklist);
      blacklist.delete(Key.of(phone));
    }
    log.info("{}: remove phone {} from blacklist.", companyName, phone);

    return true;
  }

  @Scheduled(cron =  "0 0 * * * *")
  public void sendUpdatedBlacklist() {
    Message message = new Message();
    message.setBlacklist(blacklist);
    timestamp++;
    message.setTimestamp(timestamp);
    message.setCompanyName(companyName);
    log.info("{}: send blacklist to bloom-server.", companyName);

    bloomSender.sendMessage(message);
  }

  public boolean mayExist(String phone) {
    Key key = Key.of(phone);
    return blacklist.mayExists(key) || Objects.nonNull(mergedBlacklist) && mergedBlacklist.mayExists(key);
  }
}
