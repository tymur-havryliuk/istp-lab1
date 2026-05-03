INSERT INTO users (user_id, full_name, email, password_hash, role)
VALUES
    (1001, 'Gateway Teacher', 'teacher@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER'),
    (1002, 'Gateway Student', 'student@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STUDENT')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO teachers (teacher_id, user_id, department)
VALUES (1001, 1001, 'Gateway Demo Department')
ON CONFLICT (teacher_id) DO NOTHING;

INSERT INTO students (student_id, user_id, group_name)
VALUES (1001, 1002, 'GW-01')
ON CONFLICT (student_id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('users', 'user_id'), GREATEST(
        COALESCE((SELECT max(user_id) FROM users), 1),
        1002
));
SELECT setval(pg_get_serial_sequence('teachers', 'teacher_id'), GREATEST(
        COALESCE((SELECT max(teacher_id) FROM teachers), 1),
        1001
));
SELECT setval(pg_get_serial_sequence('students', 'student_id'), GREATEST(
        COALESCE((SELECT max(student_id) FROM students), 1),
        1001
));
