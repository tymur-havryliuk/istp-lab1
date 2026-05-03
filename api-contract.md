

# API Contract

## 1. Загальна інформація

API призначене для взаємодії frontend та backend частин системи дистанційного навчання.

Система підтримує дві основні ролі користувачів:

- `STUDENT` — студент
- `TEACHER` — викладач

Базовий URL API:

```text
/api/v1
```

Формат обміну даними:

```text
JSON
```

Авторизація виконується через JWT-токен, який frontend передає у заголовку кожного захищеного запиту.

```http
Authorization: Bearer <token>
```

---

## 2. Стандартні HTTP-коди

| Код | Значення |
|---|---|
| `200 OK` | Запит виконано успішно |
| `201 Created` | Ресурс створено |
| `400 Bad Request` | Некоректні дані запиту |
| `401 Unauthorized` | Користувач не авторизований |
| `403 Forbidden` | Немає прав доступу |
| `404 Not Found` | Ресурс не знайдено |
| `500 Internal Server Error` | Внутрішня помилка сервера |

---

## 3. Auth API

### 3.1. Login

Авторизація користувача в системі.

```http
POST /api/v1/auth/login
```

### Request

```json
{
  "email": "student@example.com",
  "password": "password123"
}
```

### Response `200 OK`

```json
{
  "token": "jwt-token",
  "user": {
    "id": 1,
    "fullName": "Іван Петренко",
    "email": "student@example.com",
    "role": "STUDENT"
  }
}
```

---

### 3.2. Register

Реєстрація нового користувача.

```http
POST /api/v1/auth/register
```

### Request

```json
{
  "fullName": "Іван Петренко",
  "email": "student@example.com",
  "password": "password123",
  "role": "STUDENT"
}
```

### Response `201 Created`

```json
{
  "id": 1,
  "fullName": "Іван Петренко",
  "email": "student@example.com",
  "role": "STUDENT"
}
```

---

### 3.3. Get current user

Отримання інформації про поточного авторизованого користувача.

```http
GET /api/v1/auth/me
```

### Response `200 OK`

```json
{
  "id": 1,
  "fullName": "Іван Петренко",
  "email": "student@example.com",
  "role": "STUDENT"
}
```

---

## 4. Courses API

Курси є основною сутністю системи. Студент може переглядати доступні курси та записуватись на них. Викладач може створювати й редагувати курси.

На цьому етапі всі Courses endpoints є публічними. JWT/Auth буде додано окремо; `X-User-Id` не використовується.

---

### 4.1. Get all available courses

Отримання списку всіх доступних курсів.

Фільтр `statuses` приймає список статусів через кому. Якщо параметр не передано, backend використовує `ACTIVE,PLANNED`.

```http
GET /api/v1/courses?statuses=ACTIVE,PLANNED
```

### Response `200 OK`

```json
[
  {
    "id": 1,
    "title": "Основи програмування",
    "description": "Базовий курс з програмування",
    "teacherId": 2,
    "teacherName": "Олена Іваненко",
    "status": "ACTIVE"
  }
]
```

---

### 4.2. Get course by id

Отримання детальної інформації про курс.

```http
GET /api/v1/courses/{courseId}
```

### Response `200 OK`

```json
{
  "id": 1,
  "title": "Основи програмування",
  "description": "Базовий курс з програмування",
  "teacherId": 2,
  "teacherName": "Олена Іваненко",
  "status": "ACTIVE"
}
```

---

### 4.3. Create course

Створення нового курсу.

```http
POST /api/v1/courses
```

### Request

```json
{
  "title": "Основи програмування",
  "description": "Базовий курс з програмування",
  "teacherId": 2
}
```

### Response `201 Created`

```json
{
  "id": 1,
  "title": "Основи програмування",
  "description": "Базовий курс з програмування",
  "teacherId": 2,
  "teacherName": "Олена Іваненко",
  "status": "ACTIVE"
}
```

---

### 4.4. Update course

Редагування інформації про курс.

