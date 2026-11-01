package io.student.rangiffler.fixture;

import io.qameta.allure.Step;
import io.student.rangiffler.data.ApiUserRepository;
import io.student.rangiffler.data.AuthUserRepository;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

import static io.qameta.allure.Allure.step;

public final class UserFixture implements AutoCloseable {
  private final AuthUserRepository authRepository;
  private final ApiUserRepository apiRepository;
  private final TestUser user;

  private UserFixture(AuthUserRepository authRepository, ApiUserRepository apiRepository, TestUser user) {
    this.authRepository = authRepository;
    this.apiRepository = apiRepository;
    this.user = user;
  }

  @Step("Создать пользователя в БД auth")
  public static UserFixture create() {
    final var authRepository = new AuthUserRepository();
    final var apiRepository = new ApiUserRepository();
    final TestUser user = authRepository.create(RandomDataUtils.randomUsername(), RandomDataUtils.randomPassword());
    return new UserFixture(authRepository, apiRepository, user);
  }

  public TestUser user() {
    return user;
  }

  @Step("Очистить пользователя, зарегистрированного через UI: {username}")
  public static void cleanupRegisteredUser(String username) {
    final var authRepository = new AuthUserRepository();
    authRepository.findByUsername(username).ifPresent(user -> {
      new UserFixture(authRepository, new ApiUserRepository(), user).close();
      UserCleanupAssertions.shouldBeDeleted(user);
    });
  }

  @Override
  public void close() {
    RuntimeException failure = null;
    try {
      step("Удалить профиль из БД API: " + user.username(), () -> {
        apiRepository.deleteByUsername(user.username());
      });
    } catch (RuntimeException exception) {
      failure = exception;
    }
    try {
      step("Удалить пользователя из БД auth: " + user.username(), () -> {
        authRepository.delete(user.id());
      });
    } catch (RuntimeException exception) {
      if (failure == null) {
        failure = exception;
      } else {
        failure.addSuppressed(exception);
      }
    }
    if (failure != null) {
      throw failure;
    }
  }
}
