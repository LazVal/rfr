package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.HeaderComponent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class ProfilePage {
  public static final String URL = TestConfig.baseUrl() + "/profile";
  private final Page page;
  private final Locator usernameInput;
  private final HeaderComponent header;
  private final Locator firstNameInput;
  private final Locator surnameInput;
  private final Locator location;
  private final Locator avatarInput;
  private final Locator avatar;
  private final Locator save;
  private final Locator reset;

  public ProfilePage(Page page) {
    this.page = page;
    this.usernameInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username").setExact(true));
    this.header = new HeaderComponent(page);
    final Locator main = page.getByRole(AriaRole.MAIN);
    this.firstNameInput = main.getByRole(AriaRole.TEXTBOX, new Locator.GetByRoleOptions().setName("First Name").setExact(true));
    this.surnameInput = main.getByRole(AriaRole.TEXTBOX, new Locator.GetByRoleOptions().setName("Surname").setExact(true));
    this.location = main.getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName("Location").setExact(true));
    this.avatarInput = main.locator("input[type='file']");
    this.avatar = main.locator(".MuiAvatar-root img[src^='data:image/']");
    this.save = main.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save").setExact(true));
    this.reset = main.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reset").setExact(true));
  }

  @Step("Открыть профиль")
  public ProfilePage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Перезагрузить профиль")
  public ProfilePage reload() {
    page.reload();
    return this;
  }

  @Step("Заполнить имя и фамилию профиля: {firstName} {surname}")
  public ProfilePage fillProfileForm(String firstName, String surname) {
    firstNameInput.fill(firstName);
    surnameInput.fill(surname);
    return this;
  }

  @Step("Выбрать страну проживания: {countryName}")
  public ProfilePage selectLocation(String countryName) {
    location.click();
    page.getByRole(AriaRole.OPTION).filter(new Locator.FilterOptions().setHas(
        page.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName(countryName).setExact(true))))
        .click();
    return this;
  }

  @Step("Загрузить аватар: {file}")
  public ProfilePage uploadAvatar(Path file) {
    avatarInput.setInputFiles(file);
    return this;
  }

  @Step("Сохранить профиль")
  public ProfilePage clickSave() {
    page.waitForResponse(response -> {
      if (!response.url().endsWith("/graphql") || !response.request().method().equals("POST")) {
        return false;
      }
      final String body = response.request().postData();
      return body != null && Pattern.compile("\"operationName\"\\s*:\\s*\"UpdateUser\"").matcher(body).find();
    }, save::click);
    return this;
  }

  @Step("Отменить несохранённые изменения профиля")
  public ProfilePage clickReset() {
    reset.click();
    return this;
  }

  @Step("Проверить данные профиля: {firstName} {surname}, {countryName}")
  public ProfilePage shouldHaveProfile(String firstName, String surname, String countryName) {
    assertThat(firstNameInput).hasValue(firstName);
    assertThat(surnameInput).hasValue(surname);
    assertThat(location).hasText(countryName);
    return this;
  }

  @Step("Проверить аватар из файла: {expectedFile}")
  public ProfilePage shouldHaveAvatar(Path expectedFile) {
    try {
      final String expected = "data:image/png;base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(expectedFile));
      assertThat(avatar).hasAttribute("src", expected);
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
    return this;
  }

  @Step("Проверить логин в профиле: {username}")
  public ProfilePage shouldHaveUsername(String username) {
    assertThat(page).hasURL(URL);
    assertThat(usernameInput).hasValue(username);
    return this;
  }

  public HeaderComponent header() {
    return header;
  }
}
