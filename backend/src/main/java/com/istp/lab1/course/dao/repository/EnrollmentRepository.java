package com.istp.lab1.course.dao.repository;

import com.istp.lab1.course.dao.entity.EnrollmentEntity;
import com.istp.lab1.course.dao.entity.EnrollmentStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    @Query("""
            select case when count(enrollment) > 0 then true else false end
            from EnrollmentEntity enrollment
            where enrollment.student.id = :studentId
              and enrollment.course.id = :courseId
            """)
    boolean existsByStudentIdAndCourseId(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId
    );

    @Query("""
            select enrollment
            from EnrollmentEntity enrollment
            join fetch enrollment.course course
            join fetch course.teacher teacher
            join fetch teacher.user
            where enrollment.student.id = :studentId
              and enrollment.status in :statuses
            order by course.id asc
            """)
    List<EnrollmentEntity> findByStudentIdAndStatusIn(
            @Param("studentId") Long studentId,
            @Param("statuses") Collection<EnrollmentStatus> statuses
    );

    @Query("""
            select enrollment
            from EnrollmentEntity enrollment
            join fetch enrollment.student student
            join fetch student.user
            where enrollment.course.id = :courseId
              and enrollment.status in :statuses
            order by student.id asc
            """)
    List<EnrollmentEntity> findByCourseIdAndStatusIn(
            @Param("courseId") Long courseId,
            @Param("statuses") Collection<EnrollmentStatus> statuses
    );
}
