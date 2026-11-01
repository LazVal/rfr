package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

public final class LandingPage {
  public static final String URL = TestConfig.baseUrl() + "/";
  private final Page page;
  private final Locator registerLink;
  private final Locator loginButton;

  public LandingPage(Page page) {
    this.page = page;
    this.registerLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Register").setExact(true));
    this.loginButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login").setExact(true));
  }

  @Step("Открыть стартовую страницу")
  public LandingPage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Перейти к регистрации")
  public RegistrationPage openRegistration() {
    registerLink.click();
    return new RegistrationPage(page);
  }

  @Step("Перейти ко входу")
  public LoginPage openLogin() {
    loginButton.click();
    return new LoginPage(page);
  }
}
