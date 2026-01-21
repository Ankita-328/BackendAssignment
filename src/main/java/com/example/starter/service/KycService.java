package com.example.starter.service;

import com.example.starter.model.KycSubmission;
import com.example.starter.model.User;
import com.example.starter.repository.KycRepository;
import com.example.starter.repository.UserRepository;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;

public class KycService {

  private final KycRepository kycRepository;
  private final UserRepository userRepository;

  public KycService(KycRepository kycRepository, UserRepository userRepository) {
    this.kycRepository = kycRepository;
    this.userRepository = userRepository;
  }

  public Single<KycSubmission> submitKyc(String userId, String docType, String docNumber, String filePath) {
    return userRepository.findById(UUID.fromString(userId)).flatMap(optUser -> {
      if (optUser.isEmpty()) return Single.error(new RuntimeException("User not found"));
      User user = optUser.get();

      return kycRepository.findByUserId(userId).flatMap(optKyc -> {
        if (optKyc.isPresent()) {
          return Single.error(new RuntimeException("KYC already submitted. Current status: " + optKyc.get().getStatus()));
        }

        KycSubmission kyc = new KycSubmission();
        kyc.setUser(user);
        kyc.setDocumentType(docType);
        kyc.setDocumentNumber(docNumber);
        kyc.setDocumentPath(filePath);

        return kycRepository.save(kyc);
      });
    });
  }

  public Single<KycSubmission> getStatus(String userId) {
    return kycRepository.findByUserId(userId).map(optKyc -> {
      if (optKyc.isEmpty()) throw new RuntimeException("No KYC submission found");
      return optKyc.get();
    });
  }
}
