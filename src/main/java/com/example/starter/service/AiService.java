package com.example.starter.service;

import com.example.starter.model.KycSubmission;
import com.example.starter.repository.KycRepository;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import java.util.Optional;

public class AiService {

  private final WebClient webClient;
  private final KycRepository kycRepository;

  private static final String API_KEY = "sk-or-v1-";
  private static final String AI_MODEL = "google/gemini-2.0-flash-exp:free";

  public AiService(Vertx vertx, KycRepository kycRepository) {
    this.webClient = WebClient.create(vertx);
    this.kycRepository = kycRepository;
  }


  public void analyzeKyc(String kycId, String documentType, String userRole) {

    String prompt =
      "You are a rigorous KYC API. Output ONLY JSON.\n" +
        "Analyze this: Role=" + userRole + ", Doc=" + documentType + ".\n" +
        "Rules: 'confidenceScore' (0-100), 'riskFlags' (list), 'recommendation' (APPROVE/MANUAL_REVIEW).";

    JsonObject requestBody = new JsonObject()
      .put("model", AI_MODEL)
      .put("messages", new JsonArray().add(new JsonObject().put("role", "user").put("content", prompt)));

    webClient.postAbs("https://openrouter.ai/api/v1/chat/completions")
      .putHeader("Authorization", "Bearer " + API_KEY)
      .putHeader("Content-Type", "application/json")
      .putHeader("HTTP-Referer", "http://localhost:8000")
      .rxSendJsonObject(requestBody)
      .flatMapCompletable(response -> {

        if (response.statusCode() == 200) {
          String rawContent = response.bodyAsJsonObject()
            .getJsonArray("choices").getJsonObject(0).getJsonObject("message").getString("content");

          String cleanJson = rawContent.replaceAll("```json", "").replaceAll("```", "").trim();
          if(cleanJson.indexOf("{") > 0) cleanJson = cleanJson.substring(cleanJson.indexOf("{"));
          if(cleanJson.lastIndexOf("}") < cleanJson.length() -1) cleanJson = cleanJson.substring(0, cleanJson.lastIndexOf("}") + 1);

          return updateKycWithAiResult(kycId, cleanJson, "AI_COMPLETED");
        }

        else {
          String errorMsg = "AI API Failed. Status: " + response.statusCode() + " Body: " + response.bodyAsString();
          System.err.println( errorMsg);
          JsonObject errorJson = new JsonObject().put("error", errorMsg);
          return updateKycWithAiResult(kycId, errorJson.encode(), "AI_FAILED");
        }
      })
      .doOnError(err -> {
        System.err.println("  Error: " + err.getMessage());
        JsonObject errorJson = new JsonObject().put("error", "Network Error: " + err.getMessage());
        updateKycWithAiResult(kycId, errorJson.encode(), "AI_ERROR").subscribe();
      })
      .subscribe(
        () -> System.out.println("Workflow Finished."),
        err -> System.err.println(" error: " + err.getMessage())
      );
  }

  private Completable updateKycWithAiResult(String kycId, String jsonResult, String status) {
    return kycRepository.findById(kycId)
      .flatMapCompletable((Optional<KycSubmission> optKyc) -> {
        if (optKyc.isPresent()) {
          KycSubmission kyc = optKyc.get();
          kyc.setAiAnalysis(jsonResult);

          if ("AI_FAILED".equals(status) || "AI_ERROR".equals(status)) {
            kyc.setAiStatus("AI_FAILED");
          } else if (jsonResult.contains("MANUAL_REVIEW")) {
            kyc.setAiStatus("AI_FLAGGED");
          } else {
            kyc.setAiStatus("AI_CLEAR");
          }

          return kycRepository.save(kyc).ignoreElement();
        }
        return Completable.complete();
      });
  }
}
