package com.example.starter.service;

import com.example.starter.model.KycStatus; // <--- Added Import
import com.example.starter.model.Role;
import com.example.starter.model.User;
import com.example.starter.repository.UserRepository;
import com.example.starter.repository.KycRepository;
import com.example.starter.utils.PasswordUtil;
import io.reactivex.rxjava3.core.Single;
import com.example.starter.model.KycSubmission;
import com.example.starter.model.KycStatus;

import java.util.List;
import java.util.UUID;

public class AdminService {

  private final UserRepository userRepository;
  private final KycRepository kycRepository; // <--- 1. ADD THIS FIELD

  // 2. UPDATE CONSTRUCTOR TO ACCEPT BOTH REPOS
  public AdminService(UserRepository userRepository, KycRepository kycRepository) {
    this.userRepository = userRepository;
    this.kycRepository = kycRepository;
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

  // --- NEW KYC METHODS ---

  public Single<List<KycSubmission>> getAllKycs() {
    return kycRepository.findAll();
  }
  public Single<KycSubmission> verifyKyc(String kycId, boolean approve, String reason) {
    return kycRepository.findById(kycId).flatMap(kyc -> {
      if (kyc == null) throw new RuntimeException("KYC not found");

      if (approve) {
        kyc.setStatus(KycStatus.APPROVED);
        kyc.setRejectionReason(null);
      } else {
        kyc.setStatus(KycStatus.REJECTED);
        kyc.setRejectionReason(reason);
      }
      return kycRepository.save(kyc);
    });
  }

  // Replace the old 'verifyKyc' method with this:

  public Single<KycSubmission> updateKycStatus(String kycId, com.example.starter.model.KycStatus newStatus, String reason) {
    return kycRepository.findById(kycId).flatMap(kyc -> {
      if (kyc == null) throw new RuntimeException("KYC not found");

      // Set the new status
      kyc.setStatus(newStatus);

      // Only save the reason if it is REJECTED; otherwise clear it
      if (newStatus == com.example.starter.model.KycStatus.REJECTED) {
        kyc.setRejectionReason(reason);
      } else {
        kyc.setRejectionReason(null);
      }

      return kycRepository.save(kyc);
    });
  }

}
