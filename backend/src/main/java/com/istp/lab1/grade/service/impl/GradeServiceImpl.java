package com.istp.lab1.grade.service.impl;

import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ForbiddenException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.grade.service.api.GradeService;
import com.istp.lab1.grade.service.dto.GradeDto;
import com.istp.lab1.grade.service.dto.GradeSaveDto;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.submission.dao.entity.SubmissionEntity;
import com.istp.lab1.submission.dao.repository.SubmissionRepository;
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
public class GradeServiceImpl implements GradeService {

    private static final String TEACHER_ROLE_REQUIRED = "Teacher role is required";
    private static final String STUDENT_ROLE_REQUIRED = "Student role is required";
    private static final String GRADE_ACCESS_DENIED = "You do not have access to this submission";

    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public SubmissionDto gradeSubmission(CurrentUser currentUser, Long submissionId, GradeSaveDto grade) {
        SubmissionEntity submission = findSubmission(submissionId);
        requireGradeAccess(currentUser, submission);

        if (grade.grade() > submission.getAssignment().getMaxScore()) {
            throw new BadRequestException("Grade must not be greater than assignment max score");
        }

        submission.grade(grade.grade(), grade.feedback(), LocalDateTime.now());
        return toSubmissionDto(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeDto> getStudentGrades(CurrentUser currentUser) {
        StudentEntity student = findStudentForCurrentUser(currentUser);
        return submissionRepository.findByStudentIdAndScoreIsNotNullOrderByIdAsc(student.getId()).stream()
                .map(this::toGradeDto)
                .toList();
    }

    private SubmissionEntity findSubmission(Long submissionId) {
        return submissionRepository.findWithDetailsById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
    }

    private StudentEntity findStudentForCurrentUser(CurrentUser currentUser) {
        if (currentUser.role() != UserRole.STUDENT) {
            throw new ForbiddenException(STUDENT_ROLE_REQUIRED);
        }
        return studentRepository.findByUserId(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private SubmissionDto toSubmissionDto(SubmissionEntity submission) {
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

    private GradeDto toGradeDto(SubmissionEntity submission) {
        return new GradeDto(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getAssignment().getTitle(),
                submission.getAssignment().getCourse().getId(),
                submission.getAssignment().getCourse().getTitle(),
                submission.getScore(),
                submission.getFeedback(),
                submission.getGradedAt()
        );
    }

    private void requireGradeAccess(CurrentUser currentUser, SubmissionEntity submission) {
        if (currentUser.role() != UserRole.TEACHER) {
            throw new ForbiddenException(TEACHER_ROLE_REQUIRED);
        }
        if (!submission.getAssignment().getCourse().getTeacher().getUser().getId().equals(currentUser.id())) {
            throw new ForbiddenException(GRADE_ACCESS_DENIED);
        }
    }
}
