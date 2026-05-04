UPDATE users
SET email = 'gateway.teacher.legacy@example.com'
WHERE user_id = 1001
  AND email = 'teacher@example.com';

UPDATE users
SET email = 'gateway.student.legacy@example.com'
WHERE user_id = 1002
  AND email = 'student@example.com';

UPDATE users
SET email = 'teacher@example.com',
    password_hash = '$2a$10$96pfR4RqB/5UbTemv2NUN.Bfo8yKmLvyXtOJeOcc2Rl45yeXa.6vG'
WHERE user_id = 1;

UPDATE users
SET email = 'student@example.com',
    password_hash = '$2a$10$96pfR4RqB/5UbTemv2NUN.Bfo8yKmLvyXtOJeOcc2Rl45yeXa.6vG'
WHERE user_id = 3;
