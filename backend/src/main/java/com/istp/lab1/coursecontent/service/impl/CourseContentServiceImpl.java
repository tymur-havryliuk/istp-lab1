package com.istp.lab1.coursecontent.service.impl;

import com.istp.lab1.course.dao.entity.CourseEntity;
import com.istp.lab1.course.dao.repository.CourseRepository;
import com.istp.lab1.coursecontent.dao.entity.CourseContentEntity;
import com.istp.lab1.coursecontent.dao.repository.CourseContentRepository;
import com.istp.lab1.coursecontent.service.api.CourseContentService;
import com.istp.lab1.coursecontent.service.dto.CourseContentCreateDto;
import com.istp.lab1.coursecontent.service.dto.CourseContentDto;
import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ForbiddenException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.security.CurrentUserResolver;
import com.istp.lab1.user.dao.entity.UserRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseContentServiceImpl implements CourseContentService {

    private static final String TEACHER_ROLE_REQUIRED = "Teacher role is required";
    private static final String COURSE_ACCESS_DENIED = "You do not have access to this course";
    private static final String DELIVERY_MODE_MESSAGE = "Provide either a room for offline content or a meetingLink for online content";

    private final CourseRepository courseRepository;
    private final CourseContentRepository courseContentRepository;
    private final CurrentUserResolver currentUserResolver;

    @Override
    @Transactional(readOnly = true)
    public List<CourseContentDto> getCourseContent(Long courseId) {
        requireCourseExists(courseId);
        return courseContentRepository.findByCourseIdOrderByPositionAscIdAsc(courseId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CourseContentDto createCourseContent(Long courseId, CourseContentCreateDto content) {
        CurrentUser currentUser = currentUser();
        CourseEntity course = findCourse(courseId);
        requireCourseOwner(currentUser, course);
        String room = normalizeNullable(content.room());
        String meetingLink = normalizeNullable(content.meetingLink());
        validateDeliveryDetails(room, meetingLink);

        CourseContentEntity savedContent = courseContentRepository.save(new CourseContentEntity(
                course,
                content.title(),
                content.description(),
                courseContentRepository.findMaxPositionByCourseId(courseId) + 1,
                content.scheduledAt(),
                room,
                meetingLink
        ));
        return toDto(savedContent);
    }

    @Override
    @Transactional
    public void deleteCourseContent(Long courseId, Long contentId) {
        CurrentUser currentUser = currentUser();
        CourseEntity course = findCourse(courseId);
        requireCourseOwner(currentUser, course);

        CourseContentEntity content = courseContentRepository.findByIdAndCourseId(contentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course content not found"));
        courseContentRepository.delete(content);
    }

    private void requireCourseExists(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found");
        }
    }

    private CourseEntity findCourse(Long courseId) {
        return courseRepository.findWithTeacherById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private void validateDeliveryDetails(String room, String meetingLink) {
        if ((room == null) == (meetingLink == null)) {
            throw new BadRequestException(DELIVERY_MODE_MESSAGE);
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private void requireCourseOwner(CurrentUser currentUser, CourseEntity course) {
        if (currentUser.role() != UserRole.TEACHER) {
            throw new ForbiddenException(TEACHER_ROLE_REQUIRED);
        }
        if (!course.getTeacher().getUser().getId().equals(currentUser.id())) {
            throw new ForbiddenException(COURSE_ACCESS_DENIED);
        }
    }

    private CurrentUser currentUser() {
        return currentUserResolver.resolveCurrentUser();
    }

    private CourseContentDto toDto(CourseContentEntity content) {
        return new CourseContentDto(
                content.getId(),
                content.getCourse().getId(),
                content.getTitle(),
                content.getDescription(),
                content.getPosition(),
                content.getScheduledAt(),
                content.getRoom(),
                content.getMeetingLink()
        );
    }
}
