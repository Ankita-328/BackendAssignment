package com.example.starter.controller;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class RoleHandler {

  public static Handler<RoutingContext> requireRole(String requiredRole) {
    return ctx -> {
      if (ctx.user() == null) {
        ctx.response().setStatusCode(401).end(new JsonObject().put("error", "Unauthorized").encode());
        return;
      }

      JsonObject principal = ctx.user().principal();
      String userRole = principal.getString("role");

      if (requiredRole.equalsIgnoreCase(userRole)) {
        ctx.next();
      } else {
        ctx.response().setStatusCode(403).end(
          new JsonObject().put("error", "Access Denied: Requires " + requiredRole + " role").encode()
        );
      }
    };
  }
}
