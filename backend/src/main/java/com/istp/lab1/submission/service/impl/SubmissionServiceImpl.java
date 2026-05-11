package com.istp.lab1.submission.service.impl;

import com.istp.lab1.assignment.dao.entity.AssignmentEntity;
import com.istp.lab1.assignment.dao.repository.AssignmentRepository;
import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ForbiddenException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.course.dao.entity.EnrollmentStatus;
import com.istp.lab1.course.dao.repository.EnrollmentRepository;
import com.istp.lab1.file.dao.entity.FileEntity;
import com.istp.lab1.file.dao.repository.FileRepository;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.security.CurrentUserResolver;
import com.istp.lab1.submission.dao.entity.SubmissionEntity;
import com.istp.lab1.submission.dao.entity.SubmissionStatus;
import com.istp.lab1.submission.dao.repository.SubmissionRepository;
import com.istp.lab1.submission.service.api.SubmissionService;
import com.istp.lab1.submission.service.dto.SubmissionCreateDto;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import com.istp.lab1.user.dao.entity.StudentEntity;
import com.istp.lab1.user.dao.entity.UserRole;
import com.istp.lab1.user.dao.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private static final List<EnrollmentStatus> SUBMISSION_ALLOWED_STATUSES = List.of(
            EnrollmentStatus.ACTIVE,
            EnrollmentStatus.COMPLETED
    );
    private static final String TEACHER_ROLE_REQUIRED = "Teacher role is required";
    private static final String STUDENT_ROLE_REQUIRED = "Student role is required";
    private static final String SUBMISSION_ACCESS_DENIED = "You do not have access to this submission";
    private static final String SUBMISSION_NOT_ALLOWED = "You are not enrolled in this course";

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final FileRepository fileRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CurrentUserResolver currentUserResolver;

    @Override
    @Transactional
    public SubmissionDto submitAssignment(Long assignmentId, SubmissionCreateDto submission) {
        CurrentUser currentUser = currentUser();
        AssignmentEntity assignment = findAssignment(assignmentId);
        StudentEntity student = findStudentForCurrentUser(currentUser);
        FileEntity file = findFile(submission.fileId());

        if (!enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(
                student.getId(),
                assignment.getCourse().getId(),
                SUBMISSION_ALLOWED_STATUSES
        )) {
            throw new ForbiddenException(SUBMISSION_NOT_ALLOWED);
        }

        if (submissionRepository.existsByAssignmentIdAndStudentId(assignment.getId(), student.getId())) {
            throw new BadRequestException("Student has already submitted this assignment");
        }

        SubmissionEntity savedSubmission = submissionRepository.save(new SubmissionEntity(
                assignment,
                student,
                LocalDateTime.now(),
                file,
                submission.comment(),
                SubmissionStatus.SUBMITTED
        ));

        return toDto(savedSubmission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionDto> getSubmittedByStudent() {
        CurrentUser currentUser = currentUser();
        StudentEntity student = findStudentForCurrentUser(currentUser);
        return submissionRepository.findByStudentIdOrderByIdAsc(student.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionDto> getAssignmentSubmissions(Long assignmentId) {
        CurrentUser currentUser = currentUser();
        AssignmentEntity assignment = findAssignment(assignmentId);
        requireAssignmentTeacher(currentUser, assignment);
        return submissionRepository.findByAssignmentIdOrderByIdAsc(assignment.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionDto getSubmissionById(Long submissionId) {
        CurrentUser currentUser = currentUser();
        SubmissionEntity submission = findSubmission(submissionId);
        requireSubmissionAccess(currentUser, submission);
        return toDto(submission);
    }

    private AssignmentEntity findAssignment(Long assignmentId) {
        return assignmentRepository.findWithCourseById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    }

    private StudentEntity findStudentForCurrentUser(CurrentUser currentUser) {
        requireRole(currentUser, UserRole.STUDENT, STUDENT_ROLE_REQUIRED);
        return studentRepository.findByUserId(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private SubmissionEntity findSubmission(Long submissionId) {
        return submissionRepository.findWithDetailsById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
    }

    private FileEntity findFile(Long fileId) {
        if (fileId == null) {
            throw new BadRequestException("fileId is required");
        }
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    private SubmissionDto toDto(SubmissionEntity submission) {
        return new SubmissionDto(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getStudent().getId(),
                submission.getStudent().getUser().getFullName(),
                submission.getComment(),
                submission.getFile() == null ? null : submission.getFile().getId(),
                submission.getSubmissionDate(),
                submission.getScore(),
                submission.getFeedback()
        );
    }

    private void requireAssignmentTeacher(CurrentUser currentUser, AssignmentEntity assignment) {
        requireRole(currentUser, UserRole.TEACHER, TEACHER_ROLE_REQUIRED);
        if (!assignment.getCourse().getTeacher().getUser().getId().equals(currentUser.id())) {
            throw new ForbiddenException(SUBMISSION_ACCESS_DENIED);
        }
    }

    private void requireSubmissionAccess(CurrentUser currentUser, SubmissionEntity submission) {
        if (currentUser.role() == UserRole.STUDENT) {
            if (!submission.getStudent().getUser().getId().equals(currentUser.id())) {
                throw new ForbiddenException(SUBMISSION_ACCESS_DENIED);
            }
            return;
        }

        requireAssignmentTeacher(currentUser, submission.getAssignment());
    }

    private void requireRole(CurrentUser currentUser, UserRole expectedRole, String message) {
        if (currentUser.role() != expectedRole) {
            throw new ForbiddenException(message);
        }
    }

    private CurrentUser currentUser() {
        return currentUserResolver.resolveCurrentUser();
    }
}
