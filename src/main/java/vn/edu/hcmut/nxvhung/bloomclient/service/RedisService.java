package vn.edu.hcmut.nxvhung.bloomclient.service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
  private static final String TIMESTAMP_VECTOR = "%s_TIMESTAMP_VECTOR";
  private final StringRedisTemplate springRedisTemplate;

  private final RedisTemplate<Object, Object> redisTemplate;

  private HashOperations<Object, String, Filterable<Key>> hashOperations;
  private HashOperations<Object, String, Map> timestampVectorOperations;


  private ValueOperations<String, String> timestampOperation;


  @PostConstruct
  public void init() {
    hashOperations = redisTemplate.opsForHash();
    timestampVectorOperations = redisTemplate.opsForHash();
    timestampOperation = springRedisTemplate.opsForValue();

  }

  public void saveCompanyBlackList(Filterable<Key> blacklist) {
    hashOperations.put(HASH_KEY, String.format(COMPANY_BLACKLIST_HASH_KEY, companyName), blacklist);
  }

  public void saveMergedBlacklist(Filterable<Key> blacklist) {
    hashOperations.put(HASH_KEY,String.format(MERGE_BLACKLIST_HASH_KEY, companyName), blacklist);
  }

  public void saveTimestampVector(Map<String, Integer> timestampsMap) {
    timestampVectorOperations.put(HASH_KEY, String.format(TIMESTAMP_VECTOR, companyName), timestampsMap);
  }

  public void increaseTimestamp() {
    String key = String.format(TIMESTAMP_HASH_KEY, companyName);
    if(Objects.isNull(timestampOperation.get(key))) {
      timestampOperation.set(key, "11");
      timestampOperation.increment(key, -10);
    } else {
      timestampOperation.increment(key) ;
    }

  }

  public Integer getTimestamp() {
    String timestampAsString = timestampOperation.get(String.format(TIMESTAMP_HASH_KEY, companyName));
    return StringUtils.isBlank(timestampAsString) ? null : Integer.parseInt(timestampAsString) ;
  }

  public Filterable<Key> getMergedBlacklist () {
    return hashOperations.get(HASH_KEY,String.format(MERGE_BLACKLIST_HASH_KEY, companyName));
  }

  public Map<String, Integer> getTimestampsMap() {
    return timestampVectorOperations.get(HASH_KEY, String.format(TIMESTAMP_VECTOR, companyName));
  }

}
