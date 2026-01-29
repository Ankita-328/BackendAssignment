package com.example.starter.controller;

import com.example.starter.service.UpdateService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public enum UpdateController {
  INSTANCE;
  private UpdateService updateService;

  public void init(UpdateService updateService) {
    this.updateService = updateService;
  }

  public void updateProfile(RoutingContext ctx) {
    String userId = ctx.user().principal().getString("userId");
    JsonObject body = ctx.body().asJsonObject();

    String fullName = body.getString("fullName");
    String mobileNumber = body.getString("mobileNumber");
    String courseName = body.getString("courseName");
    String qualification = body.getString("qualification");
    Integer experienceYears = body.getInteger("experienceYears");
    String emailId = body.getString("emailId");

    updateService.updateProfile(userId, fullName, mobileNumber, courseName, qualification, experienceYears,emailId)
      .subscribe(
        updatedUser -> ctx.json(updatedUser),
        error -> ctx.response().setStatusCode(400).end(new JsonObject().put("error", error.getMessage()).encode())
      );
  }
}
