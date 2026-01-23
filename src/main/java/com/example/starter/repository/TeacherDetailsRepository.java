package com.example.starter.repository;

import com.example.starter.model.TeacherDetails;
import io.ebean.DB;
import io.reactivex.rxjava3.core.Single;
import java.util.Optional;
import java.util.UUID;

public class TeacherDetailsRepository {

  public Single<Optional<TeacherDetails>> findByUserId(UUID userId) {
    return Single.fromCallable(() ->
      DB.find(TeacherDetails.class)
        .where()
        .eq("user.id", userId)
        .findOneOrEmpty()
    );
  }

  public Single<TeacherDetails> save(TeacherDetails details) {
    return Single.fromCallable(() -> {
      DB.save(details);
      return details;
    });
  }
}
