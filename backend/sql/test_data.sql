BEGIN;

TRUNCATE TABLE
    course_contents,
    submissions,
    files,
    enrollments,
    assignments,
    courses,
    students,
    teachers,
    users
RESTART IDENTITY CASCADE;

-- USERS
INSERT INTO users (user_id, full_name, email, password_hash, role, created_at)
VALUES
    (1, 'Olena Shevchenko', 'teacher@example.com', '$2a$10$96pfR4RqB/5UbTemv2NUN.Bfo8yKmLvyXtOJeOcc2Rl45yeXa.6vG', 'TEACHER', '2026-01-10 09:00:00'),
    (2, 'Taras Melnyk', 'taras.melnyk@university.test', '$2a$10$teacher02hash', 'TEACHER', '2026-01-11 10:00:00'),
    (3, 'Andrii Kovalenko', 'student@example.com', '$2a$10$96pfR4RqB/5UbTemv2NUN.Bfo8yKmLvyXtOJeOcc2Rl45yeXa.6vG', 'STUDENT', '2026-01-12 08:30:00'),
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

-- COURSE CONTENT
INSERT INTO course_contents (content_id, course_id, title, description, position, scheduled_at, room, meeting_link, created_at)
VALUES
    (1, 1, 'Relational Modeling Basics', 'Entities, attributes, primary keys and how to translate a domain into a clean relational model.', 1, '2026-02-10 09:00:00', 'Room 201', NULL, '2026-02-01 09:00:00'),
    (2, 1, 'Normalization Workshop', '1NF to 3NF with practical examples, anti-patterns and how to avoid redundant data.', 2, '2026-02-12 11:00:00', NULL, 'https://meet.google.com/dbs-normalization', '2026-02-01 09:05:00'),
    (3, 1, 'SQL Retrieval and Joins', 'SELECT, filtering, grouping and combining tables with readable join strategies.', 3, '2026-02-17 09:00:00', 'Room 201', NULL, '2026-02-01 09:10:00'),
    (4, 1, 'Indexes and Transactions', 'Why indexes matter, when they hurt, and how transactions protect consistency.', 4, '2026-02-19 11:00:00', NULL, 'https://meet.google.com/dbs-transactions', '2026-02-01 09:15:00'),
    (5, 2, 'Java OOP Refresher', 'Classes, inheritance, interfaces and clean object collaboration.', 1, '2026-02-11 10:00:00', 'Lab A-12', NULL, '2026-02-02 10:00:00'),
    (6, 2, 'Collections and Streams', 'List, Set, Map and stream pipelines for practical data processing tasks.', 2, '2026-02-13 12:00:00', NULL, 'https://meet.google.com/java-streams', '2026-02-02 10:05:00'),
    (7, 2, 'Spring MVC Foundations', 'Controllers, DTOs, validation and request-response flow in a REST app.', 3, '2026-02-18 10:00:00', 'Lab A-12', NULL, '2026-02-02 10:10:00'),
    (8, 2, 'Persistence with JPA', 'Entity mapping, repositories and common service-layer data patterns.', 4, '2026-02-20 12:00:00', NULL, 'https://meet.google.com/java-jpa', '2026-02-02 10:15:00'),
    (9, 3, 'Testing Pyramid', 'Where unit, integration and API tests fit and what each one protects.', 1, '2025-09-09 13:00:00', 'Room 305', NULL, '2025-09-01 08:00:00'),
    (10, 3, 'JUnit and Mockito', 'Arrange-act-assert structure, mocks, stubs and useful verification patterns.', 2, '2025-09-11 15:00:00', NULL, 'https://meet.google.com/testing-mockito', '2025-09-01 08:05:00'),
    (11, 3, 'Repository and MVC Tests', 'Testing persistence and web layers without losing readability.', 3, '2025-09-16 13:00:00', 'Room 305', NULL, '2025-09-01 08:10:00'),
    (12, 3, 'Regression Thinking', 'How to design tests that catch risky behavior, not just happy paths.', 4, '2025-09-18 15:00:00', NULL, 'https://meet.google.com/testing-regression', '2025-09-01 08:15:00'),
    (13, 4, 'Complexity Fundamentals', 'Big O intuition and how to reason about cost before coding.', 1, '2026-09-10 14:00:00', 'Room 118', NULL, '2026-03-02 09:00:00'),
    (14, 4, 'Linear Structures', 'Arrays, linked lists, stacks and queues in real problem solving.', 2, '2026-09-12 16:00:00', NULL, 'https://meet.google.com/algo-linear', '2026-03-02 09:05:00'),
    (15, 4, 'Trees and Graphs', 'Traversal patterns, shortest paths and when to choose which structure.', 3, '2026-09-17 14:00:00', 'Room 118', NULL, '2026-03-02 09:10:00'),
    (16, 4, 'Greedy and Dynamic Programming', 'Two core problem-solving strategies with practical tradeoffs.', 4, '2026-09-19 16:00:00', NULL, 'https://meet.google.com/algo-dp', '2026-03-02 09:15:00')
ON CONFLICT (content_id) DO NOTHING;

-- Keep BIGSERIAL sequences in sync after explicit ids above.
SELECT setval(pg_get_serial_sequence('users', 'user_id'), (SELECT max(user_id) FROM users));
SELECT setval(pg_get_serial_sequence('teachers', 'teacher_id'), (SELECT max(teacher_id) FROM teachers));
SELECT setval(pg_get_serial_sequence('students', 'student_id'), (SELECT max(student_id) FROM students));
SELECT setval(pg_get_serial_sequence('courses', 'course_id'), (SELECT max(course_id) FROM courses));
SELECT setval(pg_get_serial_sequence('assignments', 'assignment_id'), (SELECT max(assignment_id) FROM assignments));
SELECT setval(pg_get_serial_sequence('enrollments', 'enrollment_id'), (SELECT max(enrollment_id) FROM enrollments));
SELECT setval(pg_get_serial_sequence('files', 'file_id'), (SELECT max(file_id) FROM files));
SELECT setval(pg_get_serial_sequence('submissions', 'submission_id'), (SELECT max(submission_id) FROM submissions));
SELECT setval(pg_get_serial_sequence('course_contents', 'content_id'), (SELECT max(content_id) FROM course_contents));

COMMIT;
