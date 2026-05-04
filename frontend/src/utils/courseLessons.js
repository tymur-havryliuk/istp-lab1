const COURSE_LESSONS = {
  1: [
    {
      title: 'Relational Modeling Basics',
      summary: 'Entities, attributes, primary keys and how to translate a domain into a clean relational model.',
      scheduledAt: '2026-02-10T09:00:00',
      room: 'Room 201'
    },
    {
      title: 'Normalization Workshop',
      summary: '1NF to 3NF with practical examples, anti-patterns and how to avoid redundant data.',
      scheduledAt: '2026-02-12T11:00:00',
      meetingLink: 'https://meet.google.com/dbs-normalization'
    },
    {
      title: 'SQL Retrieval and Joins',
      summary: 'SELECT, filtering, grouping and combining tables with readable join strategies.',
      scheduledAt: '2026-02-17T09:00:00',
      room: 'Room 201'
    },
    {
      title: 'Indexes and Transactions',
      summary: 'Why indexes matter, when they hurt, and how transactions protect consistency.',
      scheduledAt: '2026-02-19T11:00:00',
      meetingLink: 'https://meet.google.com/dbs-transactions'
    }
  ],
  2: [
    {
      title: 'Java OOP Refresher',
      summary: 'Classes, inheritance, interfaces and clean object collaboration.',
      scheduledAt: '2026-02-11T10:00:00',
      room: 'Lab A-12'
    },
    {
      title: 'Collections and Streams',
      summary: 'List, Set, Map and stream pipelines for practical data processing tasks.',
      scheduledAt: '2026-02-13T12:00:00',
      meetingLink: 'https://meet.google.com/java-streams'
    },
    {
      title: 'Spring MVC Foundations',
      summary: 'Controllers, DTOs, validation and request-response flow in a REST app.',
      scheduledAt: '2026-02-18T10:00:00',
      room: 'Lab A-12'
    },
    {
      title: 'Persistence with JPA',
      summary: 'Entity mapping, repositories and common service-layer data patterns.',
      scheduledAt: '2026-02-20T12:00:00',
      meetingLink: 'https://meet.google.com/java-jpa'
    }
  ],
  3: [
    {
      title: 'Testing Pyramid',
      summary: 'Where unit, integration and API tests fit and what each one protects.',
      scheduledAt: '2026-02-09T13:00:00',
      room: 'Room 305'
    },
    {
      title: 'JUnit and Mockito',
      summary: 'Arrange-act-assert structure, mocks, stubs and useful verification patterns.',
      scheduledAt: '2026-02-11T15:00:00',
      meetingLink: 'https://meet.google.com/testing-mockito'
    },
    {
      title: 'Repository and MVC Tests',
      summary: 'Testing persistence and web layers without losing readability.',
      scheduledAt: '2026-02-16T13:00:00',
      room: 'Room 305'
    },
    {
      title: 'Regression Thinking',
      summary: 'How to design tests that catch risky behavior, not just happy paths.',
      scheduledAt: '2026-02-18T15:00:00',
      meetingLink: 'https://meet.google.com/testing-regression'
    }
  ],
  4: [
    {
      title: 'Complexity Fundamentals',
      summary: 'Big O intuition and how to reason about cost before coding.',
      scheduledAt: '2026-02-10T14:00:00',
      room: 'Room 118'
    },
    {
      title: 'Linear Structures',
      summary: 'Arrays, linked lists, stacks and queues in real problem solving.',
      scheduledAt: '2026-02-12T16:00:00',
      meetingLink: 'https://meet.google.com/algo-linear'
    },
    {
      title: 'Trees and Graphs',
      summary: 'Traversal patterns, shortest paths and when to choose which structure.',
      scheduledAt: '2026-02-17T14:00:00',
      room: 'Room 118'
    },
    {
      title: 'Greedy and Dynamic Programming',
      summary: 'Two core problem-solving strategies with practical tradeoffs.',
      scheduledAt: '2026-02-19T16:00:00',
      meetingLink: 'https://meet.google.com/algo-dp'
    }
  ]
}

export function getCourseLessons(course, assignments = []) {
  if (!course) {
    return []
  }

  const predefinedLessons = COURSE_LESSONS[course.id]
  if (predefinedLessons?.length) {
    return predefinedLessons
  }

  return assignments.slice(0, 4).map((assignment, index) => ({
    title: `Lesson ${index + 1}: ${assignment.title}`,
    summary: assignment.description || 'This lesson outline is based on the first assignment in the course.'
  }))
}
