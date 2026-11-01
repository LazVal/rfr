DELETE FROM `like` WHERE id = :likeId
AND NOT EXISTS (SELECT 1 FROM photo_like WHERE like_id = :likeId);
