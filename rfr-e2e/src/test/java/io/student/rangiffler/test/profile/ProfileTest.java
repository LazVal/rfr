package io.student.rangiffler.test.profile;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.config.UiOptions;
import io.student.rangiffler.fixture.UserCleanupAssertions;
import io.student.rangiffler.junit.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@UsePlaywright(UiOptions.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class ProfileTest {
  private final List<TestUser> users = new ArrayList<>();
  private final Path avatar = resource("avatar.png");
  private final Path anotherAvatar = resource("another-avatar.png");

  @Test
  void savedProfileSurvivesReload(@User TestUser user, Page page) {
    users.add(user);
    new LandingPage(page).open().openLogin().fillLoginForm(user.username(), user.password())
        .signIn().header().openProfile().shouldHaveUsername(user.username())
        .fillProfileForm("Alice", "Smith").selectLocation("France").uploadAvatar(avatar)
        .shouldHaveAvatar(avatar).clickSave().shouldHaveProfile("Alice", "Smith", "France")
        .shouldHaveAvatar(avatar).reload().shouldHaveProfile("Alice", "Smith", "France")
        .shouldHaveAvatar(avatar).shouldHaveUsername(user.username());
  }

  @Test
  void resetRestoresPreviouslySavedProfile(@User TestUser user, Page page) {
    users.add(user);
    new LandingPage(page).open().openLogin().fillLoginForm(user.username(), user.password())
        .signIn().header().openProfile().shouldHaveUsername(user.username())
        .fillProfileForm("Alice", "Smith").selectLocation("France").uploadAvatar(avatar)
        .shouldHaveAvatar(avatar).clickSave().reload()
        .shouldHaveProfile("Alice", "Smith", "France").shouldHaveAvatar(avatar)
        .fillProfileForm("Bob", "Jones").selectLocation("Germany").uploadAvatar(anotherAvatar)
        .shouldHaveProfile("Bob", "Jones", "Germany").shouldHaveAvatar(anotherAvatar)
        .clickReset().shouldHaveProfile("Alice", "Smith", "France").shouldHaveAvatar(avatar)
        .reload().shouldHaveProfile("Alice", "Smith", "France").shouldHaveAvatar(avatar);
  }

  @Test
  void countryCanBeChanged(@User TestUser user, Page page) {
    users.add(user);
    new LandingPage(page).open().openLogin().fillLoginForm(user.username(), user.password())
        .signIn().header().openProfile().shouldHaveUsername(user.username())
        .fillProfileForm("Alice", "Smith").selectLocation("France").clickSave().reload()
        .shouldHaveProfile("Alice", "Smith", "France")
        .selectLocation("Germany").clickSave().reload()
        .shouldHaveProfile("Alice", "Smith", "Germany");
  }

  @AfterAll
  void usersAreDeletedAfterExtensionCleanup() {
    users.forEach(UserCleanupAssertions::shouldBeDeleted);
  }

  private static Path resource(String name) {
    try {
      return Path.of(Objects.requireNonNull(ProfileTest.class.getResource("/images/" + name)).toURI());
    } catch (URISyntaxException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
