CREATE TABLE files (
                       file_id BIGSERIAL PRIMARY KEY,
                       file_name VARCHAR(255) NOT NULL,
                       content_type VARCHAR(100) NOT NULL,
                       size BIGINT NOT NULL,
                       storage_path VARCHAR(255) NOT NULL UNIQUE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT chk_files_size CHECK (size >= 0)
);

ALTER TABLE submissions
    ADD COLUMN file_id BIGINT,
    ADD COLUMN graded_at TIMESTAMP;

ALTER TABLE submissions
    ADD CONSTRAINT fk_submissions_file
        FOREIGN KEY (file_id) REFERENCES files(file_id);

UPDATE submissions
SET file_id = CAST(file_url AS BIGINT)
WHERE file_url ~ '^[0-9]+$'
  AND EXISTS (
      SELECT 1
      FROM files
      WHERE files.file_id = CAST(submissions.file_url AS BIGINT)
  );

UPDATE submissions
SET graded_at = submission_date
WHERE score IS NOT NULL
  AND graded_at IS NULL;
