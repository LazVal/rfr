package io.student.rangiffler.test.auth;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.config.UiOptions;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbs;
import io.student.rangiffler.fixture.UserCleanupAssertions;
import io.student.rangiffler.junit.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.utils.RandomDataUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UsePlaywright(UiOptions.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class LoginNegativeTest {
  private final List<TestUser> users = new ArrayList<>();

  @Test
  void existingUserCannotSignInWithWrongPassword(@User TestUser user, Page page) {
    users.add(user);
    page.context().setExtraHTTPHeaders(Map.of("Accept-Language", "en-US"));
    new LandingPage(page).open().openLogin().fillLoginForm(user.username(), user.password() + "wrong")
        .signInExpectingFailure().shouldHaveError("Bad credentials");
  }

  @Test
  void nonexistentUserCannotSignIn(Page page) {
    final String username = RandomDataUtils.randomUsername();
    page.context().setExtraHTTPHeaders(Map.of("Accept-Language", "en-US"));
    step("Проверить отсутствие пользователя в auth: " + username, () ->
        assertEquals(0, Jdbs.jdbcClient(Databases.AUTH).sql(Jdbs.sql("common/count-user-by-username"))
            .param("username", username).query(Integer.class).single()));
    new LandingPage(page).open().openLogin().fillLoginForm(username, RandomDataUtils.randomPassword())
        .signInExpectingFailure().shouldHaveError("Bad credentials");
  }

  @AfterAll
  void usersAreDeletedAfterExtensionCleanup() {
    users.forEach(UserCleanupAssertions::shouldBeDeleted);
  }
}
