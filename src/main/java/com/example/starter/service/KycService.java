package com.example.starter.service;

import com.example.starter.model.KycSubmission;
import com.example.starter.model.User;
import com.example.starter.repository.KycRepository;
import com.example.starter.repository.UserRepository;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;
import java.util.regex.Pattern;
public class KycService {

  private final KycRepository kycRepository;
  private final UserRepository userRepository;
  private final AiService aiService;

  public KycService(KycRepository kycRepository, UserRepository userRepository, AiService aiService) {
    this.kycRepository = kycRepository;
    this.userRepository = userRepository;
    this.aiService = aiService;
  }



  public Single<KycSubmission> getStatus(String userId) {
    return kycRepository.findByUserId(userId).map(optKyc -> {
      if (optKyc.isEmpty()) throw new RuntimeException("No KYC submission found");
      return optKyc.get();
    });
  }
  private void validateDocument(String type, String number) {
    if (type == null || number == null) {
      throw new IllegalArgumentException("Document type and number cannot be null");
    }
<<<<<<< HEAD
    String lowerPath = type.toLowerCase();
    boolean isValid = lowerPath.endsWith(".jpg") ||
      lowerPath.endsWith(".jpeg") ||
      lowerPath.endsWith(".pdf");
=======
//    String lowerPath = type.toLowerCase();
//    boolean isValid = lowerPath.endsWith(".jpg") ||
//      lowerPath.endsWith(".jpeg") ||
//      lowerPath.endsWith(".pdf");
//
//    if (!isValid) {
//      throw new IllegalArgumentException("Invalid file type. Only JPG, JPEG, and PDF are allowed.");
//    }
>>>>>>> bc9de24 (cookie & enum functionality)

    switch (type.toUpperCase()) {
      case "PAN":
        String panRegex = "[A-Z]{5}[0-9]{4}[A-Z]{1}";
        if (!Pattern.matches(panRegex, number)) {
          throw new IllegalArgumentException("Invalid PAN format. Expected format: ABCDE1234F");
        }
        break;

      case "AADHAAR":
        String aadhaarRegex = "\\d{12}";
        if (!Pattern.matches(aadhaarRegex, number)) {
          throw new IllegalArgumentException("Invalid Aadhaar format. Expected 12 digits.");
        }
        break;

      default:
        throw new IllegalArgumentException("Invalid Document Type. Please check docType is 'AADHAAR' or 'PAN'");

    }
  }

  public Single<KycSubmission> submitKyc(String userId, String documentType, String documentNumber, String filePath) {

    UUID userUuid;
    try {
      userUuid = UUID.fromString(userId);
    } catch (IllegalArgumentException e) {
      return Single.error(new RuntimeException("Invalid User ID format"));
    }
    try {
      validateDocument(documentType, documentNumber);
    } catch (IllegalArgumentException e) {
      return Single.error(e);
    }

    return userRepository.findById(userUuid)
      .flatMap(optUser -> {
        if (optUser.isEmpty()) {
          return Single.error(new RuntimeException("User not found"));
        }

        KycSubmission kyc = new KycSubmission();
        kyc.setId(UUID.randomUUID());
        kyc.setUser(optUser.get());
        kyc.setDocumentType(documentType);
        kyc.setDocumentNumber(documentNumber);
        kyc.setDocumentPath(filePath);
        kyc.setAiStatus("PENDING");

        return kycRepository.save(kyc);
      })
      .map(savedKyc -> {
        String userRole = savedKyc.getUser().getRole().toString();
        aiService.analyzeKyc(
          savedKyc.getId().toString(),
          documentType,
          userRole,filePath
        );

        return savedKyc;
      });
  }
}
