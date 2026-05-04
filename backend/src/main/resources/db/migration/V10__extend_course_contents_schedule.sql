ALTER TABLE course_contents
    ADD COLUMN scheduled_at TIMESTAMP,
    ADD COLUMN room VARCHAR(100),
    ADD COLUMN meeting_link VARCHAR(255);

UPDATE course_contents
SET
    scheduled_at = CASE content_id
        WHEN 1 THEN TIMESTAMP '2026-02-10 09:00:00'
        WHEN 2 THEN TIMESTAMP '2026-02-12 11:00:00'
        WHEN 3 THEN TIMESTAMP '2026-02-17 09:00:00'
        WHEN 4 THEN TIMESTAMP '2026-02-19 11:00:00'
        WHEN 5 THEN TIMESTAMP '2026-02-11 10:00:00'
        WHEN 6 THEN TIMESTAMP '2026-02-13 12:00:00'
        WHEN 7 THEN TIMESTAMP '2026-02-18 10:00:00'
        WHEN 8 THEN TIMESTAMP '2026-02-20 12:00:00'
        WHEN 9 THEN TIMESTAMP '2026-02-09 13:00:00'
        WHEN 10 THEN TIMESTAMP '2026-02-11 15:00:00'
        WHEN 11 THEN TIMESTAMP '2026-02-16 13:00:00'
        WHEN 12 THEN TIMESTAMP '2026-02-18 15:00:00'
        WHEN 13 THEN TIMESTAMP '2026-02-10 14:00:00'
        WHEN 14 THEN TIMESTAMP '2026-02-12 16:00:00'
        WHEN 15 THEN TIMESTAMP '2026-02-17 14:00:00'
        WHEN 16 THEN TIMESTAMP '2026-02-19 16:00:00'
        ELSE CURRENT_TIMESTAMP
    END,
    room = CASE content_id
        WHEN 1 THEN 'Room 201'
        WHEN 3 THEN 'Room 201'
        WHEN 5 THEN 'Lab A-12'
        WHEN 7 THEN 'Lab A-12'
        WHEN 9 THEN 'Room 305'
        WHEN 11 THEN 'Room 305'
        WHEN 13 THEN 'Room 118'
        WHEN 15 THEN 'Room 118'
        ELSE NULL
    END,
    meeting_link = CASE content_id
        WHEN 2 THEN 'https://meet.google.com/dbs-normalization'
        WHEN 4 THEN 'https://meet.google.com/dbs-transactions'
        WHEN 6 THEN 'https://meet.google.com/java-streams'
        WHEN 8 THEN 'https://meet.google.com/java-jpa'
        WHEN 10 THEN 'https://meet.google.com/testing-mockito'
        WHEN 12 THEN 'https://meet.google.com/testing-regression'
        WHEN 14 THEN 'https://meet.google.com/algo-linear'
        WHEN 16 THEN 'https://meet.google.com/algo-dp'
        ELSE NULL
    END;

UPDATE course_contents
SET room = 'TBD'
WHERE room IS NULL
  AND meeting_link IS NULL;

ALTER TABLE course_contents
    ALTER COLUMN scheduled_at SET NOT NULL;

ALTER TABLE course_contents
    ADD CONSTRAINT chk_course_contents_delivery_mode
        CHECK (((room IS NOT NULL)::int + (meeting_link IS NOT NULL)::int) = 1);
