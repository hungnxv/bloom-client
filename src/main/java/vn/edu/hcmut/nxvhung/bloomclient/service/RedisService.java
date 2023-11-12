package vn.edu.hcmut.nxvhung.bloomclient.service;

import jakarta.annotation.PostConstruct;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import vn.edu.hcmut.nxvhung.bloomfilter.Filterable;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.Key;

@Service
@RequiredArgsConstructor
public class RedisService {
  private static final String HASH_KEY = "BLACKLIST";

  @Value("${company.name}")
  private String companyName;
  private static final String MERGE_BLACKLIST_HASH_KEY = "%s_MERGED_BLACKLIST";
  private static final String COMPANY_BLACKLIST_HASH_KEY = "%s_COMPANY_BLACKLIST";

  private static final String TIMESTAMP_HASH_KEY = "%s_TIMESTAMP";


  private final RedisTemplate<Object, Object> redisTemplate;

  private HashOperations<Object, String, Filterable<Key>> hashOperations;

  private ValueOperations<Object, Object> timestampOperation;

  @PostConstruct
  public void init() {
    hashOperations = redisTemplate.opsForHash();
    timestampOperation = redisTemplate.opsForValue();

  }

  public void saveCompanyBlackList(Filterable<Key> blacklist) {
    hashOperations.put(HASH_KEY, String.format(COMPANY_BLACKLIST_HASH_KEY, companyName), blacklist);
  }

  public void saveMergedBlacklist(Filterable<Key> blacklist) {
    hashOperations.put(HASH_KEY,String.format(MERGE_BLACKLIST_HASH_KEY, companyName), blacklist);
  }

  public void increaseTimestamp() {
    String key = String.format(TIMESTAMP_HASH_KEY, companyName);
    if(Objects.isNull(timestampOperation.get(key))) {
      timestampOperation.set(key, 0);
    } else {
      timestampOperation.increment(key) ;
    }

  }

  public Integer getTimestamp() {
    return (Integer) timestampOperation.get(TIMESTAMP_HASH_KEY) ;
  }

}
