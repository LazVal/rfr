package io.student.rangiffler.test;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.config.UiOptions;
import io.student.rangiffler.fixture.UserFixture;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;

@UsePlaywright(UiOptions.class)
public final class RegistrationTest {

  @Test
  void registeredUserCanSignInToPersonalAccount(Page page) {
    final String username = RandomDataUtils.randomUsername();
    final String password = RandomDataUtils.randomPassword();
    page.setDefaultTimeout(15_000);

    try {
      step("Зарегистрироваться, войти и проверить личный кабинет", () -> {
      new LandingPage(page).open()
          .openRegistration()
          .fillRegistrationForm(username, password)
          .register()
          .shouldBeSuccessful()
          .proceedToSignIn()
          .openLogin()
          .fillLoginForm(username, password)
          .signIn()
          .header()
          .shouldBeAuthorized()
          .openProfile()
          .shouldHaveUsername(username);
      });
    } finally {
      UserFixture.cleanupRegisteredUser(username);
    }
  }
}
