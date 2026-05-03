package com.istp.lab1.submission.service.impl;

import com.istp.lab1.assignment.dao.entity.AssignmentEntity;
import com.istp.lab1.assignment.dao.repository.AssignmentRepository;
import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.file.dao.entity.FileEntity;
import com.istp.lab1.file.dao.repository.FileRepository;
import com.istp.lab1.submission.dao.entity.SubmissionEntity;
import com.istp.lab1.submission.dao.entity.SubmissionStatus;
import com.istp.lab1.submission.dao.repository.SubmissionRepository;
import com.istp.lab1.submission.service.api.SubmissionService;
import com.istp.lab1.submission.service.dto.SubmissionCreateDto;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import com.istp.lab1.user.dao.entity.StudentEntity;
import com.istp.lab1.user.dao.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final FileRepository fileRepository;

    public SubmissionServiceImpl(
            SubmissionRepository submissionRepository,
            AssignmentRepository assignmentRepository,
            StudentRepository studentRepository,
            FileRepository fileRepository
    ) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentRepository = studentRepository;
        this.fileRepository = fileRepository;
    }

    @Override
    @Transactional
    public SubmissionDto submitAssignment(Long assignmentId, Long studentId, SubmissionCreateDto submission) {
        AssignmentEntity assignment = findAssignment(assignmentId);
        StudentEntity student = findStudent(studentId);
        FileEntity file = findFile(submission.fileId());

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
    public List<SubmissionDto> getSubmittedByStudent(Long studentId) {
        StudentEntity student = findStudent(studentId);
        return submissionRepository.findByStudentIdOrderByIdAsc(student.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionDto> getAssignmentSubmissions(Long assignmentId) {
        AssignmentEntity assignment = findAssignment(assignmentId);
        return submissionRepository.findByAssignmentIdOrderByIdAsc(assignment.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionDto getSubmissionById(Long submissionId) {
        return toDto(findSubmission(submissionId));
    }

    private AssignmentEntity findAssignment(Long assignmentId) {
        return assignmentRepository.findWithCourseById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    }

    private StudentEntity findStudent(Long studentId) {
        if (studentId == null) {
            throw new BadRequestException("studentId is required");
        }
        return studentRepository.findById(studentId)
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
}
