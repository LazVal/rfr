package io.student.rangiffler.test;

import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbs;
import io.student.rangiffler.fixture.UserFixture;
import io.student.rangiffler.junit.annotation.User;
import io.student.rangiffler.model.TestUser;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public final class UserExtensionTest {

  @Test
  void fixtureSupportsTryWithResources() {
    final TestUser user;
    try (final UserFixture fixture = UserFixture.create()) {
      user = fixture.user();
      assertDatabaseRows(user.id(), 1, 2);
      createApiProfileWithContent(user);
    }

    assertDatabaseRows(user.id(), 0, 0);
    assertApiProfileCount(user.username(), 0);
  }

  @Test
  void removesEveryUserAndTheirAuthoritiesAfterSuccessfulTest() {
    SuccessfulScenario.USERS.clear();
    EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(SuccessfulScenario.class))
        .execute()
        .testEvents().assertStatistics(stats -> stats.started(1).succeeded(1).failed(0));

    assertEquals(2, SuccessfulScenario.USERS.size());
    SuccessfulScenario.USERS.forEach(user -> assertDatabaseRows(user.id(), 0, 0));
    SuccessfulScenario.USERS.forEach(user -> assertApiProfileCount(user.username(), 0));
  }

  @Test
  void removesUserAndAuthoritiesEvenWhenTestFails() {
    FailingScenario.USERS.clear();
    EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(FailingScenario.class))
        .execute()
        .testEvents().assertStatistics(stats -> stats.started(1).succeeded(0).failed(1));

    assertEquals(1, FailingScenario.USERS.size());
    FailingScenario.USERS.forEach(user -> assertDatabaseRows(user.id(), 0, 0));
    FailingScenario.USERS.forEach(user -> assertApiProfileCount(user.username(), 0));
  }

  private static void assertApiProfileCount(String username, int count) {
    assertEquals(count, Jdbs.jdbcClient(Databases.API).sql(Jdbs.sql("common/count-user-by-username"))
        .param("username", username).query(Integer.class).single());
  }

  private static void createApiProfileWithContent(TestUser user) {
    final var jdbc = Jdbs.jdbcClient(Databases.API);
    Jdbs.inTransaction(Databases.API, () -> {
      assertEquals(1, jdbc.sql(Jdbs.sql("fixtures/api/create-profile"))
          .param("username", user.username()).update());
      final byte[] id = jdbc.sql(Jdbs.sql("common/find-user-id"))
          .param("username", user.username()).query(byte[].class).single();
      for (final String sql : List.of("create-photo", "create-like", "create-photo-like", "create-statistic")) {
        jdbc.sql(Jdbs.sql("fixtures/api/" + sql)).param("userId", id).update();
      }
    });
    assertApiProfileCount(user.username(), 1);
  }

  private static void assertDatabaseRows(UUID id, int users, int authorities) {
    final var jdbc = Jdbs.jdbcClient(Databases.AUTH);
    final byte[] userId = ByteBuffer.allocate(16)
        .putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits()).array();
    assertEquals(users, jdbc.sql(Jdbs.sql("common/count-user-by-id"))
        .param("userId", userId).query(Integer.class).single());
    assertEquals(authorities, jdbc.sql(Jdbs.sql("auth/count-authorities"))
        .param("userId", userId).query(Integer.class).single());
  }

  public static final class SuccessfulScenario {
    private static final List<TestUser> USERS = new ArrayList<>();

    @Test
    void receivesTwoDistinctUsers(@User TestUser first, @User TestUser second) {
      USERS.add(first);
      USERS.add(second);
      assertNotEquals(first.id(), second.id());
      assertNotEquals(first.username(), second.username());
      assertDatabaseRows(first.id(), 1, 2);
      assertDatabaseRows(second.id(), 1, 2);
      createApiProfileWithContent(first);
    }
  }

  public static final class FailingScenario {
    private static final List<TestUser> USERS = new ArrayList<>();

    @Test
    void failsAfterReceivingUser(@User TestUser user) {
      USERS.add(user);
      assertDatabaseRows(user.id(), 1, 2);
      createApiProfileWithContent(user);
      throw new AssertionError("Ожидаемое падение для проверки очистки afterEach");
    }
  }
}
