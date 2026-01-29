package com.example.starter.controller;

import com.example.starter.model.Role;
import com.example.starter.service.AdminService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public enum AdminController {
  INSTANCE;

  private AdminService adminService;

  public void init(AdminService adminService) {
    this.adminService = adminService;
  }

  public void onboardStudent(RoutingContext ctx) {
    onboard(ctx, Role.STUDENT);
  }

  public void onboardTeacher(RoutingContext ctx) {
    onboard(ctx, Role.TEACHER);
  }

  private void onboard(RoutingContext ctx, Role role) {
    JsonObject body = ctx.body().asJsonObject();

    if (body == null || !body.containsKey("email") || !body.containsKey("initialPassword") || !body.containsKey("fullName")) {
      ctx.response().setStatusCode(400).end(
        new JsonObject().put("error", "Missing required fields: fullName, email, initialPassword").encode()
      );
      return;
    }

    adminService.onboardUser(
      body.getString("fullName"),
      body.getString("email"),
      body.getString("mobileNumber"),
      body.getString("initialPassword"),
      role
    ).subscribe(
      createdUser -> {
        ctx.response().setStatusCode(201).end(
          new JsonObject()
            .put("message", role + " created successfully")
            .put("userId", createdUser.getId().toString())
            .encode()
        );
      },
      error -> {
        ctx.response().setStatusCode(400).end(
          new JsonObject().put("error", error.getMessage()).encode()
        );
      }
    );
  }

  public void getProfile(RoutingContext ctx) {
    String userId = ctx.user().principal().getString("userId");

    adminService.getUserProfile(userId).subscribe(
      user -> ctx.json(user),
      error -> ctx.response().setStatusCode(404).end(new JsonObject().put("error", error.getMessage()).encode())
    );
  }

  public void updateProfile(RoutingContext ctx) {
    String userId = ctx.user().principal().getString("userId");
    JsonObject body = ctx.body().asJsonObject();

    adminService.updateProfile(userId, body.getString("fullName"), body.getString("mobileNumber"))
      .subscribe(
        updatedUser -> ctx.json(updatedUser),
        error -> ctx.response().setStatusCode(400).end(new JsonObject().put("error", error.getMessage()).encode())
      );
  }

  public void listUsers(RoutingContext ctx) {
    String roleParam = ctx.request().getParam("role");

    if (roleParam == null) {
      ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Query param 'role' is required").encode());
      return;
    }

    try {
      Role role = Role.valueOf(roleParam.toUpperCase());
      adminService.listUsers(role).subscribe(
        users -> ctx.json(users),
        error -> ctx.response().setStatusCode(500).end(new JsonObject().put("error", error.getMessage()).encode())
      );
    } catch (IllegalArgumentException e) {
      ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Invalid role specified").encode());
    }
  }

  public void changeUserStatus(RoutingContext ctx) {
    String userId = ctx.pathParam("userId");
    JsonObject body = ctx.body().asJsonObject();

    if (body == null || !body.containsKey("active")) {
      ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Body must contain 'active' boolean").encode());
      return;
    }

    adminService.toggleUserStatus(userId, body.getBoolean("active")).subscribe(
      message -> ctx.json(new JsonObject().put("message", message)),
      error -> ctx.response().setStatusCode(404).end(new JsonObject().put("error", error.getMessage()).encode())
    );
  }

  public void listKycs(RoutingContext ctx) {
    adminService.getAllKycs().subscribe(
      list -> ctx.json(list),
      error -> ctx.response().setStatusCode(500).end(new JsonObject().put("error", error.getMessage()).encode())
    );
  }


  public void reviewKyc(RoutingContext ctx) {
    String kycId = ctx.pathParam("id");
    JsonObject body = ctx.body().asJsonObject();

    if (body == null || !body.containsKey("status")) {
      ctx.response().setStatusCode(400).end(
        new JsonObject().put("error", "Body must contain 'status' string (PENDING, SUBMITTED, APPROVED, REJECTED)").encode()
      );
      return;
    }

    try {
      com.example.starter.model.KycStatus newStatus =
        com.example.starter.model.KycStatus.valueOf(body.getString("status").toUpperCase());


      adminService.updateKycStatus(kycId, newStatus).subscribe(
        updatedKyc -> {
          ctx.response().setStatusCode(200).end(
            new JsonObject()
              .put("message", "KYC status updated to " + updatedKyc.getStatus())
              .put("id", updatedKyc.getId().toString())
              .encode()
          );
        },
        error -> {
          int code = error.getMessage().contains("not found") ? 404 : 400;
          ctx.response().setStatusCode(code).end(
            new JsonObject().put("error", error.getMessage()).encode()
          );
        }
      );

    } catch (IllegalArgumentException e) {
      ctx.response().setStatusCode(400).end(
        new JsonObject().put("error", "Invalid status. Allowed: PENDING, SUBMITTED, APPROVED, REJECTED").encode()
      );
    }
  }

  public void downloadCsvTemplate(RoutingContext ctx) {
    try {
      java.net.URL resource = getClass().getClassLoader().getResource("users.csv");
      if (resource == null) {
        ctx.response().setStatusCode(404).end("File not found!");
        return;
      }
      String path = java.nio.file.Paths.get(resource.toURI()).toFile().getAbsolutePath();
      ctx.response()
        .putHeader("Content-Disposition", "attachment; filename=\"users.csv\"")
        .sendFile(path);

    } catch (Exception e) {
      ctx.response().setStatusCode(500).end("Error: " + e.getMessage());
    }
  }

  public void uploadCsv(RoutingContext ctx) {
    if (ctx.fileUploads().isEmpty()) {
      ctx.response().setStatusCode(400).end("No CSV file");
      return;
    }

    String adminId = ctx.user().principal().getString("userId");
    String filePath = ctx.fileUploads().iterator().next().uploadedFileName();

    adminService.startBulkUpload(adminId, filePath).subscribe(
      uploadId -> ctx.json(new JsonObject()
        .put("message", "Upload started")
        .put("uploadId", uploadId)), // Returns ID immediately
      err -> ctx.response().setStatusCode(400).end(err.getMessage())
    );
  }

  public void getBulkStatus(RoutingContext ctx) {
    String uploadId = ctx.pathParam("id");
    adminService.getUploadStatus(uploadId).subscribe(
      status -> ctx.json(status),
      err -> ctx.response().setStatusCode(404).end("Job not found")
    );
  }

  public void getBulkErrors(RoutingContext ctx) {
    String uploadId = ctx.pathParam("id");
    adminService.getUploadErrors(uploadId).subscribe(
      errors -> ctx.json(errors),
      err -> ctx.response().setStatusCode(500).end(err.getMessage())
    );
  }
}
