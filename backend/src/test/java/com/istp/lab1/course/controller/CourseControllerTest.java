package com.istp.lab1.course.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.istp.lab1.course.controller.mapper.CourseMapperImpl;
import com.istp.lab1.course.service.api.CourseService;
import com.istp.lab1.course.service.dto.CourseCreateDto;
import com.istp.lab1.course.service.dto.CourseDto;
import com.istp.lab1.course.service.dto.CourseSaveDto;
import com.istp.lab1.course.service.dto.CourseStudentDto;
import com.istp.lab1.course.service.dto.EnrollmentDto;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.security.CurrentUserResolver;
import com.istp.lab1.user.dao.entity.UserRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CourseController.class)
@Import(CourseMapperImpl.class)
class CourseControllerTest {

    private static final String COURSES_URL = "/api/v1/courses";
    private static final CurrentUser TEACHER_USER = new CurrentUser(1L, "teacher@example.com", UserRole.TEACHER);
    private static final CurrentUser STUDENT_USER = new CurrentUser(2L, "student@example.com", UserRole.STUDENT);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    @Test
    void getCoursesReturnsMappedCourses() throws Exception {
        when(courseService.getCourses("ACTIVE,PLANNED")).thenReturn(List.of(new CourseDto(
                1L,
                "Java Basics",
                "Intro course",
                10L,
                "Ada Teacher",
                "ACTIVE"
        )));

        mockMvc.perform(get(COURSES_URL).param("statuses", "ACTIVE,PLANNED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Basics"))
                .andExpect(jsonPath("$[0].teacherId").value(10))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(courseService).getCourses("ACTIVE,PLANNED");
    }

    @Test
    void getEnrolledCoursesUsesTrustedCurrentUser() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(STUDENT_USER);
        when(courseService.getEnrolledCourses(STUDENT_USER)).thenReturn(List.of(new CourseDto(
                2L,
                "Databases",
                "SQL and transactions",
                11L,
                "Grace Teacher",
                "PLANNED"
        )));

        mockMvc.perform(get(COURSES_URL + "/enrolled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].status").value("PLANNED"));

        verify(courseService).getEnrolledCourses(STUDENT_USER);
    }

    @Test
    void getOwnedCoursesUsesTrustedCurrentUser() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(TEACHER_USER);
        when(courseService.getOwnedCourses(TEACHER_USER)).thenReturn(List.of(new CourseDto(
                3L,
                "Architecture",
                "System design",
                11L,
                "Grace Teacher",
                "ACTIVE"
        )));

        mockMvc.perform(get(COURSES_URL + "/owned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].teacherId").value(11));

        verify(courseService).getOwnedCourses(TEACHER_USER);
    }

    @Test
    void getCourseByIdReturnsMappedCourse() throws Exception {
        when(courseService.getCourseById(1L)).thenReturn(new CourseDto(
                1L,
                "Java Basics",
                "Intro course",
                10L,
                "Ada Teacher",
                "ACTIVE"
        ));

        mockMvc.perform(get(COURSES_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.teacherName").value("Ada Teacher"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(courseService).getCourseById(1L);
    }

    @Test
    void createCoursePassesTrustedCurrentUserThroughMapper() throws Exception {
        CourseCreateDto createDto = new CourseCreateDto("Java Basics", "Intro course");
        when(currentUserResolver.resolve(any())).thenReturn(TEACHER_USER);
        when(courseService.createCourse(TEACHER_USER, createDto)).thenReturn(new CourseDto(
                1L,
                "Java Basics",
                "Intro course",
                20L,
                "Ada Teacher",
                "ACTIVE"
        ));

        mockMvc.perform(post(COURSES_URL)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Java Basics",
                                  "description": "Intro course"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(courseService).createCourse(TEACHER_USER, createDto);
    }

    @Test
    void createCourseWithInvalidBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post(COURSES_URL)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "Intro course"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("title: must not be blank"));

        verifyNoInteractions(courseService);
    }

    @Test
    void updateCoursePassesTrustedCurrentUserThroughMapper() throws Exception {
        CourseSaveDto saveDto = new CourseSaveDto("Updated title", "Updated description");
        when(currentUserResolver.resolve(any())).thenReturn(TEACHER_USER);
        when(courseService.updateCourse(TEACHER_USER, 1L, saveDto)).thenReturn(new CourseDto(
                1L,
                "Updated title",
                "Updated description",
                20L,
                "Ada Teacher",
                "ACTIVE"
        ));

        mockMvc.perform(put(COURSES_URL + "/1")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title",
                                  "description": "Updated description"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));

        verify(courseService).updateCourse(TEACHER_USER, 1L, saveDto);
    }

    @Test
    void deleteCourseReturnsContractMessage() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(TEACHER_USER);

        mockMvc.perform(delete(COURSES_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course deleted successfully"));

        verify(courseService).deleteCourse(TEACHER_USER, 1L);
    }

    @Test
    void enrollInCourseReturnsContractResponse() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(STUDENT_USER);
        when(courseService.enrollInCourse(STUDENT_USER, 1L)).thenReturn(new EnrollmentDto(1L, 30L, "ENROLLED"));

        mockMvc.perform(post(COURSES_URL + "/1/enroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.studentId").value(30))
                .andExpect(jsonPath("$.status").value("ENROLLED"));

        verify(courseService).enrollInCourse(STUDENT_USER, 1L);
    }

    @Test
    void getCourseStudentsReturnsMappedStudents() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(TEACHER_USER);
        when(courseService.getCourseStudents(TEACHER_USER, 1L)).thenReturn(List.of(new CourseStudentDto(
                30L,
                "Lin Student",
                "lin@example.com",
                "STUDENT"
        )));

        mockMvc.perform(get(COURSES_URL + "/1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(30))
                .andExpect(jsonPath("$[0].role").value("STUDENT"));

        verify(courseService).getCourseStudents(TEACHER_USER, 1L);
    }

    @Test
    void getCourseByIdReturnsNotFound() throws Exception {
        when(courseService.getCourseById(999L)).thenThrow(new ResourceNotFoundException("Course not found"));

        mockMvc.perform(get(COURSES_URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Course not found"))
                .andExpect(jsonPath("$.path").value(COURSES_URL + "/999"));
    }
}
