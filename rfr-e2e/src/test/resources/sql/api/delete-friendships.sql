DELETE FROM friendship WHERE requester_id = :userId OR addressee_id = :userId;
