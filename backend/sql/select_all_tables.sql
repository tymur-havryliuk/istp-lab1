SELECT entity_type, entity_id, data
FROM (
    SELECT 'users' AS entity_type, user_id AS entity_id, row_to_json(u) AS data
    FROM users u

    UNION ALL

    SELECT 'teachers' AS entity_type, teacher_id AS entity_id, row_to_json(t) AS data
    FROM teachers t

    UNION ALL

    SELECT 'students' AS entity_type, student_id AS entity_id, row_to_json(s) AS data
    FROM students s

    UNION ALL

    SELECT 'courses' AS entity_type, course_id AS entity_id, row_to_json(c) AS data
    FROM courses c

    UNION ALL

    SELECT 'assignments' AS entity_type, assignment_id AS entity_id, row_to_json(a) AS data
    FROM assignments a

    UNION ALL

    SELECT 'enrollments' AS entity_type, enrollment_id AS entity_id, row_to_json(e) AS data
    FROM enrollments e

    UNION ALL

    SELECT 'submissions' AS entity_type, submission_id AS entity_id, row_to_json(su) AS data
    FROM submissions su
) AS all_data
ORDER BY entity_type, entity_id;
