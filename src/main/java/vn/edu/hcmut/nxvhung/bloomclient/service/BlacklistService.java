package vn.edu.hcmut.nxvhung.bloomclient.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import vn.edu.hcmut.nxvhung.bloomclient.dto.BlacklistDto;
import vn.edu.hcmut.nxvhung.bloomclient.entity.PhoneBlacklist;
import vn.edu.hcmut.nxvhung.bloomclient.repository.PhoneBlacklistJpaRepository;
import vn.edu.hcmut.nxvhung.bloomfilter.Filterable;
import vn.edu.hcmut.nxvhung.bloomfilter.hash.Hash;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.Key;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.MergeableCountingBloomFilter;

@Service
@RequiredArgsConstructor
public class BlacklistService {

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  private Filterable<Key> blacklist;

  private Filterable<Key> mergedBlacklist;

  @Value("${bloom.vector-size}")
  private int vectorSize;
  private final PhoneBlacklistJpaRepository phoneBlacklistJpaRepository;

  public void loadBlacklist() throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(ResourceUtils.getFile("classpath:blacklist1.csv").toPath())) {
      List<BlacklistDto> blacklistSource = reader.lines().skip(1).map(this::toBlacklistDto).toList();
      for(int i = 0 ; i < blacklistSource.size() - 1; i++){
        String phone = blacklistSource.get(i).getPhone();
        blacklist.add(Key.of(phone));
        System.out.println(i + ": " +  phone + " added: ");
      }
    }

}

  private BlacklistDto toBlacklistDto(String line) {
    String[] row = line.split("[;,]");

    return new BlacklistDto(row[0], LocalDate.parse(row[1].replaceAll("\\W", ""), formatter).atStartOfDay());
  }

//  @PostConstruct
  @Async
  public void init() throws IOException {
    blacklist = new MergeableCountingBloomFilter(vectorSize, 10, Hash.MURMUR_HASH, 4);
    loadBlacklist();
  }

  public boolean existInBlacklist(String phone) {
    Key key = Key.of(phone);
    return blacklist.mayExists(key) && blacklist.mayExists(key);
  }

  public synchronized void updateMergedBlacklist(Filterable<Key> filterable) {
    this.mergedBlacklist = mergedBlacklist;
  }

  public Filterable<Key> getBlacklist() {
    return blacklist;
  }

  public PhoneBlacklist add(String phone, LocalDateTime expiredTime) {
    PhoneBlacklist phoneBlacklist = new PhoneBlacklist();
    phoneBlacklist.setPhoneNumber(phone);
    phoneBlacklist.setAddedTime(LocalDateTime.now());
    phoneBlacklist.setRemovedTime(expiredTime);
    mergedBlacklist.add(Key.of(phone));
    return phoneBlacklistJpaRepository.save(phoneBlacklist);
  }

  public boolean remove(String phone) {
    PhoneBlacklist blacklist = phoneBlacklistJpaRepository.findByPhoneNumber(phone);
    if(Objects.isNull(blacklist)) {
      return false;
    }

    blacklist.setRemovedTime(LocalDateTime.now());
    blacklist.setDeleted(true);
    phoneBlacklistJpaRepository.save(blacklist);
    mergedBlacklist.delete(Key.of(phone));
    return true;

  }
}
