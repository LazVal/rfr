package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.CredentialsComponent;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class RegistrationPage {
  public static final String URL = TestConfig.authUrl() + "/register";
  private final Page page;
  private final CredentialsComponent credentials;
  private final Locator confirmPassword;
  private final Locator submitButton;
  private final Locator successMessage;
  private final Locator signInLink;

  public RegistrationPage(Page page) {
    this.page = page;
    this.credentials = new CredentialsComponent(page);
    this.confirmPassword = page.getByPlaceholder("Repeat your password", new Page.GetByPlaceholderOptions().setExact(true));
    this.submitButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up").setExact(true));
    this.successMessage = page.getByText("Congratulations! You've registered!", new Page.GetByTextOptions().setExact(true));
    this.signInLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign in").setExact(true));
  }

  public RegistrationPage fillRegistrationForm(String username, String password) {
    Allure.step("Заполнить форму регистрации: " + username, () -> {
      credentials.fillCredentials(username, password);
      confirmPassword.fill(password);
    });
    return this;
  }

  @Step("Отправить форму регистрации")
  public RegistrationPage register() {
    submitButton.click();
    return this;
  }

  @Step("Проверить успешную регистрацию")
  public RegistrationPage shouldBeSuccessful() {
    assertThat(successMessage).isVisible();
    return this;
  }

  @Step("Вернуться на стартовую страницу для входа")
  public LandingPage proceedToSignIn() {
    signInLink.click();
    return new LandingPage(page);
  }
}
