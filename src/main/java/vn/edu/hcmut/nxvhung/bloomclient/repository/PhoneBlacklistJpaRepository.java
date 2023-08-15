package vn.edu.hcmut.nxvhung.bloomclient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmut.nxvhung.bloomclient.entity.PhoneBlacklist;

@Repository
public interface PhoneBlacklistJpaRepository extends JpaRepository<PhoneBlacklist, Long> {
  PhoneBlacklist findByPhoneNumber(String phone);
}
