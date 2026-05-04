ALTER TABLE submissions
    ADD CONSTRAINT chk_submissions_score_range
        CHECK (score IS NULL OR (score >= 0 AND score <= 100));
