package com.example.starter.service;

import com.example.starter.model.Role;
import com.example.starter.model.User;
import com.example.starter.model.TeacherDetails;
import com.example.starter.model.StudentDetails;
import com.example.starter.repository.UserRepository;
import com.example.starter.repository.KycRepository;
import com.example.starter.repository.StudentDetailsRepository;
import com.example.starter.repository.TeacherDetailsRepository;

import com.example.starter.utils.PasswordUtil;
import io.reactivex.rxjava3.core.Single;
import com.example.starter.model.KycSubmission;
import io.reactivex.rxjava3.core.Observable;

import com.example.starter.model.BulkUpload;
import com.example.starter.repository.BulkUploadRepository;
import com.example.starter.utils.CsvUtil;
import java.time.LocalDateTime;
import io.vertx.core.json.JsonObject;
import com.example.starter.model.BulkUploadError;

import java.util.List;
import java.util.UUID;

public class AdminService {

  private final UserRepository userRepository;
  private final KycRepository kycRepository;
  private final BulkUploadRepository bulkRepository;



  public AdminService(UserRepository userRepo, KycRepository kycRepo, BulkUploadRepository bulkRepo) {
    this.userRepository = userRepo;
    this.kycRepository = kycRepo;
    this.bulkRepository = bulkRepo;
  }


  public Single<User> onboardUser(String fullName, String email, String mobile, String initialPassword, Role role) {
    return userRepository.findByEmail(email).flatMap(optUser -> {
      if (optUser.isPresent()) {
        return Single.error(new RuntimeException("User with this email already exists"));
      }
      User newUser = new User();
      newUser.setFullName(fullName);
      newUser.setEmail(email);
      newUser.setMobileNumber(mobile);
      newUser.setPassword(PasswordUtil.hash(initialPassword));
      newUser.setRole(role);
      newUser.setActive(true);

      return userRepository.save(newUser);
    });
  }

  public Single<User> getUserProfile(String userId) {
    return userRepository.findById(UUID.fromString(userId)).map(optUser -> {
      if (optUser.isEmpty()) {
        throw new RuntimeException("User not found");
      }
      return optUser.get();
    });
  }

  public Single<User> updateProfile(String userId, String fullName, String mobileNumber) {
    return userRepository.findById(UUID.fromString(userId)).flatMap(optUser -> {
      if (optUser.isEmpty()) {
        return Single.error(new RuntimeException("User not found"));
      }

      User user = optUser.get();
      if (fullName != null) user.setFullName(fullName);
      if (mobileNumber != null) user.setMobileNumber(mobileNumber);

      return userRepository.save(user);
    });
  }

  public Single<List<User>> listUsers(Role role) {
    return userRepository.findAllByRole(role);
  }

  public Single<String> toggleUserStatus(String userId, boolean isActive) {
    return userRepository.findById(UUID.fromString(userId)).flatMap(optUser -> {
      if (optUser.isEmpty()) {
        return Single.error(new RuntimeException("User not found"));
      }

      User user = optUser.get();
      user.setActive(isActive);

      return userRepository.save(user).map(u -> "User status updated to: " + isActive);
    });
  }


  public Single<List<KycSubmission>> getAllKycs() {
    return kycRepository.findAll();
  }


public Single<KycSubmission> updateKycStatus(String kycId, com.example.starter.model.KycStatus newStatus) {
  return kycRepository.findById(kycId).flatMap(optKyc -> { // optKyc is Optional<KycSubmission>

    if (optKyc.isEmpty()) {
      return Single.error(new RuntimeException("KYC not found"));
    }

    KycSubmission kyc = optKyc.get();
    kyc.setStatus(newStatus);

    return kycRepository.save(kyc);
  });
}





  public Single<String> startBulkUpload(String adminId, String filePath) {
    String uploadId = UUID.randomUUID().toString();

    List<JsonObject> usersData;
    try {
      usersData = CsvUtil.parseCsv(filePath);
    } catch (Exception e) {
      return Single.error(new RuntimeException("Invalid CSV File"));
    }

    BulkUpload upload = new BulkUpload();
    upload.setId(uploadId);
    upload.setAdminId(adminId);
    upload.setTotalRecords(usersData.size());
    upload.setStatus("IN_PROGRESS");
    upload.setSuccessCount(0);
    upload.setFailureCount(0);

    return bulkRepository.save(upload).map(saved -> {
      processBulkUsers(uploadId, usersData);

      return uploadId;
    });
  }

  private void processBulkUsers(String uploadId, List<JsonObject> users) {
    io.reactivex.rxjava3.core.Observable.fromIterable(users)
      .concatMapSingle(data -> {
        com.example.starter.model.Role role;
        try {
          role = com.example.starter.model.Role.valueOf(data.getString("role").toUpperCase());
        } catch (Exception e) {
          return io.reactivex.rxjava3.core.Single.just("ERROR:Invalid Role");
        }

        return onboardUser(
          data.getString("fullName"),
          data.getString("email"),
          data.getString("mobileNumber"),
          data.getString("initialPassword"),
          role
        )
          .map(u -> "SUCCESS")
          .onErrorReturn(e -> "ERROR:" + e.getMessage());
      })
      .toList()
      .subscribe(results -> {
        int success = 0;
        int failure = 0;

        for (int i = 0; i < results.size(); i++) {
          String result = results.get(i);
          if ("SUCCESS".equals(result)) {
            success++;
          } else {
            failure++;
            String reason = result.replace("ERROR:", "");
            String email = users.get(i).getString("email");

            bulkRepository.saveError(uploadId, email, reason);
          }
        }

        int finalSuccess = success;
        int finalFailure = failure;

        bulkRepository.findById(uploadId).subscribe(record -> {
          if (record != null) {
            record.setSuccessCount(finalSuccess);
            record.setFailureCount(finalFailure);
            record.setStatus("COMPLETED");
            bulkRepository.save(record).subscribe();
          }
        });
      });
  }

  public Single<BulkUpload> getUploadStatus(String uploadId) {
    return bulkRepository.findById(uploadId);
  }

  public Single<List<BulkUploadError>> getUploadErrors(String uploadId) {
    return bulkRepository.findErrors(uploadId);
  }

}
