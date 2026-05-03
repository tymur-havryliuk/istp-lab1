BEGIN;

-- USERS
INSERT INTO users (user_id, full_name, email, password_hash, role, created_at)
VALUES
    (1, 'Olena Shevchenko', 'olena.shevchenko@university.test', '$2a$10$teacher01hash', 'TEACHER', '2026-01-10 09:00:00'),
    (2, 'Taras Melnyk', 'taras.melnyk@university.test', '$2a$10$teacher02hash', 'TEACHER', '2026-01-11 10:00:00'),
    (3, 'Andrii Kovalenko', 'andrii.kovalenko@student.test', '$2a$10$student01hash', 'STUDENT', '2026-01-12 08:30:00'),
    (4, 'Iryna Bondar', 'iryna.bondar@student.test', '$2a$10$student02hash', 'STUDENT', '2026-01-12 08:45:00'),
    (5, 'Maksym Hnatiuk', 'maksym.hnatiuk@student.test', '$2a$10$student03hash', 'STUDENT', '2026-01-12 09:00:00'),
    (6, 'Sofiia Tkachenko', 'sofiia.tkachenko@student.test', '$2a$10$student04hash', 'STUDENT', '2026-01-12 09:15:00'),
    (7, 'Dmytro Savchuk', 'dmytro.savchuk@student.test', '$2a$10$student05hash', 'STUDENT', '2026-01-12 09:30:00'),
    (8, 'Kateryna Lysenko', 'kateryna.lysenko@student.test', '$2a$10$student06hash', 'STUDENT', '2026-01-12 09:45:00')
ON CONFLICT (user_id) DO NOTHING;

-- TEACHERS
INSERT INTO teachers (teacher_id, user_id, department)
VALUES
    (1, 1, 'Computer Science'),
    (2, 2, 'Software Engineering')
ON CONFLICT (teacher_id) DO NOTHING;

-- STUDENTS
INSERT INTO students (student_id, user_id, group_name)
VALUES
    (1, 3, 'CS-21'),
    (2, 4, 'CS-21'),
    (3, 5, 'SE-22'),
    (4, 6, 'SE-22'),
    (5, 7, 'CS-23'),
    (6, 8, 'CS-23')
ON CONFLICT (student_id) DO NOTHING;

-- COURSES
INSERT INTO courses (course_id, title, description, teacher_id, start_date, end_date, status, created_at)
VALUES
    (1, 'Database Systems', 'Introduction to relational databases, SQL, indexes and transactions.', 1, '2026-02-01', '2026-06-15', 'ACTIVE', '2026-01-20 10:00:00'),
    (2, 'Java Programming', 'Object-oriented programming in Java with Spring basics.', 2, '2026-02-10', '2026-06-20', 'ACTIVE', '2026-01-22 11:00:00'),
    (3, 'Software Testing', 'Testing strategies, unit tests, integration tests and QA process.', 2, '2025-09-01', '2025-12-20', 'COMPLETED', '2025-08-15 12:00:00'),
    (4, 'Algorithms and Data Structures', 'Core algorithms, asymptotic complexity and practical problem solving.', 1, '2026-09-01', '2026-12-20', 'PLANNED', '2026-03-01 09:00:00')
ON CONFLICT (course_id) DO NOTHING;

-- ASSIGNMENTS
INSERT INTO assignments (assignment_id, course_id, title, description, due_date, max_score, created_at)
VALUES
    (1, 1, 'ER Model Design', 'Design an ER diagram for a university information system.', '2026-03-05 23:59:00', 100, '2026-02-15 09:00:00'),
    (2, 1, 'SQL Queries Practice', 'Write SELECT, JOIN, GROUP BY and subquery examples.', '2026-03-20 23:59:00', 100, '2026-02-20 09:00:00'),
    (3, 2, 'Collections API Lab', 'Implement custom processing using List, Set and Map.', '2026-03-10 23:59:00', 100, '2026-02-18 10:00:00'),
    (4, 2, 'Spring REST Service', 'Create a CRUD REST API with validation and DTO mapping.', '2026-04-01 23:59:00', 120, '2026-03-01 10:30:00'),
    (5, 3, 'JUnit Basics', 'Cover service methods with unit tests using JUnit and Mockito.', '2025-10-01 23:59:00', 80, '2025-09-10 08:00:00'),
    (6, 3, 'Integration Testing', 'Test repository and API layers using integration tests.', '2025-11-05 23:59:00', 100, '2025-10-05 08:00:00')
ON CONFLICT (assignment_id) DO NOTHING;

-- ENROLLMENTS
INSERT INTO enrollments (enrollment_id, student_id, course_id, enrollment_date, status)
VALUES
    (1, 1, 1, '2026-02-02', 'ACTIVE'),
    (2, 2, 1, '2026-02-02', 'ACTIVE'),
    (3, 3, 1, '2026-02-03', 'ACTIVE'),
    (4, 2, 2, '2026-02-10', 'ACTIVE'),
    (5, 3, 2, '2026-02-10', 'ACTIVE'),
    (6, 4, 2, '2026-02-11', 'ACTIVE'),
    (7, 1, 3, '2025-09-02', 'COMPLETED'),
    (8, 5, 3, '2025-09-03', 'COMPLETED'),
    (9, 6, 3, '2025-09-03', 'DROPPED'),
    (10, 4, 4, '2026-03-15', 'ACTIVE'),
    (11, 5, 4, '2026-03-15', 'ACTIVE')
