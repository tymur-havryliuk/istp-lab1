package com.istp.lab1.course.service.impl;

import com.istp.lab1.course.dao.entity.CourseEntity;
import com.istp.lab1.course.dao.entity.CourseStatus;
import com.istp.lab1.course.dao.repository.CourseRepository;
import com.istp.lab1.course.service.api.CourseService;
import com.istp.lab1.course.service.dto.CourseDto;
import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseServiceImpl implements CourseService {

    private static final List<CourseStatus> DEFAULT_STATUSES = List.of(CourseStatus.ACTIVE, CourseStatus.PLANNED);

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getCourses(List<String> statuses) {
        List<CourseStatus> resolvedStatuses = resolveStatuses(statuses);

        return courseRepository.findByStatusInOrderByIdAsc(resolvedStatuses).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long courseId) {
        return courseRepository.findWithTeacherById(courseId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private List<CourseStatus> resolveStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return DEFAULT_STATUSES;
        }

        try {
            return statuses.stream()
                    .map(CourseStatus::fromValue)
                    .distinct()
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }

    private CourseDto toDto(CourseEntity course) {
        return new CourseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getTeacher().getId(),
                course.getTeacher().getUser().getFullName()
        );
    }
}
