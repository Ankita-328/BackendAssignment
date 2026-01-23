package com.example.starter.repository;

import com.example.starter.model.StudentDetails;
import io.ebean.DB;
import io.reactivex.rxjava3.core.Single;
import java.util.Optional;
import java.util.UUID;

public class StudentDetailsRepository {

  public Single<Optional<StudentDetails>> findByUserId(UUID userId) {
    return Single.fromCallable(() ->
      DB.find(StudentDetails.class)
        .where()
        .eq("user.id", userId)
        .findOneOrEmpty()
    );
  }


  public Single<StudentDetails> save(StudentDetails details) {
    return Single.fromCallable(() -> {
      DB.save(details);
      return details;
    });
  }
}
