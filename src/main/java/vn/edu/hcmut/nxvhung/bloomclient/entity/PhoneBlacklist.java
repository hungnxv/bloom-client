package vn.edu.hcmut.nxvhung.bloomclient.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "phone_blacklist")
public class PhoneBlacklist implements Serializable {
  private static final long serialVersionUID = -6898807534187544624L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name="seq",sequenceName="blacklist_seq")
  @Column(name = "id")
  private Long id;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "added_time")
  private LocalDateTime addedTime;

  @Column(name = "removed_time")
  private LocalDateTime removedTime;

  @Column(name = "deleted")
  private Boolean deleted;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public LocalDateTime getAddedTime() {
    return addedTime;
  }

  public void setAddedTime(LocalDateTime addedTime) {
    this.addedTime = addedTime;
  }

  public LocalDateTime getRemovedTime() {
    return removedTime;
  }

  public void setRemovedTime(LocalDateTime removedTime) {
    this.removedTime = removedTime;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }
}
