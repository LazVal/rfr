package io.student.rangiffler.data.db;

import io.student.rangiffler.config.TestConfig;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public enum Databases {
  AUTH(TestConfig.authDbUrl(), TestConfig.authDbUsername(), TestConfig.authDbPassword()),
  API(TestConfig.apiDbUrl(), TestConfig.apiDbUsername(), TestConfig.apiDbPassword());

  private final DriverManagerDataSource dataSource;
  private final DataSourceTransactionManager transactionManager;

  Databases(String url, String username, String password) {
    this.dataSource = new DriverManagerDataSource(url, username, password);
    this.transactionManager = new DataSourceTransactionManager(dataSource);
  }

  public DriverManagerDataSource dataSource() {
    return dataSource;
  }

  public DataSourceTransactionManager transactionManager() {
    return transactionManager;
  }
}
