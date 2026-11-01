package io.student.rangiffler.junit.extension;

import io.student.rangiffler.fixture.UserFixture;
import io.student.rangiffler.junit.annotation.User;
import io.student.rangiffler.model.TestUser;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class UserExtension implements BeforeEachCallback, ParameterResolver, AfterEachCallback {
  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(UserExtension.class);

  @Override
  public void beforeEach(ExtensionContext context) {
    final Parameter[] parameters = context.getRequiredTestMethod().getParameters();
    for (final Parameter parameter : parameters) {
      if (parameter.isAnnotationPresent(User.class) && parameter.getType() != TestUser.class) {
        throw new ExtensionConfigurationException("@User поддерживает только параметры TestUser");
      }
    }

    final var state = new UserState(new LinkedHashMap<>());
    context.getStore(NAMESPACE).put(UserState.class, state);
    for (int index = 0; index < parameters.length; index++) {
      if (parameters[index].isAnnotationPresent(User.class)) {
        state.fixtures().put(index, UserFixture.create());
      }
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context) {
    return parameterContext.isAnnotated(User.class)
        && parameterContext.getParameter().getType() == TestUser.class;
  }

  @Override
  public TestUser resolveParameter(ParameterContext parameterContext, ExtensionContext context) {
    final UserState state = context.getStore(NAMESPACE).get(UserState.class, UserState.class);
    if (!parameterContext.getDeclaringExecutable().equals(context.getRequiredTestMethod())
        || state == null || !state.fixtures().containsKey(parameterContext.getIndex())) {
      throw new ParameterResolutionException(
          "Пользователь @User должен быть создан в beforeEach для параметра метода теста");
    }
    return state.fixtures().get(parameterContext.getIndex()).user();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    final UserState state = context.getStore(NAMESPACE).remove(UserState.class, UserState.class);
    if (state == null) {
      return;
    }

    RuntimeException cleanupFailure = null;
    for (final UserFixture fixture : state.fixtures().values()) {
      try {
        fixture.close();
      } catch (RuntimeException exception) {
        if (cleanupFailure == null) {
          cleanupFailure = exception;
        } else {
          cleanupFailure.addSuppressed(exception);
        }
      }
    }
    if (cleanupFailure != null) {
      throw cleanupFailure;
    }
  }

  private record UserState(Map<Integer, UserFixture> fixtures) {
  }
}
