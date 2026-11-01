package io.student.rangiffler.model;

import java.util.UUID;

public record TestUser(UUID id, String username, String password) {

  @Override
  public String toString() {
    return "TestUser[id=" + id + ", username=" + username + "]";
  }
}
