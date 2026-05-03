package com.istp.lab1.grade.service.impl;

import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.grade.service.api.GradeService;
import com.istp.lab1.grade.service.dto.GradeDto;
import com.istp.lab1.grade.service.dto.GradeSaveDto;
import com.istp.lab1.submission.dao.entity.SubmissionEntity;
import com.istp.lab1.submission.dao.repository.SubmissionRepository;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import com.istp.lab1.user.dao.entity.StudentEntity;
import com.istp.lab1.user.dao.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public SubmissionDto gradeSubmission(Long submissionId, GradeSaveDto grade) {
        SubmissionEntity submission = findSubmission(submissionId);

        if (grade.grade() > submission.getAssignment().getMaxScore()) {
            throw new BadRequestException("Grade must not be greater than assignment max score");
        }

        submission.grade(grade.grade(), grade.feedback(), LocalDateTime.now());
        return toSubmissionDto(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeDto> getStudentGrades(Long studentId) {
        StudentEntity student = findStudent(studentId);
        return submissionRepository.findByStudentIdAndScoreIsNotNullOrderByIdAsc(student.getId()).stream()
                .map(this::toGradeDto)
                .toList();
    }

    private SubmissionEntity findSubmission(Long submissionId) {
        return submissionRepository.findWithDetailsById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
    }

    private StudentEntity findStudent(Long studentId) {
        if (studentId == null) {
            throw new BadRequestException("studentId is required");
        }
        return studentRepository.findById(studentId)
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
}
