package com.example.starter.service;

import com.example.starter.model.User;
import com.example.starter.repository.UserRepository;
import com.example.starter.utils.PasswordUtil;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import java.util.Collections;

public class AuthService {

  private final UserRepository userRepository;
  private final JWTAuth jwtAuth;

  public AuthService(UserRepository userRepository, JWTAuth jwtAuth) {
    this.userRepository = userRepository;
    this.jwtAuth = jwtAuth;
  }

  public Single<String> login(String email, String password) {
    return userRepository.findByEmail(email).map(optUser -> {

      if (optUser.isEmpty()) {
        throw new RuntimeException("User not found");
      }

      User user = optUser.get();

      if (!PasswordUtil.verify(password, user.getPassword())) {
        throw new RuntimeException("Invalid Password");
      }

      if (!user.isActive()) {
        throw new RuntimeException("Account is disabled. Contact Admin.");
      }

      return jwtAuth.generateToken(
        new JsonObject()
          .put("userId", user.getId().toString())
          .put("role", user.getRole().toString()),
        new JWTOptions().setExpiresInMinutes(7200)
      );
    });
  }
}
