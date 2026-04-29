package com.istp.lab1.user.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "user_id")
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    protected UserEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }
}
