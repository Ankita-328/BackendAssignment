package com.example.starter.model;

import io.ebean.Model;
import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "teacher_details")
public class TeacherDetails extends Model {

  @Id
  private UUID id;

  @OneToOne(optional = false)
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  private String qualification;

  @Column(name = "experience_years")
  private Integer experienceYears;


  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }

  public String getQualification() { return qualification; }
  public void setQualification(String qualification) { this.qualification = qualification; }

  public Integer getExperienceYears() { return experienceYears; }
  public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
}
