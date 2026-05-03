UPDATE submissions
SET file_id = CAST(file_url AS BIGINT)
WHERE file_id IS NULL
  AND file_url ~ '^[0-9]+$'
  AND EXISTS (
      SELECT 1
      FROM files
      WHERE files.file_id = CAST(submissions.file_url AS BIGINT)
  );

INSERT INTO files (file_name, content_type, size, storage_path, created_at)
SELECT CASE
           WHEN file_url IS NOT NULL AND POSITION('/' IN file_url) > 0 THEN REGEXP_REPLACE(file_url, '^.*/', '')
           WHEN file_url IS NOT NULL AND file_url <> '' THEN 'legacy-file-' || file_url || '.bin'
           ELSE 'legacy-submission-' || submission_id || '.bin'
       END,
       'application/octet-stream',
       0,
       'legacy-submission-' || submission_id,
       submission_date
FROM submissions
WHERE file_id IS NULL;

UPDATE submissions
SET file_id = files.file_id
FROM files
WHERE submissions.file_id IS NULL
  AND files.storage_path = 'legacy-submission-' || submissions.submission_id;

UPDATE submissions
SET graded_at = submission_date
WHERE score IS NOT NULL
  AND graded_at IS NULL;

ALTER TABLE submissions
    ALTER COLUMN file_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_submissions_file_id ON submissions(file_id);

ALTER TABLE submissions
    ADD CONSTRAINT chk_submissions_score_non_negative
        CHECK (score IS NULL OR score >= 0),
    ADD CONSTRAINT chk_submissions_score_and_graded_at
        CHECK (
            (score IS NULL AND graded_at IS NULL)
                OR (score IS NOT NULL AND graded_at IS NOT NULL)
        );

ALTER TABLE submissions
    DROP COLUMN file_url;
