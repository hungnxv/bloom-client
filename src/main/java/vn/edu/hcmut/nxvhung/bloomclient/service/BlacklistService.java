package vn.edu.hcmut.nxvhung.bloomclient.service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import vn.edu.hcmut.nxvhung.bloomfilter.Filterable;
import vn.edu.hcmut.nxvhung.bloomfilter.hash.Hash;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.Key;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.MergeableCountingBloomFilter;

@Service
public class BlacklistService {

  private Filterable<Key> blacklist;

  private Filterable<Key> mergedBlacklist;

  public void loadBlacklist() throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(ResourceUtils.getFile("classpath:blacklist.csv").toPath())) {
      List<String> blacklistSource = reader.lines().skip(1).map(line -> line.replaceAll("\\W", ""))).toList();
      blacklistSource.forEach(phone -> blacklist.add(Key.of(phone)));
    }

}

  @PostConstruct
  public void init() throws IOException {
    blacklist = new MergeableCountingBloomFilter(479253, 10, Hash.MURMUR_HASH, 4);
    loadBlacklist();
  }

  public boolean existInBlacklist(String phone) {
    Key key = Key.of(phone);
    return blacklist.mayExists(key) && blacklist.mayExists(key);
  }

  public synchronized void updateMergedBlacklist(Filterable<Key> filterable) {
    this.mergedBlacklist = mergedBlacklist;
  }
}