ON CONFLICT (enrollment_id) DO NOTHING;

-- FILES
INSERT INTO files (file_id, file_name, content_type, size, storage_path, created_at)
VALUES
    (101, 'er-model-v1.pdf', 'application/pdf', 245760, 'seed-101-er-model-v1.pdf', '2026-03-04 20:15:00'),
    (102, 'er-model-final.pdf', 'application/pdf', 198144, 'seed-102-er-model-final.pdf', '2026-03-05 21:40:00'),
    (103, 'sql-queries.sql', 'text/plain', 12288, 'seed-103-sql-queries.sql', '2026-03-19 19:00:00'),
    (104, 'collections-lab.zip', 'application/zip', 524288, 'seed-104-collections-lab.zip', '2026-03-09 18:30:00'),
    (105, 'collections-bonus.zip', 'application/zip', 655360, 'seed-105-collections-bonus.zip', '2026-03-10 22:10:00'),
    (106, 'spring-rest.zip', 'application/zip', 786432, 'seed-106-spring-rest.zip', '2026-04-01 20:55:00'),
    (107, 'junit-basics.zip', 'application/zip', 327680, 'seed-107-junit-basics.zip', '2025-09-30 17:20:00'),
    (108, 'junit-scenarios.zip', 'application/zip', 344064, 'seed-108-junit-scenarios.zip', '2025-10-01 16:40:00'),
    (109, 'integration-tests.zip', 'application/zip', 458752, 'seed-109-integration-tests.zip', '2025-11-05 22:45:00')
ON CONFLICT (file_id) DO NOTHING;

-- SUBMISSIONS
INSERT INTO submissions (submission_id, assignment_id, student_id, submission_date, file_id, comment, status, score, feedback, graded_at)
VALUES
    (1, 1, 1, '2026-03-04 20:15:00', 101, 'Initial version of ER model.', 'REVIEWED', 92, 'Good structure, minor normalization issues.', '2026-03-05 09:30:00'),
    (2, 1, 2, '2026-03-05 21:40:00', 102, 'Added all core entities.', 'REVIEWED', 97, 'Excellent work.', '2026-03-06 10:00:00'),
    (3, 2, 1, '2026-03-19 19:00:00', 103, 'Queries for all tasks are included.', 'SUBMITTED', NULL, NULL, NULL),
    (4, 3, 2, '2026-03-09 18:30:00', 104, 'Used streams where possible.', 'REVIEWED', 88, 'Solid solution, but add more edge-case handling.', '2026-03-10 10:30:00'),
    (5, 3, 3, '2026-03-10 22:10:00', 105, 'Implemented bonus task too.', 'REVIEWED', 95, 'Clean implementation.', '2026-03-11 11:00:00'),
    (6, 4, 4, '2026-04-01 20:55:00', 106, 'REST service with Swagger docs.', 'SUBMITTED', NULL, NULL, NULL),
    (7, 5, 1, '2025-09-30 17:20:00', 107, 'Tests for service layer.', 'REVIEWED', 76, 'Need stronger assertion coverage.', '2025-10-01 09:30:00'),
    (8, 5, 5, '2025-10-01 16:40:00', 108, 'Covered positive and negative scenarios.', 'REVIEWED', 80, 'Well done.', '2025-10-02 10:00:00'),
    (9, 6, 5, '2025-11-05 22:45:00', 109, 'PostgreSQL and MockMvc integration tests.', 'REJECTED', 40, 'Tests are incomplete and one scenario is failing.', '2025-11-06 10:00:00')
ON CONFLICT (submission_id) DO NOTHING;

-- Keep BIGSERIAL sequences in sync after explicit ids above.
SELECT setval(pg_get_serial_sequence('users', 'user_id'), (SELECT max(user_id) FROM users));
SELECT setval(pg_get_serial_sequence('teachers', 'teacher_id'), (SELECT max(teacher_id) FROM teachers));
SELECT setval(pg_get_serial_sequence('students', 'student_id'), (SELECT max(student_id) FROM students));
SELECT setval(pg_get_serial_sequence('courses', 'course_id'), (SELECT max(course_id) FROM courses));
SELECT setval(pg_get_serial_sequence('assignments', 'assignment_id'), (SELECT max(assignment_id) FROM assignments));
SELECT setval(pg_get_serial_sequence('enrollments', 'enrollment_id'), (SELECT max(enrollment_id) FROM enrollments));
SELECT setval(pg_get_serial_sequence('files', 'file_id'), (SELECT max(file_id) FROM files));
SELECT setval(pg_get_serial_sequence('submissions', 'submission_id'), (SELECT max(submission_id) FROM submissions));

COMMIT;
