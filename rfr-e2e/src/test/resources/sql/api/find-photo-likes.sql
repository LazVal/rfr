SELECT DISTINCT pl.like_id FROM photo_like pl
JOIN photo p ON p.id = pl.photo_id WHERE p.user_id = :userId;
