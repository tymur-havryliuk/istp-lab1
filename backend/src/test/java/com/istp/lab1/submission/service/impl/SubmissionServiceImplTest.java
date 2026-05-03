package com.istp.lab1.submission.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.istp.lab1.assignment.dao.entity.AssignmentEntity;
import com.istp.lab1.assignment.dao.repository.AssignmentRepository;
import com.istp.lab1.course.dao.entity.CourseEntity;
import com.istp.lab1.course.dao.entity.CourseStatus;
import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.file.dao.entity.FileEntity;
import com.istp.lab1.file.dao.repository.FileRepository;
import com.istp.lab1.submission.dao.entity.SubmissionEntity;
import com.istp.lab1.submission.dao.entity.SubmissionStatus;
import com.istp.lab1.submission.dao.repository.SubmissionRepository;
import com.istp.lab1.submission.service.dto.SubmissionCreateDto;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {

    private static final LocalDateTime SUBMITTED_AT = LocalDateTime.of(2026, 4, 20, 18, 30);

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    @Test
    void submitAssignmentSavesSubmittedSubmission() {
        AssignmentEntity assignment = assignment(1L);
        StudentEntity student = student(2L);
        when(assignmentRepository.findWithCourseById(1L)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(fileRepository.findById(10L)).thenReturn(Optional.of(file(10L)));
        when(submissionRepository.existsByAssignmentIdAndStudentId(1L, 2L)).thenReturn(false);
        when(submissionRepository.save(any(SubmissionEntity.class))).thenAnswer(invocation -> {
            SubmissionEntity savedSubmission = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedSubmission, "id", 100L);
            return savedSubmission;
        });

        SubmissionDto result = submissionService.submitAssignment(1L, 2L, new SubmissionCreateDto("Done", 10L));

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.assignmentId()).isEqualTo(1L);
        assertThat(result.studentId()).isEqualTo(2L);
        assertThat(result.studentName()).isEqualTo("Lin Student");
        assertThat(result.comment()).isEqualTo("Done");
        assertThat(result.fileId()).isEqualTo(10L);
        assertThat(result.submittedAt()).isNotNull();
        assertThat(result.grade()).isNull();
        assertThat(result.feedback()).isNull();

        ArgumentCaptor<SubmissionEntity> submissionCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);
        verify(submissionRepository).save(submissionCaptor.capture());
        assertThat(submissionCaptor.getValue().getAssignment()).isSameAs(assignment);
        assertThat(submissionCaptor.getValue().getStudent()).isSameAs(student);
        assertThat(submissionCaptor.getValue().getFile().getId()).isEqualTo(10L);
        assertThat(submissionCaptor.getValue().getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
    }

    @Test
    void submitAssignmentRejectsDuplicateSubmission() {
        AssignmentEntity assignment = assignment(1L);
        StudentEntity student = student(2L);
        when(assignmentRepository.findWithCourseById(1L)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(fileRepository.findById(10L)).thenReturn(Optional.of(file(10L)));
        when(submissionRepository.existsByAssignmentIdAndStudentId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> submissionService.submitAssignment(1L, 2L, new SubmissionCreateDto("Done", 10L)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Student has already submitted this assignment");

        verify(submissionRepository, never()).save(any());
    }

    @Test
    void submitAssignmentThrowsWhenFileMissing() {
        AssignmentEntity assignment = assignment(1L);
        StudentEntity student = student(2L);
        when(assignmentRepository.findWithCourseById(1L)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(fileRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.submitAssignment(1L, 2L, new SubmissionCreateDto("Done", 404L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("File not found");
    }

    @Test
    void submitAssignmentThrowsWhenAssignmentMissing() {
        when(assignmentRepository.findWithCourseById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.submitAssignment(404L, 2L, new SubmissionCreateDto("Done", 10L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Assignment not found");
    }

    @Test
    void submitAssignmentThrowsWhenStudentMissing() {
        AssignmentEntity assignment = assignment(1L);
        when(assignmentRepository.findWithCourseById(1L)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.submitAssignment(1L, 404L, new SubmissionCreateDto("Done", 10L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Student not found");
    }

    @Test
    void getSubmittedByStudentMapsSubmissions() {
        StudentEntity student = student(2L);
        SubmissionEntity submission = reviewedSubmission(file(10L));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(submissionRepository.findByStudentIdOrderByIdAsc(2L)).thenReturn(List.of(submission));

        List<SubmissionDto> result = submissionService.getSubmittedByStudent(2L);

        assertThat(result).containsExactly(new SubmissionDto(
                100L,
                1L,
                2L,
                "Lin Student",
                "Done",
                10L,
                SUBMITTED_AT,
                95,
                "Good work"
        ));
    }

    @Test
    void getAssignmentSubmissionsMapsSubmissions() {
        AssignmentEntity assignment = assignment(1L);
        SubmissionEntity submission = reviewedSubmission(file(10L));
        when(assignmentRepository.findWithCourseById(1L)).thenReturn(Optional.of(assignment));
        when(submissionRepository.findByAssignmentIdOrderByIdAsc(1L)).thenReturn(List.of(submission));

        List<SubmissionDto> result = submissionService.getAssignmentSubmissions(1L);

        assertThat(result).extracting(SubmissionDto::id).containsExactly(100L);
        assertThat(result.getFirst().grade()).isEqualTo(95);
    }

    @Test
    void getSubmissionByIdMapsScoreAndSubmittedAt() {
        SubmissionEntity submission = reviewedSubmission(file(10L));
        when(submissionRepository.findWithDetailsById(100L)).thenReturn(Optional.of(submission));

        SubmissionDto result = submissionService.getSubmissionById(100L);

        assertThat(result).isEqualTo(new SubmissionDto(
                100L,
                1L,
                2L,
                "Lin Student",
                "Done",
                10L,
                SUBMITTED_AT,
                95,
                "Good work"
        ));
    }

    @Test
    void getSubmissionByIdReturnsNullFileIdWhenFileIsMissing() {
        SubmissionEntity submission = reviewedSubmission(null);
        when(submissionRepository.findWithDetailsById(100L)).thenReturn(Optional.of(submission));

        SubmissionDto result = submissionService.getSubmissionById(100L);

        assertThat(result.fileId()).isNull();
    }

    @Test
    void getSubmissionByIdThrowsWhenMissing() {
        when(submissionRepository.findWithDetailsById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.getSubmissionById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Submission not found");
    }

    private SubmissionEntity reviewedSubmission(FileEntity file) {
        SubmissionEntity submission = new SubmissionEntity(
                assignment(1L),
                student(2L),
                SUBMITTED_AT,
                file,
                "Done",
                SubmissionStatus.REVIEWED
        );
        ReflectionTestUtils.setField(submission, "id", 100L);
        ReflectionTestUtils.setField(submission, "score", 95);
        ReflectionTestUtils.setField(submission, "feedback", "Good work");
        ReflectionTestUtils.setField(submission, "gradedAt", SUBMITTED_AT.plusDays(1));
        return submission;
    }

    private FileEntity file(Long id) {
        FileEntity file = new FileEntity("lab.pdf", "application/pdf", 10L, id + "-lab.pdf");
        ReflectionTestUtils.setField(file, "id", id);
        return file;
    }

    private AssignmentEntity assignment(Long id) {
        AssignmentEntity assignment = new AssignmentEntity(
                course(1L),
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

        CourseEntity course = new CourseEntity("Java Basics", "Intro course", teacher, CourseStatus.ACTIVE);
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
}
