package com.istp.lab1.submission.dao.repository;

import com.istp.lab1.statistics.service.dto.CourseAverageGradeDto;
import com.istp.lab1.submission.dao.entity.SubmissionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    boolean existsByFile_Id(Long fileId);

    @EntityGraph(attributePaths = {"assignment", "assignment.course", "student", "student.user", "file"})
    List<SubmissionEntity> findByStudentIdOrderByIdAsc(Long studentId);

    @EntityGraph(attributePaths = {"assignment", "assignment.course", "student", "student.user", "file"})
    List<SubmissionEntity> findByAssignmentIdOrderByIdAsc(Long assignmentId);

    @EntityGraph(attributePaths = {"assignment", "assignment.course", "student", "student.user", "file"})
    List<SubmissionEntity> findByStudentIdAndScoreIsNotNullOrderByIdAsc(Long studentId);

    @EntityGraph(attributePaths = {"assignment", "assignment.course", "student", "student.user", "file"})
    @Query("""
            select submission
            from SubmissionEntity submission
            where submission.assignment.course.teacher.user.id = :teacherUserId
              and submission.score is not null
            order by submission.assignment.course.id asc, submission.assignment.id asc, submission.id asc
            """)
    List<SubmissionEntity> findGradedByTeacherUserId(@Param("teacherUserId") Long teacherUserId);

    @Query("""
            select new com.istp.lab1.statistics.service.dto.CourseAverageGradeDto(
                submission.assignment.course.id,
                submission.assignment.course.title,
                avg(submission.score)
            )
            from SubmissionEntity submission
            where submission.score is not null
            group by submission.assignment.course.id, submission.assignment.course.title
            order by submission.assignment.course.id asc
            """)
    List<CourseAverageGradeDto> findAverageGradesByCourse();

    @EntityGraph(attributePaths = {"assignment", "assignment.course", "student", "student.user", "file"})
    @Query("select submission from SubmissionEntity submission where submission.id = :id")
    Optional<SubmissionEntity> findWithDetailsById(@Param("id") Long id);
}
