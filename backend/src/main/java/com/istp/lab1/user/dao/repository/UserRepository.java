package com.istp.lab1.user.dao.repository;

import com.istp.lab1.user.dao.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
