package com.istp.lab1.course.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.istp.lab1.course.dao.entity.CourseEntity;
import com.istp.lab1.course.dao.entity.CourseStatus;
import com.istp.lab1.course.dao.entity.EnrollmentEntity;
import com.istp.lab1.course.dao.entity.EnrollmentStatus;
import com.istp.lab1.course.dao.repository.CourseRepository;
import com.istp.lab1.course.dao.repository.EnrollmentRepository;
import com.istp.lab1.course.service.dto.CourseCreateDto;
import com.istp.lab1.course.service.dto.CourseDto;
import com.istp.lab1.course.service.dto.CourseSaveDto;
import com.istp.lab1.course.service.dto.CourseStudentDto;
import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ForbiddenException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.security.CurrentUserResolver;
import com.istp.lab1.user.dao.entity.StudentEntity;
import com.istp.lab1.user.dao.entity.TeacherEntity;
import com.istp.lab1.user.dao.entity.UserEntity;
import com.istp.lab1.user.dao.entity.UserRole;
import com.istp.lab1.user.dao.repository.StudentRepository;
import com.istp.lab1.user.dao.repository.TeacherRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    private static final CurrentUser TEACHER_USER = new CurrentUser(1L, "teacher@example.com", UserRole.TEACHER);
    private static final CurrentUser OTHER_TEACHER_USER = new CurrentUser(9L, "other.teacher@example.com", UserRole.TEACHER);
    private static final CurrentUser STUDENT_USER = new CurrentUser(2L, "student@example.com", UserRole.STUDENT);

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CurrentUserResolver currentUserResolver;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void getCoursesUsesActiveAndPlannedByDefault() {
        TeacherEntity teacher = teacher(10L, user(1L, UserRole.TEACHER, "Ada Teacher", "ada@example.com"));
        CourseEntity course = course(100L, "Java Basics", "Intro course", teacher, CourseStatus.ACTIVE);
        when(courseRepository.findByStatusInOrderByIdAsc(List.of(CourseStatus.ACTIVE, CourseStatus.PLANNED)))
                .thenReturn(List.of(course));

        List<CourseDto> result = courseService.getCourses(null);

        assertThat(result).containsExactly(new CourseDto(
                100L,
                "Java Basics",
                "Intro course",
                10L,
                "Ada Teacher",
                "ACTIVE"
        ));
    }

    @Test
    void createCourseUsesTeacherFromTrustedCurrentUser() {
        TeacherEntity teacher = teacher(10L, user(1L, UserRole.TEACHER, "Ada Teacher", "ada@example.com"));
        when(currentUserResolver.resolveCurrentUser()).thenReturn(TEACHER_USER);
        when(teacherRepository.findByUserId(1L)).thenReturn(Optional.of(teacher));
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(invocation -> {
            CourseEntity savedCourse = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedCourse, "id", 100L);
            return savedCourse;
        });

        CourseDto result = courseService.createCourse(new CourseCreateDto("Java Basics", "Intro course"));

        assertThat(result).isEqualTo(new CourseDto(
                100L,
                "Java Basics",
                "Intro course",
                10L,
                "Ada Teacher",
                "ACTIVE"
        ));

        ArgumentCaptor<CourseEntity> courseCaptor = ArgumentCaptor.forClass(CourseEntity.class);
        verify(courseRepository).save(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getTeacher()).isSameAs(teacher);
    }

    @Test
    void createCourseRejectsStudentRole() {
        when(currentUserResolver.resolveCurrentUser()).thenReturn(STUDENT_USER);

        assertThatThrownBy(() -> courseService.createCourse(new CourseCreateDto("Java Basics", "Intro course")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Teacher role is required");
    }

    @Test
    void updateCourseRequiresOwnerTeacher() {
        TeacherEntity teacher = teacher(10L, user(1L, UserRole.TEACHER, "Ada Teacher", "ada@example.com"));
        CourseEntity course = course(100L, "Old title", "Old description", teacher, CourseStatus.ACTIVE);
        when(currentUserResolver.resolveCurrentUser()).thenReturn(TEACHER_USER);
        when(courseRepository.findWithTeacherById(100L)).thenReturn(Optional.of(course));

        CourseDto result = courseService.updateCourse(100L, new CourseSaveDto("New title", "New description"));

        assertThat(result.title()).isEqualTo("New title");
        assertThat(result.description()).isEqualTo("New description");
    }

    @Test
    void updateCourseRejectsNonOwnerTeacher() {
        TeacherEntity teacher = teacher(10L, user(1L, UserRole.TEACHER, "Ada Teacher", "ada@example.com"));
        CourseEntity course = course(100L, "Old title", "Old description", teacher, CourseStatus.ACTIVE);
        when(currentUserResolver.resolveCurrentUser()).thenReturn(OTHER_TEACHER_USER);
        when(courseRepository.findWithTeacherById(100L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.updateCourse(100L, new CourseSaveDto("New title", "New description")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You do not have access to this course");
    }

    @Test
    void deleteCourseCancelsCourseForOwner() {
        TeacherEntity teacher = teacher(10L, user(1L, UserRole.TEACHER, "Ada Teacher", "ada@example.com"));
        CourseEntity course = course(100L, "Java Basics", "Intro course", teacher, CourseStatus.ACTIVE);
        when(currentUserResolver.resolveCurrentUser()).thenReturn(TEACHER_USER);
        when(courseRepository.findWithTeacherById(100L)).thenReturn(Optional.of(course));

        courseService.deleteCourse(100L);

        assertThat(course.getStatus()).isEqualTo(CourseStatus.CANCELLED);
    }

    @Test
    void enrollInCourseUsesStudentFromTrustedCurrentUser() {
        StudentEntity student = student(20L, user(2L, UserRole.STUDENT, "Lin Student", "lin@example.com"));
        TeacherEntity teacher = teacher(10L, user(1L, UserRole.TEACHER, "Ada Teacher", "ada@example.com"));
        CourseEntity course = course(100L, "Java Basics", "Intro course", teacher, CourseStatus.ACTIVE);
        when(currentUserResolver.resolveCurrentUser()).thenReturn(STUDENT_USER);
        when(studentRepository.findByUserId(2L)).thenReturn(Optional.of(student));
        when(courseRepository.findWithTeacherById(100L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(20L, 100L)).thenReturn(false);

        var result = courseService.enrollInCourse(100L);

        assertThat(result.courseId()).isEqualTo(100L);
        assertThat(result.studentId()).isEqualTo(20L);
        assertThat(result.status()).isEqualTo("ENROLLED");
        verify(enrollmentRepository).save(any(EnrollmentEntity.class));
    }

    @Test
    void enrollInCourseRejectsDuplicateEnrollment() {
        StudentEntity student = student(20L, user(2L, UserRole.STUDENT, "Lin Student", "lin@example.com"));
        TeacherEntity teacher = teacher(10L, user(1L, UserRole.TEACHER, "Ada Teacher", "ada@example.com"));
        CourseEntity course = course(100L, "Java Basics", "Intro course", teacher, CourseStatus.ACTIVE);
        when(currentUserResolver.resolveCurrentUser()).thenReturn(STUDENT_USER);
        when(studentRepository.findByUserId(2L)).thenReturn(Optional.of(student));
        when(courseRepository.findWithTeacherById(100L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(20L, 100L)).thenReturn(true);

        assertThatThrownBy(() -> courseService.enrollInCourse(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Student is already enrolled in course");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void getEnrolledCoursesReturnsStudentCourses() {
        StudentEntity student = student(20L, user(2L, UserRole.STUDENT, "Lin Student", "lin@example.com"));
        TeacherEntity teacher = teacher(10L, user(1L, UserRole.TEACHER, "Ada Teacher", "ada@example.com"));
        CourseEntity course = course(100L, "Java Basics", "Intro course", teacher, CourseStatus.ACTIVE);
        when(currentUserResolver.resolveCurrentUser()).thenReturn(STUDENT_USER);
        when(studentRepository.findByUserId(2L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndStatusIn(20L, List.of(
                EnrollmentStatus.ACTIVE,
                EnrollmentStatus.COMPLETED
        ))).thenReturn(List.of(new EnrollmentEntity(student, course, EnrollmentStatus.ACTIVE)));

        List<CourseDto> result = courseService.getEnrolledCourses();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(100L);
    }

    @Test
    void getOwnedCoursesReturnsTeacherCoursesExceptCancelled() {
        TeacherEntity teacher = teacher(10L, user(1L, UserRole.TEACHER, "Ada Teacher", "ada@example.com"));
        CourseEntity course = course(100L, "Java Basics", "Intro course", teacher, CourseStatus.PLANNED);
        when(currentUserResolver.resolveCurrentUser()).thenReturn(TEACHER_USER);
        when(teacherRepository.findByUserId(1L)).thenReturn(Optional.of(teacher));
        when(courseRepository.findByTeacherIdAndStatusNotOrderByIdAsc(10L, CourseStatus.CANCELLED))
                .thenReturn(List.of(course));

        List<CourseDto> result = courseService.getOwnedCourses();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo("PLANNED");
    }

    @Test
    void getCourseStudentsRequiresOwnerTeacher() {
        TeacherEntity teacher = teacher(10L, user(1L, UserRole.TEACHER, "Ada Teacher", "ada@example.com"));
        CourseEntity course = course(100L, "Java Basics", "Intro course", teacher, CourseStatus.ACTIVE);
        StudentEntity student = student(20L, user(2L, UserRole.STUDENT, "Lin Student", "lin@example.com"));
        when(currentUserResolver.resolveCurrentUser()).thenReturn(TEACHER_USER);
        when(courseRepository.findWithTeacherById(100L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseIdAndStatusIn(100L, List.of(
                EnrollmentStatus.ACTIVE,
                EnrollmentStatus.COMPLETED
        ))).thenReturn(List.of(new EnrollmentEntity(student, course, EnrollmentStatus.ACTIVE)));

        List<CourseStudentDto> result = courseService.getCourseStudents(100L);

        assertThat(result).containsExactly(new CourseStudentDto(
                20L,
                "Lin Student",
                "lin@example.com",
                "STUDENT"
        ));
    }

    @Test
    void getCourseByIdThrowsWhenMissing() {
        when(courseRepository.findWithTeacherById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course not found");
    }

    private UserEntity user(Long id, UserRole role, String fullName, String email) {
        UserEntity user = org.mockito.Mockito.mock(UserEntity.class);
        lenient().when(user.getId()).thenReturn(id);
        lenient().when(user.getRole()).thenReturn(role);
        lenient().when(user.getFullName()).thenReturn(fullName);
        lenient().when(user.getEmail()).thenReturn(email);
        return user;
    }

    private TeacherEntity teacher(Long id, UserEntity user) {
        TeacherEntity teacher = org.mockito.Mockito.mock(TeacherEntity.class);
        lenient().when(teacher.getId()).thenReturn(id);
        lenient().when(teacher.getUser()).thenReturn(user);
        return teacher;
    }

    private StudentEntity student(Long id, UserEntity user) {
        StudentEntity student = org.mockito.Mockito.mock(StudentEntity.class);
        lenient().when(student.getId()).thenReturn(id);
        lenient().when(student.getUser()).thenReturn(user);
        return student;
    }

    private CourseEntity course(Long id, String title, String description, TeacherEntity teacher, CourseStatus status) {
        CourseEntity course = new CourseEntity(title, description, teacher, status);
        ReflectionTestUtils.setField(course, "id", id);
        return course;
    }
}
