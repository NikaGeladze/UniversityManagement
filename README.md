# University Management API

REST API for managing departments, courses, students and enrollments, built with Spring Boot 4, Java 21 and PostgreSQL.

## Requirements

- Java 21
- PostgreSQL with a database named `university_db`

## Run

Database settings default to `jdbc:postgresql://localhost:5432/university_db` with user `postgres` / password `postgres`. Override them with `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`:

```powershell
$env:DB_PASSWORD="your_password"; .\mvnw.cmd spring-boot:run
```

Tables are created automatically on startup. The API runs on http://localhost:8080.

## API docs

Swagger UI: http://localhost:8080/swagger-ui.html

## Endpoints

| Resource    | Base path          | Extras                                                        |
|-------------|--------------------|---------------------------------------------------------------|
| Departments | `/api/departments` | filter by `name`                                              |
| Courses     | `/api/courses`     | `/popular`, `/{id}/students`, filter by `title`, `departmentId` |
| Students    | `/api/students`    | `/by-email`, `/{id}/courses`, `/{id}/profile-image`, `search` |
| Enrollments | `/api/enrollments` | `PUT /{id}/cancel`, filter by `studentId`, `courseId`, `status` |

List endpoints support paging and sorting: `?page=0&size=10&sort=name,asc`.

## Quick test

Create data in this order: department, course, student, enrollment.

```json
POST /api/departments  { "name": "Computer Science" }
POST /api/courses      { "title": "Algorithms", "credits": 6, "capacity": 30, "departmentId": 1 }
POST /api/students     { "firstName": "John", "lastName": "Doe", "email": "john.doe@university.edu" }
POST /api/enrollments  { "studentId": 1, "courseId": 1 }
```

## Business rules

- Duplicate emails, duplicate enrollments and enrolling in a full course return `409`.
- A department with courses, a course with active enrollments, or a student with active enrollments cannot be deleted (`409`).
- A course's capacity cannot be reduced below its number of active enrollments.
- Profile images must be JPEG, PNG or WEBP and at most 2 MB. They are stored in `uploads/`.
