package com.example.starter.config;

import com.example.starter.model.Role;
import com.example.starter.model.User;
import com.example.starter.utils.PasswordUtil;
import io.ebean.DB;

public class AdminSeeder {

  public static void seed() {
    int adminCount = DB.find(User.class)
      .where().eq("role", Role.ADMIN)
      .findCount();


    if (adminCount == 0) {
      System.out.println("🌱 No admin found. Seeding default Super Admin...");

      User admin = new User();
      admin.setFullName("Super Admin");
      admin.setEmail("admin@lms.com");
      admin.setMobileNumber("0000000000");
      admin.setRole(Role.ADMIN);

      admin.setPassword(PasswordUtil.hash("Admin@123"));

      admin.setActive(true);
      admin.save();

      System.out.println("Default Admin Created: admin@lms.com / Admin@123");
    } else {
      System.out.println("Admin already exists.");
    }
  }
}
