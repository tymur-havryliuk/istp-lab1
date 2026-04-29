ALTER TABLE users DROP CONSTRAINT chk_users_role;
ALTER TABLE courses DROP CONSTRAINT chk_courses_status;
ALTER TABLE enrollments DROP CONSTRAINT chk_enrollments_status;
ALTER TABLE submissions DROP CONSTRAINT chk_submissions_status;

UPDATE users
SET role = UPPER(role);

UPDATE courses
SET status = UPPER(status);

UPDATE enrollments
SET status = UPPER(status);

UPDATE submissions
SET status = UPPER(status);

ALTER TABLE users
    ADD CONSTRAINT chk_users_role
        CHECK (role IN ('STUDENT', 'TEACHER'));

ALTER TABLE courses
    ADD CONSTRAINT chk_courses_status
        CHECK (status IN ('PLANNED', 'ACTIVE', 'COMPLETED', 'CANCELLED'));

ALTER TABLE enrollments
    ADD CONSTRAINT chk_enrollments_status
        CHECK (status IN ('ACTIVE', 'COMPLETED', 'DROPPED'));

ALTER TABLE submissions
    ADD CONSTRAINT chk_submissions_status
        CHECK (status IN ('SUBMITTED', 'REVIEWED', 'REJECTED'));
