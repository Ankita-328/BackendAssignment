package com.example.starter.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Model;
import io.ebean.annotation.NotNull;
import javax.persistence.*;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User extends Model {

  @Id
  private UUID id;

  @Enumerated(EnumType.STRING)
  @NotNull
  private Role role;

  @Column(unique = true)
  @NotNull
  private String email;

  @NotNull
  private String fullName;

  private String mobileNumber;

  @NotNull
  @JsonIgnore
  private String password; // Hashed

  private boolean active = true;

  private String qualification;
  private Integer experienceYears;
  private String courseEnrolled;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  public void onCreate() {
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = Instant.now();
  }

   public UUID getId() { return id; }
   public void setId(UUID id) { this.id = id; }

   public Role getRole() { return role; }
   public void setRole(Role role) { this.role = role; }

   public String getEmail() { return email; }
   public void setEmail(String email) { this.email = email; }

   public String getFullName() { return fullName; }
   public void setFullName(String fullName) { this.fullName = fullName; }

   public String getMobileNumber() { return mobileNumber; }
   public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

   public String getPassword() { return password; }
   public void setPassword(String password) { this.password = password; }

   public boolean isActive() { return active; }
   public void setActive(boolean active) { this.active = active; }

   public Instant getCreatedAt() { return createdAt; }
   public Instant getUpdatedAt() { return updatedAt; }

  public String getQualification() { return qualification; }
  public void setQualification(String qualification) { this.qualification = qualification; }
  public Integer getExperienceYears() { return experienceYears; }
  public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
  public String getCourseEnrolled() { return courseEnrolled; }
  public void setCourseEnrolled(String courseEnrolled) { this.courseEnrolled = courseEnrolled; }
}
