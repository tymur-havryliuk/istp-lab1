-- USERS
CREATE TABLE users (
                       user_id BIGSERIAL PRIMARY KEY,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT chk_users_role CHECK (role IN ('student', 'teacher'))
);

-- TEACHERS
CREATE TABLE teachers (
                          teacher_id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL UNIQUE,
                          department VARCHAR(100) NOT NULL,
                          CONSTRAINT fk_teachers_user
                              FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- STUDENTS
CREATE TABLE students (
                          student_id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL UNIQUE,
                          group_name VARCHAR(50) NOT NULL,
                          CONSTRAINT fk_students_user
                              FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- COURSES
CREATE TABLE courses (
                         course_id BIGSERIAL PRIMARY KEY,
                         title VARCHAR(150) NOT NULL,
                         description TEXT,
                         teacher_id BIGINT NOT NULL,
                         start_date DATE,
                         end_date DATE,
                         status VARCHAR(30) NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_courses_teacher
                             FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id),

                         CONSTRAINT chk_courses_status
                             CHECK (status IN ('planned', 'active', 'completed', 'cancelled')),

                         CONSTRAINT chk_courses_dates
                             CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);

-- ASSIGNMENTS
CREATE TABLE assignments (
                             assignment_id BIGSERIAL PRIMARY KEY,
                             course_id BIGINT NOT NULL,
                             title VARCHAR(150) NOT NULL,
                             description TEXT,
                             due_date TIMESTAMP NOT NULL,
                             max_score INT NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_assignments_course
                                 FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,

                             CONSTRAINT chk_assignments_max_score
                                 CHECK (max_score > 0)
);

-- ENROLLMENTS (many-to-many)
CREATE TABLE enrollments (
                             enrollment_id BIGSERIAL PRIMARY KEY,
                             student_id BIGINT NOT NULL,
                             course_id BIGINT NOT NULL,
                             enrollment_date DATE NOT NULL DEFAULT CURRENT_DATE,
                             status VARCHAR(30) NOT NULL,

                             CONSTRAINT fk_enrollments_student
                                 FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,

                             CONSTRAINT fk_enrollments_course
                                 FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,

                             CONSTRAINT uq_enrollments UNIQUE (student_id, course_id),

                             CONSTRAINT chk_enrollments_status
                                 CHECK (status IN ('active', 'completed', 'dropped'))
);

-- SUBMISSIONS
CREATE TABLE submissions (
                             submission_id BIGSERIAL PRIMARY KEY,
                             assignment_id BIGINT NOT NULL,
                             student_id BIGINT NOT NULL,
                             submission_date TIMESTAMP NOT NULL,
                             file_url VARCHAR(255),
                             comment TEXT,
                             status VARCHAR(30) NOT NULL,
                             score INT,
                             feedback TEXT,

                             CONSTRAINT fk_submissions_assignment
                                 FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,

                             CONSTRAINT fk_submissions_student
                                 FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,

                             CONSTRAINT uq_submissions_assignment_student UNIQUE (assignment_id, student_id),

                             CONSTRAINT chk_submissions_status
                                 CHECK (status IN ('submitted', 'reviewed', 'rejected'))
);

-- INDEXES
CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_assignments_course_id ON assignments(course_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX idx_submissions_assignment_id ON submissions(assignment_id);
CREATE INDEX idx_submissions_student_id ON submissions(student_id);
CREATE INDEX idx_teachers_user_id ON teachers(user_id);
CREATE INDEX idx_students_user_id ON students(user_id);