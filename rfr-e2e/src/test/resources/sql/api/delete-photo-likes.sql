DELETE pl FROM photo_like pl
JOIN photo p ON p.id = pl.photo_id
JOIN `like` l ON l.id = pl.like_id
WHERE p.user_id = :userId OR l.user_id = :userId;
