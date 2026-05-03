package com.istp.lab1.file.dao.repository;

import com.istp.lab1.file.dao.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
}
