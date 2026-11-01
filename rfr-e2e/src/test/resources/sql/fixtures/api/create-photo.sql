INSERT INTO photo (id, user_id, country_id, created_date)
SELECT UUID_TO_BIN(UUID()), id, country_id, NOW() FROM `user` WHERE id = :userId;
