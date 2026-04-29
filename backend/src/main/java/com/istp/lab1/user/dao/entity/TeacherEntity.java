package com.istp.lab1.user.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "teachers")
public class TeacherEntity {

    @Id
    @Column(name = "teacher_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    protected TeacherEntity() {
    }

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }
}
