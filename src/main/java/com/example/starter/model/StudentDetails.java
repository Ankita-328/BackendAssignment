package com.example.starter.model;

import io.ebean.Model;
import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "student_details")
public class StudentDetails extends Model {

  @Id
  private UUID id;

  @OneToOne(optional = false)
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  @Column(name = "course_enrolled")
  private String courseEnrolled;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }

  public String getCourseEnrolled() { return courseEnrolled; }
  public void setCourseEnrolled(String courseEnrolled) { this.courseEnrolled = courseEnrolled; }
}
