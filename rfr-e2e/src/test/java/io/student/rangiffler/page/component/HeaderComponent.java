package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.page.ProfilePage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class HeaderComponent {
  private final Page page;
  private final Locator root;
  private final Locator logoutButton;
  private final Locator profileLink;

  public HeaderComponent(Page page) {
    this.page = page;
    this.root = page.getByRole(AriaRole.BANNER);
    this.logoutButton = root.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Logout").setExact(true));
    this.profileLink = root.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Profile").setExact(true));
  }

  @Step("Проверить доступность выхода из личного кабинета")
  public HeaderComponent shouldBeAuthorized() {
    assertThat(logoutButton).isVisible();
    return this;
  }

  @Step("Открыть профиль")
  public ProfilePage openProfile() {
    profileLink.click();
    return new ProfilePage(page);
  }
}
