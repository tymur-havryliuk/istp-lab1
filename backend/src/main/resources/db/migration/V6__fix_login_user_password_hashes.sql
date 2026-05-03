UPDATE users
SET password_hash = '$2a$10$96pfR4RqB/5UbTemv2NUN.Bfo8yKmLvyXtOJeOcc2Rl45yeXa.6vG'
WHERE email IN ('teacher@example.com', 'student@example.com');
