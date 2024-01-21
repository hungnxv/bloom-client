package vn.edu.hcmut.nxvhung.bloomclient.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
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

  private static final String FILE_PATTERN = "classpath:company%S.csv";
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  private Filterable<Key> blacklist;
  private Filterable<Key> mergedBlacklist;

  private final BloomSender bloomSender;
  private Integer timestamp;
  private Map<String, Integer> timestampVector = new ConcurrentHashMap<>();

  private final RedisService redisService;

  @Value("${bloom.vector-size}")
  private int vectorSize;
  @Value("${company.name}")
  private String companyName;

  @Value("${spring.profiles.active}")
  private String activeProfile;

  private final PhoneBlacklistJpaRepository phoneBlacklistJpaRepository;


  public void loadInitBlacklist() throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(ResourceUtils.getFile(String.format(FILE_PATTERN, activeProfile)).toPath())) {
      loadInitBlacklist(reader);
    }

}

  private void loadInitBlacklist(BufferedReader reader) {
    List<BlacklistDto> blacklistSource = reader.lines().skip(1).map(this::toBlacklistDto).toList();
    for(int i = 0 ; i < blacklistSource.size() - 1; i++){
      String phone = blacklistSource.get(i).getPhone();
      blacklist.add(Key.of(phone));
      phoneBlacklistJpaRepository.save(
          PhoneBlacklist.builder().addedTime(LocalDateTime.now()).deleted(false).phoneNumber(phone).expiredTime(blacklistSource.get(i).getExpiredDate()).build());
      log.info("{}: {} added ", i, phone);
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
    Filterable<Key> savedBlacklist = redisService.getMergedBlacklist();
    if (Objects.nonNull(savedBlacklist)) {
      mergedBlacklist = savedBlacklist;
    }
    Integer savedTimestamp = redisService.getTimestamp();
    if(Objects.nonNull(savedTimestamp)){
      timestamp = savedTimestamp;
    }

    Map<String, Integer>  savedTimestampsMap =  redisService.getTimestampsMap();
    if(Objects.nonNull(savedTimestampsMap)) {
      timestampVector = savedTimestampsMap;
    }

  }
  @Async
  @Transactional
  public void initFromFile() throws IOException {
    log.info("Blacklists are being imported");
    loadInitBlacklist();
    redisService.saveCompanyBlackList(blacklist);
  }

  @Transactional
  public void uploadFile(MultipartFile multipartFile) throws IOException {
    log.info("Blacklists are being imported");
    try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))) {
      loadInitBlacklist(bufferedReader);
    }
    redisService.saveCompanyBlackList(blacklist);
  }

  @Async
  @Transactional
  public void initFromDatabase()  {
    phoneBlacklistJpaRepository.findActiveBlacklists().forEach(phoneBlacklist -> blacklist.add(Key.of(phoneBlacklist.getPhoneNumber())));
    redisService.saveCompanyBlackList(blacklist);
    log.info("Blacklist are imported from database");
  }

  public boolean existInBlacklist(String phone) {
    Key key = Key.of(phone);
    return blacklist.mayExists(key) && blacklist.mayExists(key);
  }

  @Synchronized
  public void handleBFSUpdate(Message message) {
    Integer updatedBsfTimestamp = message.getTimestampVector().getOrDefault("BFS", 0);

    Integer currentBsfTimestamp = timestampVector.getOrDefault("BFS", 0);
    if(updatedBsfTimestamp < currentBsfTimestamp) {
      log.warn("No update, The incoming message is out of dated, Message BSF timestamp{}, current BSF timestamp: {} ", updatedBsfTimestamp, currentBsfTimestamp);
      return;
    }
    this.mergedBlacklist = message.getBlacklist();
    for(Entry<String, Integer> entry : message.getTimestampVector().entrySet()) {
      Integer ourTimestamp = timestampVector.getOrDefault(entry.getKey(), 0);
      if(ourTimestamp < entry.getValue()) {
        timestampVector.put(entry.getKey(), entry.getValue());
      }
    }
    redisService.saveTimestampVector(timestampVector);
    redisService.saveMergedBlacklist(mergedBlacklist);
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
    redisService.saveCompanyBlackList(blacklist);
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
    redisService.saveCompanyBlackList(blacklist);
    log.info("{}: remove phone {} from blacklist.", companyName, phone);

    return true;
  }

  @Scheduled(cron =  "0 0 * * * *")
  public void sendUpdatedBlacklist() {
    Message message = new Message();
    message.setBlacklist(blacklist);
    timestamp++;
    redisService.increaseTimestamp();
    message.setTimestamp(timestamp);
    message.setCompanyName(companyName);
    log.info("{}: send blacklist to bloom-server. Timestamp: {}.", companyName, timestamp);

    bloomSender.sendMessage(message);
  }

  public boolean mayExist(String phone) {
    log.info("Check phone number {} may exist in blacklist", phone);
    Key key = Key.of(phone);
    return blacklist.mayExists(key) || Objects.nonNull(mergedBlacklist) && mergedBlacklist.mayExists(key);
  }

  public void test() {
//    redisService.saveBlackList(blacklist);
//    Filterable<Key> hung = redisService.get();
//    redisService.increaseTimestamp();
//    log.info(String.valueOf(redisService.getTimestamp()));
  }
}
