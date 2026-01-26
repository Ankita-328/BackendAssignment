

package com.example.starter.service;

import com.example.starter.model.KycSubmission;
import com.example.starter.repository.KycRepository;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import java.util.Base64;
import java.util.Optional;

public class AiService {

  private final WebClient webClient;
  private final KycRepository kycRepository;
  private final Vertx vertx;


    private static final String API_KEY = "sk-or-v1-";
  private static final String AI_MODEL = "x-ai/grok-4.1-fast";

  public AiService(Vertx vertx, KycRepository kycRepository) {
    this.vertx = vertx;
    this.webClient = WebClient.create(vertx);
    this.kycRepository = kycRepository;
  }

  public void analyzeKyc(String kycId, String documentType, String userRole, String filePath) {
   
    vertx.fileSystem().readFile(filePath)
      .map(buffer -> Base64.getEncoder().encodeToString(buffer.getBytes()))
      .flatMap(base64Image -> {

        
        String promptText = "You are a professional KYC API. Analyze the attached image of a " + documentType +
          " for a user with the role '" + userRole + "'.\n" +
          "Verify if the document is authentic, readable, and matches the declared type.\n" +
          "Output ONLY JSON with: 'confidenceScore' (0-100), 'riskFlags' (list), and 'recommendation' (APPROVE/MANUAL_REVIEW).";

        
        JsonArray content = new JsonArray()
          .add(new JsonObject().put("type", "text").put("text", promptText))
          .add(new JsonObject()
            .put("type", "image_url")
            .put("image_url", new JsonObject().put("url", "data:image/jpeg;base64," + base64Image)));

        JsonObject requestBody = new JsonObject()
          .put("model", AI_MODEL)
          .put("messages", new JsonArray().add(new JsonObject().put("role", "user").put("content", content)))
          .put("response_format", new JsonObject().put("type", "json_object"));

      
        return webClient.postAbs("https://openrouter.ai/api/v1/chat/completions")
          .putHeader("Authorization", "Bearer " + API_KEY)
          .putHeader("Content-Type", "application/json")
          .putHeader("HTTP-Referer", "http://localhost:8000")
          .rxSendJsonObject(requestBody);
      })
      .flatMapCompletable(response -> {
        if (response.statusCode() == 200) {
          String rawContent = response.bodyAsJsonObject()
            .getJsonArray("choices").getJsonObject(0).getJsonObject("message").getString("content");

         
          String cleanJson = rawContent.replaceAll("```json", "").replaceAll("```", "").trim();
          if(cleanJson.indexOf("{") > 0) cleanJson = cleanJson.substring(cleanJson.indexOf("{"));
          if(cleanJson.lastIndexOf("}") < cleanJson.length() -1) cleanJson = cleanJson.substring(0, cleanJson.lastIndexOf("}") + 1);

          return updateKycWithAiResult(kycId, cleanJson, "AI_COMPLETED");
        } else {
          String errorMsg = "AI API Failed. Status: " + response.statusCode() + " Body: " + response.bodyAsString();
          return updateKycWithAiResult(kycId, new JsonObject().put("error", errorMsg).encode(), "AI_FAILED");
        }
      })
      .doOnError(err -> {
        System.err.println("Error: " + err.getMessage());
        updateKycWithAiResult(kycId, new JsonObject().put("error", err.getMessage()).encode(), "AI_ERROR").subscribe();
      })
      .subscribe(
        () -> System.out.println("Workflow Finished."),
        err -> System.err.println("Sub error: " + err.getMessage())
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
