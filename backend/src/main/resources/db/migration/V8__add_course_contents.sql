CREATE TABLE course_contents (
    content_id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    position INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_contents_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

CREATE INDEX idx_course_contents_course_id ON course_contents(course_id);

INSERT INTO course_contents (content_id, course_id, title, description, position)
SELECT seed.content_id, seed.course_id, seed.title, seed.description, seed.position
FROM (
    VALUES
        (1, 1, 'Relational Modeling Basics', 'Entities, attributes, primary keys and how to translate a domain into a clean relational model.', 1),
        (2, 1, 'Normalization Workshop', '1NF to 3NF with practical examples, anti-patterns and how to avoid redundant data.', 2),
        (3, 1, 'SQL Retrieval and Joins', 'SELECT, filtering, grouping and combining tables with readable join strategies.', 3),
        (4, 1, 'Indexes and Transactions', 'Why indexes matter, when they hurt, and how transactions protect consistency.', 4),
        (5, 2, 'Java OOP Refresher', 'Classes, inheritance, interfaces and clean object collaboration.', 1),
        (6, 2, 'Collections and Streams', 'List, Set, Map and stream pipelines for practical data processing tasks.', 2),
        (7, 2, 'Spring MVC Foundations', 'Controllers, DTOs, validation and request-response flow in a REST app.', 3),
        (8, 2, 'Persistence with JPA', 'Entity mapping, repositories and common service-layer data patterns.', 4),
        (9, 3, 'Testing Pyramid', 'Where unit, integration and API tests fit and what each one protects.', 1),
        (10, 3, 'JUnit and Mockito', 'Arrange-act-assert structure, mocks, stubs and useful verification patterns.', 2),
        (11, 3, 'Repository and MVC Tests', 'Testing persistence and web layers without losing readability.', 3),
        (12, 3, 'Regression Thinking', 'How to design tests that catch risky behavior, not just happy paths.', 4),
        (13, 4, 'Complexity Fundamentals', 'Big O intuition and how to reason about cost before coding.', 1),
        (14, 4, 'Linear Structures', 'Arrays, linked lists, stacks and queues in real problem solving.', 2),
        (15, 4, 'Trees and Graphs', 'Traversal patterns, shortest paths and when to choose which structure.', 3),
        (16, 4, 'Greedy and Dynamic Programming', 'Two core problem-solving strategies with practical tradeoffs.', 4)
) AS seed(content_id, course_id, title, description, position)
JOIN courses course_entity ON course_entity.course_id = seed.course_id
ON CONFLICT (content_id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('course_contents', 'content_id'), (SELECT max(content_id) FROM course_contents));