```http
PUT /api/v1/courses/{courseId}
```

### Request

```json
{
  "title": "Основи програмування",
  "description": "Оновлений опис курсу"
}
```

### Response `200 OK`

```json
{
  "id": 1,
  "title": "Основи програмування",
  "description": "Оновлений опис курсу",
  "teacherId": 2,
  "teacherName": "Олена Іваненко",
  "status": "ACTIVE"
}
```

---

### 4.5. Delete course

Видалення курсу.

```http
DELETE /api/v1/courses/{courseId}
```

### Response `200 OK`

```json
{
  "message": "Course deleted successfully"
}
```

---

### 4.6. Enroll in course

Запис студента на курс.

```http
POST /api/v1/courses/{courseId}/enroll?studentId=1
```

### Response `200 OK`

```json
{
  "courseId": 1,
  "studentId": 1,
  "status": "ENROLLED"
}
```

---

### 4.7. Get enrolled courses

Отримання курсів, на які записаний студент.

```http
GET /api/v1/courses/enrolled?studentId=1
```

### Response `200 OK`

```json
[
  {
    "id": 1,
    "title": "Основи програмування",
    "description": "Базовий курс з програмування",
    "teacherId": 2,
    "teacherName": "Олена Іваненко",
    "status": "ACTIVE"
  }
]
```

---

### 4.8. Get owned courses

Отримання курсів, які створив викладач.

```http
GET /api/v1/courses/owned?teacherId=2
```

### Response `200 OK`

```json
[
  {
    "id": 1,
    "title": "Основи програмування",
    "description": "Базовий курс з програмування",
    "teacherId": 2,
    "teacherName": "Олена Іваненко",
    "status": "ACTIVE"
  }
]
```

---

### 4.9. Get course students

Отримання списку студентів, записаних на курс.

```http
GET /api/v1/courses/{courseId}/students
```

### Response `200 OK`

```json
[
  {
    "id": 1,
    "fullName": "Іван Петренко",
    "email": "student@example.com",
    "role": "STUDENT"
  }
]
```

---

## 5. Assignments API

Завдання належать до конкретного курсу. Викладач створює завдання, студент переглядає їх і надсилає виконану роботу.

На цьому етапі всі Assignments endpoints є публічними. `max_score` є внутрішнім полем БД і при створенні завдання встановлюється в `100`.

---

### 5.1. Get course assignments

Отримання списку завдань конкретного курсу.

```http
GET /api/v1/courses/{courseId}/assignments
```

### Response `200 OK`

```json
[
  {
    "id": 1,
    "courseId": 1,
    "title": "Лабораторна робота №1",
    "description": "Створити UML-діаграму варіантів використання",
    "deadline": "2026-05-01T23:59:00"
  }
]
```

---

### 5.2. Get assignment by id

Отримання детальної інформації про завдання.

```http
GET /api/v1/assignments/{assignmentId}
```

### Response `200 OK`

```json
{
  "id": 1,
  "courseId": 1,
  "title": "Лабораторна робота №1",
  "description": "Створити UML-діаграму варіантів використання",
  "deadline": "2026-05-01T23:59:00"
}
```

---

### 5.3. Create assignment

Створення нового завдання для курсу.

```http
POST /api/v1/courses/{courseId}/assignments
```

### Request

```json
{
  "title": "Лабораторна робота №1",
  "description": "Створити UML-діаграму варіантів використання",
  "deadline": "2026-05-01T23:59:00"
}
```

### Response `201 Created`

```json
{
  "id": 1,
  "courseId": 1,
  "title": "Лабораторна робота №1",
  "description": "Створити UML-діаграму варіантів використання",
  "deadline": "2026-05-01T23:59:00"
}
```

---

### 5.4. Update assignment

Редагування завдання.

```http
PUT /api/v1/assignments/{assignmentId}
```

### Request

```json
{
  "title": "Лабораторна робота №1",
  "description": "Оновлений опис завдання",
  "deadline": "2026-05-03T23:59:00"
}
```

### Response `200 OK`

