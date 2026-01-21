package com.example.starter.repository;

import com.example.starter.model.KycSubmission;
import io.ebean.DB;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import java.util.Optional;
import java.util.UUID;

public class KycRepository {

  private final Vertx vertx;

  public KycRepository(Vertx vertx) {
    this.vertx = vertx;
  }

  public Single<KycSubmission> save(KycSubmission kyc) {
    return Single.create(emitter -> {
      vertx.executeBlocking(promise -> {
        try {
          kyc.save();
          promise.complete(kyc);
        } catch (Exception e) {
          promise.fail(e);
        }
      }, res -> {
        if (res.succeeded()) emitter.onSuccess((KycSubmission) res.result());
        else emitter.onError(res.cause());
      });
    });
  }

  public Single<java.util.List<KycSubmission>> findAll() {
    return Single.create(emitter -> {
      vertx.executeBlocking(promise -> {
        try {
          // .fetch("user") tells Ebean to also grab the User's name/email automatically
          java.util.List<KycSubmission> list = DB.find(KycSubmission.class)
            .fetch("user")
            .findList();
          promise.complete(list);
        } catch (Exception e) {
          promise.fail(e);
        }
      }, res -> {
        if (res.succeeded()) emitter.onSuccess((java.util.List<KycSubmission>) res.result());
        else emitter.onError(res.cause());
      });
    });
  }

  public Single<Optional<KycSubmission>> findByUserId(String userId) {
    return Single.create(emitter -> {
      vertx.executeBlocking(promise -> {
        try {
          KycSubmission kyc = DB.find(KycSubmission.class)
            .where().eq("user.id", UUID.fromString(userId))
            .findOne();
          promise.complete(Optional.ofNullable(kyc));
        } catch (Exception e) {
          promise.fail(e);
        }
      }, res -> {
        if (res.succeeded()) emitter.onSuccess((Optional<KycSubmission>) res.result());
        else emitter.onError(res.cause());
      });
    });
  }

  // --- THIS WAS MISSING! ADD THIS METHOD ---
  public Single<KycSubmission> findById(String id) {
    return Single.create(emitter -> {
      vertx.executeBlocking(promise -> {
        try {
          KycSubmission kyc = DB.find(KycSubmission.class, id);
          promise.complete(kyc);
        } catch (Exception e) {
          promise.fail(e);
        }
      }, res -> {
        if (res.succeeded()) emitter.onSuccess((KycSubmission) res.result());
        else emitter.onError(res.cause());
      });
    });
  }
}
