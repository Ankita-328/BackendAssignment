package com.example.starter.config;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;

public class DbConfig {

  public static void setup() {
    DatabaseConfig config = new DatabaseConfig();
    config.loadFromProperties();

    config.setDdlGenerate(true);
    config.setDdlRun(true);

    Database db = DatabaseFactory.create(config);

    System.out.println("Database connected successfully ");
  }
}
