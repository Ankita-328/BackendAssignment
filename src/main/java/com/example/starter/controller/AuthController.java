package com.example.starter.controller;

import com.example.starter.service.AuthService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }
  public void logout(RoutingContext ctx) {
    if (ctx.session() != null) {
      ctx.session().destroy();
    }

    ctx.response()
      .setStatusCode(200)
      .putHeader("content-type", "application/json")
      .end(new JsonObject()
        .put("status", "success")
        .put("message", "Logged out successfully")
        .encode());
  }

  public void login(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();

    if (body == null || !body.containsKey("email") || !body.containsKey("password")) {
      ctx.response().setStatusCode(400).end(
        new JsonObject().put("error", "Email and Password are required").encode()
      );
      return;
    }

    authService.login(body.getString("email"), body.getString("password"))
      .subscribe(
        token -> {
          ctx.response().putHeader("Content-Type", "application/json")
            .end(new JsonObject().put("token", token).encode());
        },
        error -> {
          ctx.response().setStatusCode(401).end(
            new JsonObject().put("error", error.getMessage()).encode()
          );
        }
      );
  }
}
