INSERT INTO photo_like (photo_id, like_id)
SELECT p.id, l.id FROM photo p JOIN `like` l ON p.user_id = l.user_id WHERE p.user_id = :userId;
