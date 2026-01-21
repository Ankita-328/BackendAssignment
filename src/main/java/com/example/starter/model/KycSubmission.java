package com.example.starter.model;

import io.ebean.Model;
import io.ebean.annotation.NotNull;
import javax.persistence.*;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "kyc_submissions")
public class KycSubmission extends Model {

  @Id
  private UUID id;


  @OneToOne
  @JoinColumn(name = "user_id", unique = true)
  @NotNull
  private User user;

  @NotNull
  private String documentType;

  @NotNull
  private String documentNumber;

  @NotNull
  private String documentPath;

  @Enumerated(EnumType.STRING)
  private KycStatus status = KycStatus.PENDING;


  private Instant submittedAt;

  @PrePersist
  public void onCreate() {
    this.submittedAt = Instant.now();
  }


  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }

  public String getDocumentType() { return documentType; }
  public void setDocumentType(String documentType) { this.documentType = documentType; }

  public String getDocumentNumber() { return documentNumber; }
  public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

  public String getDocumentPath() { return documentPath; }
  public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

  public KycStatus getStatus() { return status; }
  public void setStatus(KycStatus status) { this.status = status; }


  public Instant getSubmittedAt() { return submittedAt; }

}
