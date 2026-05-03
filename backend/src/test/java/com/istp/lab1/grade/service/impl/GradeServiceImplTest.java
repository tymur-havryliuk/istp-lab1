package com.istp.lab1.grade.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.istp.lab1.assignment.dao.entity.AssignmentEntity;
import com.istp.lab1.course.dao.entity.CourseEntity;
import com.istp.lab1.course.dao.entity.CourseStatus;
import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ForbiddenException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.file.dao.entity.FileEntity;
import com.istp.lab1.grade.service.dto.GradeDto;
import com.istp.lab1.grade.service.dto.GradeSaveDto;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.submission.dao.entity.SubmissionEntity;
import com.istp.lab1.submission.dao.entity.SubmissionStatus;
import com.istp.lab1.submission.dao.repository.SubmissionRepository;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import com.istp.lab1.user.dao.entity.StudentEntity;
import com.istp.lab1.user.dao.entity.TeacherEntity;
import com.istp.lab1.user.dao.entity.UserEntity;
import com.istp.lab1.user.dao.entity.UserRole;
import com.istp.lab1.user.dao.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GradeServiceImplTest {

    private static final LocalDateTime SUBMITTED_AT = LocalDateTime.of(2026, 4, 20, 18, 30);
    private static final LocalDateTime GRADED_AT = LocalDateTime.of(2026, 4, 21, 12, 0);
    private static final CurrentUser TEACHER_USER = new CurrentUser(1L, "teacher@example.com", UserRole.TEACHER);
    private static final CurrentUser STUDENT_USER = new CurrentUser(2L, "student@example.com", UserRole.STUDENT);
    private static final CurrentUser OTHER_TEACHER_USER = new CurrentUser(9L, "other.teacher@example.com", UserRole.TEACHER);

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private GradeServiceImpl gradeService;

    @Test
    void gradeSubmissionUpdatesReviewedSubmission() {
        SubmissionEntity submission = submission(100L, null, null, null, 1L, 2L);
        when(submissionRepository.findWithDetailsById(100L)).thenReturn(Optional.of(submission));

        SubmissionDto result = gradeService.gradeSubmission(TEACHER_USER, 100L, new GradeSaveDto(95, "Good work"));

        assertThat(result.grade()).isEqualTo(95);
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.REVIEWED);
    }

    @Test
    void gradeSubmissionRejectsGradeAboveAssignmentMaxScore() {
        SubmissionEntity submission = submission(100L, null, null, null, 1L, 2L);
        when(submissionRepository.findWithDetailsById(100L)).thenReturn(Optional.of(submission));

        assertThatThrownBy(() -> gradeService.gradeSubmission(TEACHER_USER, 100L, new GradeSaveDto(101, "Too high")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Grade must not be greater than assignment max score");
    }

    @Test
    void gradeSubmissionRejectsNonOwnerTeacher() {
        SubmissionEntity submission = submission(100L, null, null, null, 1L, 2L);
        when(submissionRepository.findWithDetailsById(100L)).thenReturn(Optional.of(submission));

        assertThatThrownBy(() -> gradeService.gradeSubmission(OTHER_TEACHER_USER, 100L, new GradeSaveDto(95, "Good work")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You do not have access to this submission");
    }

    @Test
    void getStudentGradesMapsGrades() {
        StudentEntity student = student(2L, 2L, "Lin Student");
        SubmissionEntity submission = submission(100L, 95, "Good work", GRADED_AT, 1L, 2L);
        when(studentRepository.findByUserId(2L)).thenReturn(Optional.of(student));
        when(submissionRepository.findByStudentIdAndScoreIsNotNullOrderByIdAsc(2L)).thenReturn(List.of(submission));

        List<GradeDto> result = gradeService.getStudentGrades(STUDENT_USER);

        assertThat(result).containsExactly(new GradeDto(
                100L,
                1L,
                "ER Model Design",
                3L,
                "Database Systems",
                95,
                "Good work",
                GRADED_AT
        ));
    }

    @Test
    void getStudentGradesRejectsWrongRole() {
        assertThatThrownBy(() -> gradeService.getStudentGrades(TEACHER_USER))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Student role is required");
    }

    @Test
    void gradeSubmissionThrowsWhenMissing() {
        when(submissionRepository.findWithDetailsById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.gradeSubmission(TEACHER_USER, 404L, new GradeSaveDto(95, "Good work")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Submission not found");
    }

    private SubmissionEntity submission(
            Long id,
            Integer score,
            String feedback,
            LocalDateTime gradedAt,
            Long teacherUserId,
            Long studentUserId
    ) {
        SubmissionEntity submission = new SubmissionEntity(
                assignment(1L, teacherUserId),
                student(2L, studentUserId, "Lin Student"),
                SUBMITTED_AT,
                file(10L),
                "Done",
                SubmissionStatus.SUBMITTED
        );
        ReflectionTestUtils.setField(submission, "id", id);
        ReflectionTestUtils.setField(submission, "score", score);
        ReflectionTestUtils.setField(submission, "feedback", feedback);
        ReflectionTestUtils.setField(submission, "gradedAt", gradedAt);
        return submission;
    }

    private AssignmentEntity assignment(Long id, Long teacherUserId) {
        AssignmentEntity assignment = new AssignmentEntity(
                course(3L, teacherUserId),
                "ER Model Design",
                "Design an ER diagram",
                LocalDateTime.of(2026, 5, 1, 23, 59),
                100
        );
        ReflectionTestUtils.setField(assignment, "id", id);
        return assignment;
    }

    private CourseEntity course(Long id, Long teacherUserId) {
        TeacherEntity teacher = org.mockito.Mockito.mock(TeacherEntity.class);
        UserEntity teacherUser = user(teacherUserId, UserRole.TEACHER, "Ada Teacher");
        lenient().when(teacher.getId()).thenReturn(1L);
        lenient().when(teacher.getUser()).thenReturn(teacherUser);

        CourseEntity course = new CourseEntity("Database Systems", "Intro course", teacher, CourseStatus.ACTIVE);
        ReflectionTestUtils.setField(course, "id", id);
        return course;
    }

    private StudentEntity student(Long id, Long userId, String fullName) {
        StudentEntity student = org.mockito.Mockito.mock(StudentEntity.class);
        UserEntity studentUser = user(userId, UserRole.STUDENT, fullName);
        lenient().when(student.getId()).thenReturn(id);
        lenient().when(student.getUser()).thenReturn(studentUser);
        return student;
    }

    private UserEntity user(Long id, UserRole role, String fullName) {
        UserEntity user = org.mockito.Mockito.mock(UserEntity.class);
        lenient().when(user.getId()).thenReturn(id);
        lenient().when(user.getRole()).thenReturn(role);
        lenient().when(user.getFullName()).thenReturn(fullName);
        return user;
    }

    private FileEntity file(Long id) {
        FileEntity file = new FileEntity("lab.pdf", "application/pdf", 10L, id + "-lab.pdf");
        ReflectionTestUtils.setField(file, "id", id);
        return file;
    }
}
