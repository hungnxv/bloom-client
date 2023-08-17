package vn.edu.hcmut.nxvhung.bloomclient.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.hcmut.nxvhung.bloomclient.entity.PhoneBlacklist;

@Repository
public interface PhoneBlacklistJpaRepository extends JpaRepository<PhoneBlacklist, Long> {
  List<PhoneBlacklist> findByPhoneNumberAndDeletedIsFalse(String phone);

  @Query(value = "SELECT * FROM phone_blacklist b where b.expired_time  >= now() and deleted = false", nativeQuery = true)
  List<PhoneBlacklist> findActiveBlacklists();
}
