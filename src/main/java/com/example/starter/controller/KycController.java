
package com.example.starter.controller;

import com.example.starter.model.KycSubmission;
import com.example.starter.repository.KycRepository;
import com.example.starter.service.KycService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

public class KycController {

  private final KycService kycService;
  private final KycRepository kycRepository;

  public KycController(KycService kycService, KycRepository kycRepository) {
    this.kycService = kycService;
    this.kycRepository = kycRepository;
  }

  public void submitKyc(RoutingContext ctx) {
    String userId = ctx.user().principal().getString("userId");

    if (ctx.fileUploads().isEmpty()) {
      ctx.response().setStatusCode(400).end(new JsonObject().put("error", "No file uploaded").encode());
      return;
    }

    FileUpload file = ctx.fileUploads().iterator().next();
    String filePath = file.uploadedFileName();
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

//
public void getKycById(RoutingContext ctx) {
  String kycId = ctx.pathParam("id");

  kycRepository.findById(kycId)
    .subscribe(
      optKyc -> {
        if (optKyc.isPresent()) {
          KycSubmission kyc = optKyc.get();

          JsonObject response = new JsonObject();
          response.put("kycId", kyc.getId().toString());

          // User Info
          if (kyc.getUser() != null) {
            response.put("userId", kyc.getUser().getId().toString());
            response.put("role", kyc.getUser().getRole());
          }



          ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(response.encodePrettily());

        } else {
          ctx.response().setStatusCode(404).end(new JsonObject().put("error", "KYC ID not found").encode());
        }
      },
      err -> {
        err.printStackTrace();
        ctx.response().setStatusCode(500).end("Internal Server Error");
      }
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
