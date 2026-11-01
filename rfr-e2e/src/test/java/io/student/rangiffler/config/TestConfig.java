package io.student.rangiffler.config;

import java.util.Locale;

public final class TestConfig {
  private static final String DEFAULT_BASE_URL = "http://localhost:3001";
  private static final String DEFAULT_AUTH_URL = "http://localhost:9001";
  private static final String DEFAULT_BROWSER_CHANNEL = "";
  private static final String DEFAULT_AUTH_DB_URL =
      "jdbc:mysql://localhost:3306/rangiffler-auth?serverTimezone=UTC";
  private static final String DEFAULT_AUTH_DB_USERNAME = "root";
  private static final String DEFAULT_AUTH_DB_PASSWORD = "secret";
  private static final String DEFAULT_API_DB_URL =
      "jdbc:mysql://localhost:3306/rangiffler-api?serverTimezone=UTC";
  private static final String DEFAULT_API_DB_USERNAME = "root";
  private static final String DEFAULT_API_DB_PASSWORD = "secret";

  private TestConfig() {
  }

  public static String baseUrl() {
    return value("base.url", DEFAULT_BASE_URL);
  }

  public static String browserChannel() {
    return value("browser.channel", DEFAULT_BROWSER_CHANNEL);
  }

  public static String authUrl() {
    return value("auth.url", DEFAULT_AUTH_URL);
  }

  public static String authDbUrl() {
    return value("auth.db.url", DEFAULT_AUTH_DB_URL);
  }

  public static String authDbUsername() {
    return value("auth.db.username", DEFAULT_AUTH_DB_USERNAME);
  }

  public static String authDbPassword() {
    return value("auth.db.password", DEFAULT_AUTH_DB_PASSWORD);
  }

  public static String apiDbUrl() {
    return value("api.db.url", DEFAULT_API_DB_URL);
  }

  public static String apiDbUsername() {
    return value("api.db.username", DEFAULT_API_DB_USERNAME);
  }

  public static String apiDbPassword() {
    return value("api.db.password", DEFAULT_API_DB_PASSWORD);
  }

  private static String value(String key, String fallback) {
    final String property = System.getProperty("rfr." + key);
    if (property != null && !property.isBlank()) {
      return property;
    }
    final String environment = System.getenv("RFR_" + key.toUpperCase(Locale.ROOT).replace('.', '_'));
    return environment != null && !environment.isBlank() ? environment : fallback;
  }
}
