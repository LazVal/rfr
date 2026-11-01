package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbs;
import io.student.rangiffler.model.TestUser;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class AuthUserRepository {
  private final JdbcClient jdbc = Jdbs.jdbcClient(Databases.AUTH);
  private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  public TestUser create(String username, String password) {
    final UUID id = UUID.randomUUID();
    final byte[] userId = toBytes(id);
    final String encodedPassword = passwordEncoder.encode(password);
    Jdbs.inTransaction(Databases.AUTH, () -> {
      jdbc.sql(Jdbs.sql("auth/create-user"))
          .param("id", userId).param("username", username).param("password", encodedPassword).update();
      for (final String authority : new String[]{"read", "write"}) {
        jdbc.sql(Jdbs.sql("auth/create-authority"))
            .param("id", toBytes(UUID.randomUUID())).param("userId", userId).param("authority", authority).update();
      }
    });
    return new TestUser(id, username, password);
  }

  public void delete(UUID id) {
    final byte[] userId = toBytes(id);
    Jdbs.inTransaction(Databases.AUTH, () -> {
      jdbc.sql(Jdbs.sql("auth/delete-authorities")).param("userId", userId).update();
      jdbc.sql(Jdbs.sql("common/delete-user")).param("userId", userId).update();
    });
  }

  public Optional<TestUser> findByUsername(String username) {
    return jdbc.sql(Jdbs.sql("common/find-user-id")).param("username", username)
        .query((result, row) -> {
          final ByteBuffer id = ByteBuffer.wrap(result.getBytes("id"));
          return new TestUser(new UUID(id.getLong(), id.getLong()), username, "");
        }).optional();
  }

  private static byte[] toBytes(UUID id) {
    return ByteBuffer.allocate(16)
        .putLong(id.getMostSignificantBits())
        .putLong(id.getLeastSignificantBits())
        .array();
  }
}
