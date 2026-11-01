INSERT INTO `user` (id, username, country_id)
SELECT UUID_TO_BIN(UUID()), :username, id FROM country WHERE code = 'ru' LIMIT 1;
