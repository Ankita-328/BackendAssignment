package com.example.starter.config;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;

public class DbConfig {

  public static void setup() {
    DatabaseConfig config = new DatabaseConfig();
    config.loadFromProperties();


    String envPassword = System.getenv("DB_PASSWORD");
    if (envPassword != null && !envPassword.isEmpty()) {
      config.getDataSourceConfig().setPassword(envPassword);
    }

    Database db = DatabaseFactory.create(config);

    config.addClass(com.example.starter.model.KycSubmission.class);

    System.out.println("Database connected successfully ");
  }
}
