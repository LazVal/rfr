package io.student.rangiffler.page;

import com.microsoft.playwright.Page;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.HeaderComponent;

public final class PersonalAccountPage {
  public static final String URL = TestConfig.baseUrl() + "/my-travels";
  private final Page page;
  private final HeaderComponent header;

  public PersonalAccountPage(Page page) {
    this.page = page;
    this.header = new HeaderComponent(page);
  }

  public HeaderComponent header() {
    return header;
  }
}
