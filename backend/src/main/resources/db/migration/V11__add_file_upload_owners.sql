ALTER TABLE files
    ADD COLUMN uploaded_by_user_id BIGINT;

ALTER TABLE files
    ADD CONSTRAINT fk_files_uploaded_by_user
        FOREIGN KEY (uploaded_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL;

UPDATE files file_record
SET uploaded_by_user_id = student_user.user_id
FROM submissions submission
         JOIN students student ON student.student_id = submission.student_id
         JOIN users student_user ON student_user.user_id = student.user_id
WHERE submission.file_id = file_record.file_id
  AND file_record.uploaded_by_user_id IS NULL;

CREATE INDEX idx_files_uploaded_by_user_id ON files(uploaded_by_user_id);
