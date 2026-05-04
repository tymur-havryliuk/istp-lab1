

# API Contract

## 1. Загальна інформація

API призначене для взаємодії frontend, API Gateway та backend частин системи дистанційного навчання.

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

Frontend ходить тільки в API Gateway. Gateway виконує login, перевіряє JWT, визначає поточного користувача і прокидує в backend trusted headers:

```http
X-User-Id: <userId>
X-User-Email: <email>
X-User-Role: <role>
```

Для захищених запитів frontend передає JWT-токен у заголовку:

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

## 4. Courses API

Курси є основною сутністю системи. Студент може переглядати доступні курси та записуватись на них. Викладач може створювати й редагувати курси.

Публічними лишаються тільки read endpoints. Для current-user та teacher/student actions backend бере user context з trusted headers, які прокидає Gateway.

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
  "description": "Базовий курс з програмування"
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
POST /api/v1/courses/{courseId}/enroll
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
GET /api/v1/courses/enrolled
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
GET /api/v1/courses/owned
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

Gateway вимагає `STUDENT` або `TEACHER` роль залежно від маршруту. Backend додатково перевіряє ownership/enrollment. `fileId` зберігається як `submissions.file_id` і посилається на `files.file_id`.

---

### 6.1. Submit assignment

Надсилання виконаного завдання студентом.

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

### 6.2. Get submitted submissions

Отримання списку робіт, надісланих студентом.

```http
GET /api/v1/submissions/submitted
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

`POST /api/v1/submissions/{submissionId}/grade` доступний тільки для `TEACHER`, `GET /api/v1/grades/student` — тільки для `STUDENT`. Gateway робить coarse role check, backend перевіряє ownership.

---

### 7.1. Grade submission

Виставлення оцінки та відгуку за роботу студента.

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

### 7.2. Get student grades

Отримання оцінок конкретного студента.

```http
GET /api/v1/grades/student
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

`POST /api/v1/files` доступний тільки для `STUDENT`, `DELETE /api/v1/files/{fileId}` доступний тільки для `STUDENT`, `GET /api/v1/files/{fileId}` лишається public. Metadata зберігається в таблиці `files`, bytes зберігаються на local disk.

---

### 8.1. Upload file

Завантаження файлу на сервер.

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

```http
GET /api/v1/files/{fileId}
```

### Response `200 OK`

```text
Binary file content
```

---

### 8.3. Delete uploaded file

Видалення раніше завантаженого файлу, якщо він ще не був використаний у submission.

```http
DELETE /api/v1/files/{fileId}
```

### Behavior

- видалити може тільки той `STUDENT`, який завантажив файл;
- якщо файл вже прив'язаний до submission, повертається `400 Bad Request`.

### Response `200 OK`

```json
{
  "message": "File deleted successfully"
}
```

---

## 9. Reports API

Reports API використовується викладачем для Excel export/import по оцінках.

`GET /api/v1/reports/grades/export` доступний тільки для `TEACHER` і повертає `.xlsx` файл. `POST /api/v1/reports/grades/import` доступний тільки для `TEACHER` і повертає summary по завантаженому `.xlsx`.

---

### 9.1. Export grades report

```http
GET /api/v1/reports/grades/export
```

### Response `200 OK`

```text
Binary .xlsx file
```

---

### 9.2. Import grades report

```http
POST /api/v1/reports/grades/import
Content-Type: multipart/form-data
```

### Request

```text
file: grades-report.xlsx
```

### Response `200 OK`

```json
{
  "totalRows": 12,
  "averageGrade": 87.5,
  "minGrade": 65,
  "maxGrade": 98,
  "courseCount": 3
}
```

---

## 10. Statistics API

Statistics API використовується для побудови діаграми середнього балу по курсах.

```http
GET /api/v1/statistics/courses/average-grades
```

### Response `200 OK`

```json
[
  {
    "courseId": 1,
    "courseTitle": "Основи програмування",
    "averageGrade": 87.5
  }
]
```

---

## 11. DTO Models

### 11.1. UserDto

```json
{
  "id": 1,
  "fullName": "Іван Петренко",
  "email": "student@example.com",
  "role": "STUDENT"
}
```

---

### 11.2. CourseDto

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

### 11.3. AssignmentDto

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

### 11.4. SubmissionDto

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

### 11.5. GradeDto

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

### 11.6. FileDto

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

## 12. Error Response

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

## 13. Access Rules

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
| Перегляд власних оцінок | + | - |
| Перегляд робіт по завданню | - | + |
| Перегляд конкретної роботи | + | + |
| Виставлення оцінки | - | + |
| Завантаження файлу | + | - |
| Скачування файлу | + | + |
| Export grades report | - | + |
| Import grades report | - | + |
| Перегляд статистики | + | + |

---

## 14. Notes

- Frontend працює тільки через API Gateway на `http://localhost:8080`.
- Frontend не передає `studentId`, `teacherId` або `X-User-*` headers.
- Gateway виконує login, JWT validation і coarse route-role checks.
- Backend лишається source of truth для ownership та business access checks.
- Студент може записатися на курс тільки один раз.
- `POST /api/v1/assignments/{assignmentId}/submissions` повертає `403`, якщо студент не записаний на курс цього assignment.
- Reports import не змінює БД у цьому кроці; endpoint повертає analysis summary для завантаженого `.xlsx`.
- Statistics endpoint рахує середній бал по курсах на основі graded submissions.

- Auth/JWT і role checks поки не реалізовані для public endpoints.
- Студент може записатися на курс тільки один раз.
- Завдання завжди належить конкретному курсу.
- Submission завжди належить конкретному студенту та конкретному assignment.
- Оцінка виставляється тільки після створення submission.
