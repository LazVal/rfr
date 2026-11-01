package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;

public final class CredentialsComponent {
  private final Locator root;
  private final Locator usernameInput;
  private final Locator passwordInput;

  public CredentialsComponent(Page page) {
    this.root = page.locator("form");
    this.usernameInput = root.getByLabel("Username", new Locator.GetByLabelOptions().setExact(true));
    this.passwordInput = root.getByPlaceholder("Type your password", new Locator.GetByPlaceholderOptions().setExact(true));
  }

  public CredentialsComponent fillCredentials(String username, String password) {
    Allure.step("Заполнить учётные данные: " + username, () -> {
      usernameInput.fill(username);
      passwordInput.fill(password);
    });
    return this;
  }
}
