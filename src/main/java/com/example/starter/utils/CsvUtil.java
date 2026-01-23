package com.example.starter.utils;

import com.example.starter.model.Role;
import io.vertx.core.json.JsonObject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

  public static List<JsonObject> parseCsv(String filePath) throws IOException {
    List<JsonObject> users = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      boolean isHeader = true;

      while ((line = br.readLine()) != null) {
        if (isHeader) {
          isHeader = false;
          continue;
        }

        String[] cols = line.split(",");
        if (cols.length < 5) continue;
        JsonObject user = new JsonObject()
          .put("fullName", cols[0].trim())
          .put("email", cols[1].trim())
          .put("mobileNumber", cols[2].trim())
          .put("role", cols[3].trim().toUpperCase())
          .put("initialPassword", cols[4].trim());

        users.add(user);
      }
    }
    return users;
  }
}
