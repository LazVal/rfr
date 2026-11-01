package io.student.rangiffler.data.db;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

public final class Jdbs {
  private static final Map<String, String> SQL = new ConcurrentHashMap<>();

  private Jdbs() {
  }

  public static JdbcClient jdbcClient(Databases database) {
    return JdbcClient.create(database.dataSource());
  }

  public static void inTransaction(Databases database, Runnable action) {
    new TransactionTemplate(database.transactionManager()).executeWithoutResult(status -> action.run());
  }

  public static boolean isTransaction(Databases database) {
    return TransactionSynchronizationManager.isActualTransactionActive()
        && TransactionSynchronizationManager.hasResource(database.dataSource());
  }

  public static String sql(String name) {
    return SQL.computeIfAbsent(name, key -> {
      final String path = "sql/" + key + ".sql";
      try {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
      } catch (IOException exception) {
        throw new UncheckedIOException("Не удалось загрузить SQL: " + path, exception);
      }
    });
  }
}
