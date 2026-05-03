package com.istp.lab1.user.dao.repository;

import com.istp.lab1.user.dao.entity.StudentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    @EntityGraph(attributePaths = "user")
    @Query("select student from StudentEntity student where student.user.id = :userId")
    Optional<StudentEntity> findByUserId(@Param("userId") Long userId);
}
