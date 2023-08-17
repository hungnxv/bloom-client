package vn.edu.hcmut.nxvhung.bloomclient.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "phone_blacklist")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneBlacklist implements Serializable {
  private static final long serialVersionUID = -6898807534187544624L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "added_time")
  private LocalDateTime addedTime;

  @Column(name = "expired_time")
  private LocalDateTime expiredTime;

  @Column(name = "deleted")
  private Boolean deleted = false;

}
