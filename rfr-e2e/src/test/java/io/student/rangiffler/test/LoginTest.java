package io.student.rangiffler.test;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbs;
import io.student.rangiffler.config.UiOptions;
import io.student.rangiffler.junit.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UsePlaywright(UiOptions.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class LoginTest {
  private final List<TestUser> users = new ArrayList<>();
  private final JdbcClient authJdbc = Jdbs.jdbcClient(Databases.AUTH);
  private final JdbcClient apiJdbc = Jdbs.jdbcClient(Databases.API);

  @Test
  void userCreatedInAuthDatabaseCanSignIn(@User TestUser user, Page page) {
    users.add(user);
    page.setDefaultTimeout(15_000);

    Allure.step("Проверить создание пользователя до входа", () -> {
      assertDatabaseState(user, 1, 2, 0);
      assertEquals(List.of("read", "write"), authJdbc.sql(Jdbs.sql("auth/find-authorities"))
          .param("userId", userId(user)).query(String.class).list());
    });

    new LandingPage(page).open()
        .openLogin()
        .fillLoginForm(user.username(), user.password())
        .signIn()
        .header()
        .shouldBeAuthorized()
        .openProfile()
        .shouldHaveUsername(user.username());

    Allure.step("Проверить создание профиля API при первом входе", () -> {
      assertDatabaseState(user, 1, 2, 1);
    });
  }

  @AfterAll
  @Step("Проверить удаление пользователей и профилей после afterEach extension")
  void usersAndProfilesAreDeletedAfterTests() {
    // @AfterEach выполняется до AfterEachCallback, поэтому проверяем в @AfterAll.
    users.forEach(user -> assertDatabaseState(user, 0, 0, 0));
  }

  private void assertDatabaseState(TestUser user, int authUsers, int authorities, int apiProfiles) {
    assertAll("Состояние БД для " + user.username(),
        () -> assertEquals(authUsers, authJdbc.sql(Jdbs.sql("auth/count-user-by-id-and-username"))
            .param("userId", userId(user)).param("username", user.username()).query(Integer.class).single(), "Пользователь auth"),
        () -> assertEquals(authorities, authJdbc.sql(Jdbs.sql("auth/count-authorities"))
            .param("userId", userId(user)).query(Integer.class).single(), "Права auth"),
        () -> assertEquals(apiProfiles, apiJdbc.sql(Jdbs.sql("common/count-user-by-username"))
            .param("username", user.username()).query(Integer.class).single(), "Профиль API"));
  }

  private static byte[] userId(TestUser user) {
    return ByteBuffer.allocate(16)
        .putLong(user.id().getMostSignificantBits())
        .putLong(user.id().getLeastSignificantBits())
        .array();
  }
}
