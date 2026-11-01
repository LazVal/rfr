INSERT INTO `like` (id, user_id, created_date)
VALUES (UUID_TO_BIN(UUID()), :userId, NOW());