```json
{
  "id": 1,
  "courseId": 1,
  "title": "Лабораторна робота №1",
  "description": "Оновлений опис завдання",
  "deadline": "2026-05-03T23:59:00"
}
```

---

### 5.5. Delete assignment

Видалення завдання.

```http
DELETE /api/v1/assignments/{assignmentId}
```

### Response `200 OK`

```json
{
  "message": "Assignment deleted successfully"
}
```

---

## 6. Submissions API

Submission — це виконана робота студента по конкретному завданню.

---

### 6.1. Submit assignment

Надсилання виконаного завдання студентом.

Доступно тільки для ролі: `STUDENT`.

```http
POST /api/v1/assignments/{assignmentId}/submissions
```

### Request

```json
{
  "comment": "Надсилаю виконану лабораторну роботу",
  "fileId": 10
}
```

### Response `201 Created`

```json
{
  "id": 1,
  "assignmentId": 1,
  "studentId": 1,
  "studentName": "Іван Петренко",
  "comment": "Надсилаю виконану лабораторну роботу",
  "fileId": 10,
  "submittedAt": "2026-04-20T18:30:00",
  "grade": null,
  "feedback": null
}
```

---

### 6.2. Get my submissions

Отримання списку робіт, надісланих поточним студентом.

Доступно тільки для ролі: `STUDENT`.

```http
GET /api/v1/submissions/my
```

### Response `200 OK`

```json
[
  {
    "id": 1,
    "assignmentId": 1,
    "studentId": 1,
    "studentName": "Іван Петренко",
    "comment": "Надсилаю виконану лабораторну роботу",
    "fileId": 10,
    "submittedAt": "2026-04-20T18:30:00",
    "grade": 95,
    "feedback": "Добре виконано"
  }
]
```

---

### 6.3. Get assignment submissions

Отримання всіх робіт, надісланих по конкретному завданню.

Доступно тільки для ролі: `TEACHER`.

```http
GET /api/v1/assignments/{assignmentId}/submissions
```

### Response `200 OK`

```json
[
  {
    "id": 1,
    "assignmentId": 1,
    "studentId": 1,
    "studentName": "Іван Петренко",
    "comment": "Надсилаю виконану лабораторну роботу",
    "fileId": 10,
    "submittedAt": "2026-04-20T18:30:00",
    "grade": null,
    "feedback": null
  }
]
```

---

### 6.4. Get submission by id

Отримання конкретної роботи.

Доступно для ролей: `STUDENT`, `TEACHER`.

```http
GET /api/v1/submissions/{submissionId}
```

### Response `200 OK`

```json
{
  "id": 1,
  "assignmentId": 1,
  "studentId": 1,
  "studentName": "Іван Петренко",
  "comment": "Надсилаю виконану лабораторну роботу",
  "fileId": 10,
  "submittedAt": "2026-04-20T18:30:00",
  "grade": 95,
  "feedback": "Добре виконано"
}
```

---

## 7. Grades API

Оцінки виставляє викладач. Студент може тільки переглядати свої оцінки.

---

### 7.1. Grade submission

Виставлення оцінки та відгуку за роботу студента.

Доступно тільки для ролі: `TEACHER`.

```http
POST /api/v1/submissions/{submissionId}/grade
```

### Request

```json
{
  "grade": 95,
  "feedback": "Добре виконано, але є незначні помилки в оформленні"
}
```

### Response `200 OK`

```json
{
  "id": 1,
  "assignmentId": 1,
  "studentId": 1,
  "studentName": "Іван Петренко",
  "comment": "Надсилаю виконану лабораторну роботу",
  "fileId": 10,
  "submittedAt": "2026-04-20T18:30:00",
  "grade": 95,
  "feedback": "Добре виконано, але є незначні помилки в оформленні"
}
```

---

### 7.2. Get my grades

Отримання оцінок поточного студента.

Доступно тільки для ролі: `STUDENT`.

```http
GET /api/v1/grades/my
```

### Response `200 OK`

