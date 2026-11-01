package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbs;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class ApiUserRepository {
  private final JdbcClient jdbc = Jdbs.jdbcClient(Databases.API);

  public void deleteByUsername(String username) {
    Jdbs.inTransaction(Databases.API, () -> {
      final var ids = jdbc.sql(Jdbs.sql("api/find-user-for-update"))
          .param("username", username).query(byte[].class).list();
      for (final byte[] id : ids) {
        final var photoLikes = jdbc.sql(Jdbs.sql("api/find-photo-likes"))
            .param("userId", id).query(byte[].class).list();
        jdbc.sql(Jdbs.sql("api/delete-friendships")).param("userId", id).update();
        jdbc.sql(Jdbs.sql("api/delete-photo-likes")).param("userId", id).update();
        jdbc.sql(Jdbs.sql("api/delete-user-likes")).param("userId", id).update();
        for (final byte[] likeId : photoLikes) {
          jdbc.sql(Jdbs.sql("api/delete-orphan-like")).param("likeId", likeId).update();
        }
        jdbc.sql(Jdbs.sql("api/delete-photos")).param("userId", id).update();
        jdbc.sql(Jdbs.sql("api/delete-statistics")).param("userId", id).update();
        jdbc.sql(Jdbs.sql("common/delete-user")).param("userId", id).update();
      }
    });
  }
}
