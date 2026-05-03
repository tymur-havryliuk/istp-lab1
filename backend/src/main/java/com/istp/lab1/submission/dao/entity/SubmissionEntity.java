package com.istp.lab1.submission.dao.entity;

import com.istp.lab1.assignment.dao.entity.AssignmentEntity;
import com.istp.lab1.file.dao.entity.FileEntity;
import com.istp.lab1.user.dao.entity.StudentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "submissions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private AssignmentEntity assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SubmissionStatus status;

    @Column(name = "score")
    private Integer score;

    @Column(name = "feedback", columnDefinition = "text")
    private String feedback;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    public SubmissionEntity(
            AssignmentEntity assignment,
            StudentEntity student,
            LocalDateTime submissionDate,
            FileEntity file,
            String comment,
            SubmissionStatus status
    ) {
        this.assignment = assignment;
        this.student = student;
        this.submissionDate = submissionDate;
        this.file = file;
        this.comment = comment;
        this.status = status;
    }

    public void grade(Integer score, String feedback, LocalDateTime gradedAt) {
        this.score = score;
        this.feedback = feedback;
        this.gradedAt = gradedAt;
        this.status = SubmissionStatus.REVIEWED;
    }
}
