package com.example.starter.controller;

import com.example.starter.service.KycService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

public class KycController {

  private final KycService kycService;

  public KycController(KycService kycService) {
    this.kycService = kycService;
  }

  public void submitKyc(RoutingContext ctx) {
    String userId = ctx.user().principal().getString("userId");

    if (ctx.fileUploads().isEmpty()) {
      ctx.response().setStatusCode(400).end(new JsonObject().put("error", "No file uploaded").encode());
      return;
    }

    FileUpload file = ctx.fileUploads().iterator().next();
    String filePath = file.uploadedFileName(); // Vert.x stores it in 'uploads/' folder
    String docType = ctx.request().getFormAttribute("documentType");
    String docNumber = ctx.request().getFormAttribute("documentNumber");

    if (docType == null || docNumber == null) {
      ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Missing documentType or documentNumber").encode());
      return;
    }

    kycService.submitKyc(userId, docType, docNumber, filePath).subscribe(
      kyc -> ctx.response().setStatusCode(201).end(new JsonObject().put("message", "KYC Submitted Successfully").put("status", kyc.getStatus()).encode()),
      error -> ctx.response().setStatusCode(400).end(new JsonObject().put("error", error.getMessage()).encode())
    );
  }

  public void getStatus(RoutingContext ctx) {
    String userId = ctx.user().principal().getString("userId");

    kycService.getStatus(userId).subscribe(
      kyc -> ctx.json(kyc),
      error -> ctx.response().setStatusCode(404).end(new JsonObject().put("error", error.getMessage()).encode())
    );
  }
}
