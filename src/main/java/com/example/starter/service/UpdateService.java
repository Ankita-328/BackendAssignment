package com.example.starter.service;

import com.example.starter.model.Role;
import com.example.starter.model.User;
import com.example.starter.model.TeacherDetails;
import com.example.starter.model.StudentDetails;
import com.example.starter.repository.UserRepository;
import com.example.starter.repository.StudentDetailsRepository;
import com.example.starter.repository.TeacherDetailsRepository;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;

public class UpdateService {

  private final UserRepository userRepository;
  private final TeacherDetailsRepository teacherDetailsRepository;
  private final StudentDetailsRepository studentDetailsRepository;

  public UpdateService(UserRepository userRepo, TeacherDetailsRepository teacherDetailsRepository, StudentDetailsRepository studentDetailsRepository) {
    this.userRepository = userRepo;
    this.teacherDetailsRepository = teacherDetailsRepository;
    this.studentDetailsRepository = studentDetailsRepository;
  }

  public Single<User> updateProfile(String userId, String fullName, String mobileNumber, String courseName, String qualification, Integer experienceYears,String emailId) {
    UUID userUuid = UUID.fromString(userId);

    return userRepository.findById(userUuid).flatMap(optUser -> {
      if (optUser.isEmpty()) {
        return Single.error(new RuntimeException("User not found"));
      }

      User user = optUser.get();

      if (fullName != null) user.setFullName(fullName);
      if (mobileNumber != null) user.setMobileNumber(mobileNumber);
      if (emailId != null && !emailId.isEmpty()) {
        user.setEmail(emailId);
      }

      if (user.getRole() == Role.STUDENT) {
        return studentDetailsRepository.findByUserId(userUuid)
          .flatMap(optDetails -> {
            StudentDetails details = optDetails.orElseGet(() -> {
              StudentDetails newDetails = new StudentDetails();
              newDetails.setId(UUID.randomUUID());
              newDetails.setUser(user);
              return newDetails;
            });
            details.setCourseEnrolled(courseName);
            return studentDetailsRepository.save(details).map(saved -> user);
          })
          .flatMap(u -> userRepository.save(u));
      }

      else if (user.getRole() == Role.TEACHER && (qualification != null || experienceYears != null)) {
        return teacherDetailsRepository.findByUserId(userUuid)
          .flatMap(optDetails -> {
            TeacherDetails details = optDetails.orElseGet(() -> {
              TeacherDetails newDetails = new TeacherDetails();
              newDetails.setId(UUID.randomUUID());
              newDetails.setUser(user);
              return newDetails;
            });
            if (qualification != null) details.setQualification(qualification);
            if (experienceYears != null) details.setExperienceYears(experienceYears);
            return teacherDetailsRepository.save(details).map(saved -> user);
          })
          .flatMap(u -> userRepository.save(u));
      }
      else {
        return userRepository.save(user);
      }
    });
  }
}


