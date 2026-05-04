package com.istp.lab1.coursecontent.dao.entity;

import com.istp.lab1.course.dao.entity.CourseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "course_contents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "room", length = 100)
    private String room;

    @Column(name = "meeting_link", length = 255)
    private String meetingLink;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public CourseContentEntity(
            CourseEntity course,
            String title,
            String description,
            Integer position,
            LocalDateTime scheduledAt,
            String room,
            String meetingLink
    ) {
        this.course = course;
        this.title = title;
        this.description = description;
        this.position = position;
        this.scheduledAt = scheduledAt;
        this.room = room;
        this.meetingLink = meetingLink;
    }
}
