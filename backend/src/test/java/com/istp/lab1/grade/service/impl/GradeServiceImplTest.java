package com.istp.lab1.grade.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.istp.lab1.assignment.dao.entity.AssignmentEntity;
import com.istp.lab1.course.dao.entity.CourseEntity;
import com.istp.lab1.course.dao.entity.CourseStatus;
import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.file.dao.entity.FileEntity;
import com.istp.lab1.grade.service.dto.GradeDto;
import com.istp.lab1.grade.service.dto.GradeSaveDto;
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

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private GradeServiceImpl gradeService;

    @Test
    void gradeSubmissionUpdatesReviewedSubmission() {
        SubmissionEntity submission = submission(100L, null, null, null);
        when(submissionRepository.findWithDetailsById(100L)).thenReturn(Optional.of(submission));

        SubmissionDto result = gradeService.gradeSubmission(100L, new GradeSaveDto(95, "Good work"));

        assertThat(result.grade()).isEqualTo(95);
        assertThat(result.feedback()).isEqualTo("Good work");
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.REVIEWED);
        assertThat(submission.getScore()).isEqualTo(95);
        assertThat(submission.getFeedback()).isEqualTo("Good work");
        assertThat(submission.getGradedAt()).isNotNull();
    }

    @Test
    void gradeSubmissionRejectsGradeAboveAssignmentMaxScore() {
        SubmissionEntity submission = submission(100L, null, null, null);
        when(submissionRepository.findWithDetailsById(100L)).thenReturn(Optional.of(submission));

        assertThatThrownBy(() -> gradeService.gradeSubmission(100L, new GradeSaveDto(101, "Too high")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Grade must not be greater than assignment max score");
    }

    @Test
    void gradeSubmissionThrowsWhenMissing() {
        when(submissionRepository.findWithDetailsById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.gradeSubmission(404L, new GradeSaveDto(95, "Good work")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Submission not found");
    }

    @Test
    void getStudentGradesMapsGrades() {
        StudentEntity student = student(2L);
        SubmissionEntity submission = submission(100L, 95, "Good work", GRADED_AT);
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(submissionRepository.findByStudentIdAndScoreIsNotNullOrderByIdAsc(2L))
                .thenReturn(List.of(submission));

        List<GradeDto> result = gradeService.getStudentGrades(2L);

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
    void getStudentGradesThrowsWhenStudentMissing() {
        when(studentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.getStudentGrades(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Student not found");
    }

    private SubmissionEntity submission(Long id, Integer score, String feedback, LocalDateTime gradedAt) {
        SubmissionEntity submission = new SubmissionEntity(
                assignment(1L),
                student(2L),
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

    private AssignmentEntity assignment(Long id) {
        AssignmentEntity assignment = new AssignmentEntity(
                course(3L),
                "ER Model Design",
                "Design an ER diagram",
                LocalDateTime.of(2026, 5, 1, 23, 59),
                100
        );
        ReflectionTestUtils.setField(assignment, "id", id);
        return assignment;
    }

    private CourseEntity course(Long id) {
        TeacherEntity teacher = org.mockito.Mockito.mock(TeacherEntity.class);
        UserEntity teacherUser = user(1L, UserRole.TEACHER, "Ada Teacher");
        lenient().when(teacher.getId()).thenReturn(1L);
        lenient().when(teacher.getUser()).thenReturn(teacherUser);

        CourseEntity course = new CourseEntity("Database Systems", "Intro course", teacher, CourseStatus.ACTIVE);
        ReflectionTestUtils.setField(course, "id", id);
        return course;
    }

    private StudentEntity student(Long id) {
        StudentEntity student = org.mockito.Mockito.mock(StudentEntity.class);
        UserEntity studentUser = user(2L, UserRole.STUDENT, "Lin Student");
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
