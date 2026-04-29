BEGIN;

TRUNCATE TABLE
    submissions,
    enrollments,
    assignments,
    courses,
    students,
    teachers,
    users
RESTART IDENTITY CASCADE;

COMMIT;
