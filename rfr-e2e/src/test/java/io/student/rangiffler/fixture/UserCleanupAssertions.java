package io.student.rangiffler.fixture;

import io.qameta.allure.Step;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbs;
import io.student.rangiffler.model.TestUser;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class UserCleanupAssertions {
  private UserCleanupAssertions() {
  }

  @Step("Проверить очистку пользователя и профиля: {user.username}")
  public static void shouldBeDeleted(TestUser user) {
    final byte[] id = ByteBuffer.allocate(16).putLong(user.id().getMostSignificantBits())
        .putLong(user.id().getLeastSignificantBits()).array();
    assertAll("Очистка " + user.username(),
        () -> assertEquals(0, Jdbs.jdbcClient(Databases.AUTH)
            .sql(Jdbs.sql("common/count-user-by-username")).param("username", user.username()).query(Integer.class).single()),
        () -> assertEquals(0, Jdbs.jdbcClient(Databases.AUTH)
            .sql(Jdbs.sql("auth/count-authorities")).param("userId", id).query(Integer.class).single()),
        () -> assertEquals(0, Jdbs.jdbcClient(Databases.API)
            .sql(Jdbs.sql("common/count-user-by-username")).param("username", user.username()).query(Integer.class).single()));
  }
}
