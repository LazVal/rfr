package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.CredentialsComponent;

import static io.qameta.allure.Allure.step;

public final class LoginPage {
  public static final String URL = TestConfig.authUrl() + "/login";
  private final Page page;
  private final CredentialsComponent credentials;
  private final Locator submitButton;
  private final Locator error;

  public LoginPage(Page page) {
    this.page = page;
    this.credentials = new CredentialsComponent(page);
    this.error = page.locator(".form__error");
    this.submitButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in").setExact(true));
  }

  public LoginPage fillLoginForm(String username, String password) {
    step("Заполнить форму входа: " + username, () -> {
      credentials.fillCredentials(username, password);
    });
    return this;
  }

  @Step("Войти в личный кабинет")
  public PersonalAccountPage signIn() {
    submitButton.click();
    return new PersonalAccountPage(page);
  }

  @Step("Отправить данные входа с ожидаемым отказом")
  public LoginPage signInExpectingFailure() {
    submitButton.click();
    return this;
  }

  @Step("Проверить ошибку входа: {message}")
  public LoginPage shouldHaveError(String message) {
    com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(error).isVisible();
    com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(error).hasText(message);
    return this;
  }
}
