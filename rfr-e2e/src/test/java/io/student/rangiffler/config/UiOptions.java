package io.student.rangiffler.config;

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;

public final class UiOptions implements OptionsFactory {

  @Override
  public Options getOptions() {
    var options = new Options()
        .setBaseUrl(TestConfig.baseUrl())
        .setHeadless(true);
    final String browserChannel = TestConfig.browserChannel();
    if (!browserChannel.isBlank()) {
      options.setChannel(browserChannel);
    }
    return options;
  }
}
