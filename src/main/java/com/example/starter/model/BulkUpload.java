package com.example.starter.model;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bulk_uploads")
public class BulkUpload {

  @Id
  private String id;

  private String adminId;
  private int totalRecords;
  private int successCount;
  private int failureCount;

  private String status;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "completed_at")
  private Instant completedAt;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAdminId() {
    return adminId;
  }

  public void setAdminId(String adminId) {
    this.adminId = adminId;
  }

  public int getTotalRecords() {
    return totalRecords;
  }

  public void setTotalRecords(int totalRecords) {
    this.totalRecords = totalRecords;
  }

  public int getSuccessCount() {
    return successCount;
  }

  public void setSuccessCount(int successCount) {
    this.successCount = successCount;
  }

  public int getFailureCount() {
    return failureCount;
  }

  public void setFailureCount(int failureCount) {
    this.failureCount = failureCount;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @PrePersist
  public void onCreate() {
    this.createdAt = Instant.now();
    this.completedAt = Instant.now();
  }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getCompletedAt() { return completedAt; }
}
