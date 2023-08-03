package vn.edu.hcmut.nxvhung.bloomclient.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import vn.edu.hcmut.nxvhung.bloomfilter.Filterable;
import vn.edu.hcmut.nxvhung.bloomfilter.hash.Hash;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.Key;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.MergeableCountingBloomFilter;

@Service
public class BlacklistService {

  private Filterable<Key> blacklist;

  private Filterable<Key> mergedBlacklist;

  @PostConstruct
  public void init() {
    blacklist = new MergeableCountingBloomFilter(100, 2, Hash.MURMUR_HASH, 4);
  }

  public boolean existInBlacklist(String phone) {
    Key key = Key.of(phone);
    return blacklist.mayExists(key) && blacklist.mayExists(key);
  }

  public synchronized void updateMergedBlacklist(Filterable<Key> filterable) {
    this.mergedBlacklist = mergedBlacklist;
  }
}
