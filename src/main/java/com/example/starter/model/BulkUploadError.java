package com.example.starter.model;

import javax.persistence.*;

@Entity
@Table(name = "bulk_upload_errors")
public class BulkUploadError {

  @Id
  @GeneratedValue
  private Long id;

  private String uploadId;
  private String email;
  private String reason;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUploadId() {
    return uploadId;
  }

  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
