package com.example.starter.repository;

import com.example.starter.model.User;
import io.ebean.DB;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;

import java.util.Optional;

public class UserRepository {

  private final Vertx vertx;

  public UserRepository(Vertx vertx) {
    this.vertx = vertx;
  }

  public Single<Optional<User>> findByEmail(String email) {
    return Single.create(emitter -> {
      vertx.executeBlocking(promise -> {
        try {
          User user = DB.find(User.class)
            .where().eq("email", email)
            .findOne();
          promise.complete(Optional.ofNullable(user));
        } catch (Exception e) {
          promise.fail(e);
        }
      }, res -> {
        if (res.succeeded()) {
          emitter.onSuccess((Optional<User>) res.result());
        } else {
          emitter.onError(res.cause());
        }
      });
    });
  }


  public Single<User> save(User user) {
    return Single.create(emitter -> {
      vertx.executeBlocking(promise -> {
        try {
          user.save();
          promise.complete(user);
        } catch (Exception e) {
          promise.fail(e);
        }
      }, res -> {
        if (res.succeeded()) {
          emitter.onSuccess((User) res.result());
        } else {
          emitter.onError(res.cause());
        }
      });
    });
  }
  public Single<Optional<User>> findById(java.util.UUID id) {
    return Single.create(emitter -> {
      vertx.executeBlocking(promise -> {
        try {
          User user = DB.find(User.class, id);
          promise.complete(Optional.ofNullable(user));
        } catch (Exception e) {
          promise.fail(e);
        }
      }, res -> {
        if (res.succeeded()) {
          emitter.onSuccess((Optional<User>) res.result());
        } else {
          emitter.onError(res.cause());
        }
      });
    });
  }

  public Single<java.util.List<User>> findAllByRole(com.example.starter.model.Role role) {
    return Single.create(emitter -> {
      vertx.executeBlocking(promise -> {
        try {
          java.util.List<User> users = DB.find(User.class)
            .where().eq("role", role)
            .findList();
          promise.complete(users);
        } catch (Exception e) {
          promise.fail(e);
        }
      }, res -> {
        if (res.succeeded()) {
          emitter.onSuccess((java.util.List<User>) res.result());
        } else {
          emitter.onError(res.cause());
        }
      });
    });
  }
}
