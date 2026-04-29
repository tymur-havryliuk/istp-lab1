package com.istp.lab1.course.dao.repository;

import com.istp.lab1.course.dao.entity.CourseEntity;
import com.istp.lab1.course.dao.entity.CourseStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    @EntityGraph(attributePaths = {"teacher", "teacher.user"})
    List<CourseEntity> findByStatusInOrderByIdAsc(Collection<CourseStatus> statuses);

    @EntityGraph(attributePaths = {"teacher", "teacher.user"})
    @Query("select course from CourseEntity course where course.id = :id")
    Optional<CourseEntity> findWithTeacherById(@Param("id") Long id);
}