```json
[
  {
    "submissionId": 1,
    "assignmentId": 1,
    "assignmentTitle": "Лабораторна робота №1",
    "courseId": 1,
    "courseTitle": "Основи програмування",
    "grade": 95,
    "feedback": "Добре виконано",
    "gradedAt": "2026-04-21T12:00:00"
  }
]
```

---

## 8. Files API

Files API використовується для завантаження файлів студентами під час здачі завдань.

---

### 8.1. Upload file

Завантаження файлу на сервер.

Доступно тільки для ролі: `STUDENT`.

```http
POST /api/v1/files
Content-Type: multipart/form-data
```

### Request

```text
file: lab1.pdf
```

### Response `201 Created`

```json
{
  "id": 10,
  "fileName": "lab1.pdf",
  "contentType": "application/pdf",
  "size": 245760,
  "url": "/api/v1/files/10"
}
```

---

### 8.2. Download file

Завантаження файлу за його ідентифікатором.

Доступно для ролей: `STUDENT`, `TEACHER`.

```http
GET /api/v1/files/{fileId}
```

### Response `200 OK`

```text
Binary file content
```

---

## 9. DTO Models

### 9.1. UserDto

```json
{
  "id": 1,
  "fullName": "Іван Петренко",
  "email": "student@example.com",
  "role": "STUDENT"
}
```

---

### 9.2. CourseDto

```json
{
  "id": 1,
  "title": "Основи програмування",
  "description": "Базовий курс з програмування",
  "teacherId": 2,
  "teacherName": "Олена Іваненко",
  "status": "ACTIVE"
}
```

---

### 9.3. AssignmentDto

```json
{
  "id": 1,
  "courseId": 1,
  "title": "Лабораторна робота №1",
  "description": "Створити UML-діаграму варіантів використання",
  "deadline": "2026-05-01T23:59:00"
}
```

---

### 9.4. SubmissionDto

```json
{
  "id": 1,
  "assignmentId": 1,
  "studentId": 1,
  "studentName": "Іван Петренко",
  "comment": "Надсилаю виконану лабораторну роботу",
  "fileId": 10,
  "submittedAt": "2026-04-20T18:30:00",
  "grade": 95,
  "feedback": "Добре виконано"
}
```

---

### 9.5. GradeDto

```json
{
  "submissionId": 1,
  "assignmentId": 1,
  "assignmentTitle": "Лабораторна робота №1",
  "courseId": 1,
  "courseTitle": "Основи програмування",
  "grade": 95,
  "feedback": "Добре виконано",
  "gradedAt": "2026-04-21T12:00:00"
}
```

---

### 9.6. FileDto

```json
{
  "id": 10,
  "fileName": "lab1.pdf",
  "contentType": "application/pdf",
  "size": 245760,
  "url": "/api/v1/files/10"
}
```

---

## 10. Error Response

Усі помилки API повертаються в однаковому форматі.

```json
{
  "timestamp": "2026-04-20T18:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Course not found",
  "path": "/api/v1/courses/100"
}
```

---

## 11. Access Rules

| Дія | STUDENT | TEACHER |
|---|---:|---:|
| Перегляд доступних курсів | + | + |
| Перегляд моїх курсів | + | + |
| Запис на курс | + | - |
| Створення курсу | - | + |
| Редагування курсу | - | + |
| Видалення курсу | - | + |
| Перегляд завдань курсу | + | + |
| Створення завдання | - | + |
| Редагування завдання | - | + |
| Надсилання роботи | + | - |
| Перегляд своїх оцінок | + | - |
| Перевірка робіт | - | + |
| Виставлення оцінки | - | + |
| Завантаження файлу | + | - |

---

## 12. Notes

- Студент може переглядати тільки свої submissions та grades.
- Викладач може переглядати submissions тільки для своїх курсів.
- Студент може записатися на курс тільки один раз.
- Завдання завжди належить конкретному курсу.
- Submission завжди належить конкретному студенту та конкретному assignment.
- Оцінка виставляється тільки після створення submission.
