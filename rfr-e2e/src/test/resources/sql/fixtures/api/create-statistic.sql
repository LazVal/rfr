INSERT INTO statistic (id, user_id, country_id, count)
SELECT UUID_TO_BIN(UUID()), id, country_id, 1 FROM `user` WHERE id = :userId;
