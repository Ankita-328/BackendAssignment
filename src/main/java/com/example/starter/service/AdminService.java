package com.example.starter.service;

import com.example.starter.model.Role;
import com.example.starter.model.User;
import com.example.starter.repository.UserRepository;
import com.example.starter.utils.PasswordUtil;
import io.reactivex.rxjava3.core.Single;

import java.util.List;
import java.util.UUID;

public class AdminService {

  private final UserRepository userRepository;

  public AdminService(UserRepository userRepository) {
    this.userRepository = userRepository;
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
}
