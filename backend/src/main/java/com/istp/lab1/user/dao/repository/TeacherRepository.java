package com.istp.lab1.user.dao.repository;

import com.istp.lab1.user.dao.entity.TeacherEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {

    @EntityGraph(attributePaths = "user")
    @Query("select teacher from TeacherEntity teacher where teacher.user.id = :userId")
    Optional<TeacherEntity> findByUserId(@Param("userId") Long userId);
}
