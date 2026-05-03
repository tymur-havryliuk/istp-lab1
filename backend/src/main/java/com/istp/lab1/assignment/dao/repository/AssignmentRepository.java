package com.istp.lab1.assignment.dao.repository;

import com.istp.lab1.assignment.dao.entity.AssignmentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {

    @EntityGraph(attributePaths = "course")
    List<AssignmentEntity> findByCourseIdOrderByIdAsc(Long courseId);

    @EntityGraph(attributePaths = "course")
    @Query("select assignment from AssignmentEntity assignment where assignment.id = :id")
    Optional<AssignmentEntity> findWithCourseById(@Param("id") Long id);
}
