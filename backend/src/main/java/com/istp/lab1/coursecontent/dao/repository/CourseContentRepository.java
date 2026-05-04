package com.istp.lab1.coursecontent.dao.repository;

import com.istp.lab1.coursecontent.dao.entity.CourseContentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseContentRepository extends JpaRepository<CourseContentEntity, Long> {

    List<CourseContentEntity> findByCourseIdOrderByPositionAscIdAsc(Long courseId);

    Optional<CourseContentEntity> findByIdAndCourseId(Long id, Long courseId);

    @Query("select coalesce(max(content.position), 0) from CourseContentEntity content where content.course.id = :courseId")
    int findMaxPositionByCourseId(@Param("courseId") Long courseId);
}
