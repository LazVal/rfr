package io.student.rangiffler.test;

import io.student.rangiffler.data.AuthUserRepository;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbs;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class JdbsTest {

  @Test
  void rollsBackChangesAndTracksTransactionForSpecificDatabase() {
    final String username = RandomDataUtils.randomUsername();
    final var failure = new IllegalStateException("Проверка отката транзакции");
    assertFalse(Jdbs.isTransaction(Databases.AUTH));
    assertFalse(Jdbs.isTransaction(Databases.API));

    assertEquals(failure, assertThrows(IllegalStateException.class, () ->
        Jdbs.inTransaction(Databases.AUTH, () -> {
          assertTrue(Jdbs.isTransaction(Databases.AUTH));
          assertFalse(Jdbs.isTransaction(Databases.API));
          new AuthUserRepository().create(username, RandomDataUtils.randomPassword());
          throw failure;
        })));

    assertFalse(Jdbs.isTransaction(Databases.AUTH));
    assertEquals(0, Jdbs.jdbcClient(Databases.AUTH).sql(Jdbs.sql("common/count-user-by-username"))
        .param("username", username).query(Integer.class).single());
  }
}
