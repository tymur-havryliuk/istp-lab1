package com.istp.lab1;

import com.istp.lab1.assignment.dao.repository.AssignmentRepository;
import com.istp.lab1.course.dao.repository.CourseRepository;
import com.istp.lab1.course.dao.repository.EnrollmentRepository;
import com.istp.lab1.file.dao.repository.FileRepository;
import com.istp.lab1.submission.dao.repository.SubmissionRepository;
import com.istp.lab1.user.dao.repository.StudentRepository;
import com.istp.lab1.user.dao.repository.TeacherRepository;
import com.istp.lab1.user.dao.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
class Lab1ApplicationTests {

    @MockitoBean
    private CourseRepository courseRepository;

    @MockitoBean
    private AssignmentRepository assignmentRepository;

    @MockitoBean
    private EnrollmentRepository enrollmentRepository;

    @MockitoBean
    private SubmissionRepository submissionRepository;

    @MockitoBean
    private FileRepository fileRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TeacherRepository teacherRepository;

    @MockitoBean
    private StudentRepository studentRepository;

    @Test
    void contextLoads() {
    }

}
