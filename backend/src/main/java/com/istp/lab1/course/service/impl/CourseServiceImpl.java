package com.istp.lab1.course.service.impl;

import com.istp.lab1.course.dao.entity.CourseEntity;
import com.istp.lab1.course.dao.entity.CourseStatus;
import com.istp.lab1.course.dao.entity.EnrollmentEntity;
import com.istp.lab1.course.dao.entity.EnrollmentStatus;
import com.istp.lab1.course.dao.repository.CourseRepository;
import com.istp.lab1.course.dao.repository.EnrollmentRepository;
import com.istp.lab1.course.service.api.CourseService;
import com.istp.lab1.course.service.dto.CourseCreateDto;
import com.istp.lab1.course.service.dto.CourseDto;
import com.istp.lab1.course.service.dto.CourseSaveDto;
import com.istp.lab1.course.service.dto.CourseStudentDto;
import com.istp.lab1.course.service.dto.EnrollmentDto;
import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.ForbiddenException;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.security.CurrentUserResolver;
import com.istp.lab1.user.dao.entity.StudentEntity;
import com.istp.lab1.user.dao.entity.TeacherEntity;
import com.istp.lab1.user.dao.entity.UserRole;
import com.istp.lab1.user.dao.repository.StudentRepository;
import com.istp.lab1.user.dao.repository.TeacherRepository;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final String ENROLLED_STATUS = "ENROLLED";
    private static final List<CourseStatus> DEFAULT_STATUSES = List.of(CourseStatus.ACTIVE, CourseStatus.PLANNED);
    private static final List<EnrollmentStatus> MY_ENROLLMENT_STATUSES = List.of(
            EnrollmentStatus.ACTIVE,
            EnrollmentStatus.COMPLETED
    );
    private static final String TEACHER_ROLE_REQUIRED = "Teacher role is required";
    private static final String STUDENT_ROLE_REQUIRED = "Student role is required";
    private static final String COURSE_ACCESS_DENIED = "You do not have access to this course";

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CurrentUserResolver currentUserResolver;

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getCourses(String statuses) {
        List<CourseStatus> resolvedStatuses = resolveStatuses(statuses);

        return courseRepository.findByStatusInOrderByIdAsc(resolvedStatuses).stream()
                .map(course -> new CourseDto(
                        course.getId(),
                        course.getTitle(),
                        course.getDescription(),
                        course.getTeacher().getId(),
                        course.getTeacher().getUser().getFullName(),
                        course.getStatus().name()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long courseId) {
        CourseEntity course = findCourse(courseId);
        return new CourseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getTeacher().getId(),
                course.getTeacher().getUser().getFullName(),
                course.getStatus().name()
        );
    }

    @Override
    @Transactional
    public CourseDto createCourse(CourseCreateDto course) {
        CurrentUser currentUser = currentUserResolver.resolveCurrentUser();
        TeacherEntity teacher = findTeacherForCurrentUser(currentUser);
        CourseEntity savedCourse = courseRepository.save(new CourseEntity(
                course.title(),
                course.description(),
                teacher,
                CourseStatus.ACTIVE
        ));

        return new CourseDto(
                savedCourse.getId(),
                savedCourse.getTitle(),
                savedCourse.getDescription(),
                savedCourse.getTeacher().getId(),
                savedCourse.getTeacher().getUser().getFullName(),
                savedCourse.getStatus().name()
        );
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long courseId, CourseSaveDto course) {
        CurrentUser currentUser = currentUserResolver.resolveCurrentUser();
        CourseEntity existingCourse = findCourse(courseId);
        requireCourseOwner(currentUser, existingCourse);

        existingCourse.updateDetails(course.title(), course.description());

        return new CourseDto(
                existingCourse.getId(),
                existingCourse.getTitle(),
                existingCourse.getDescription(),
                existingCourse.getTeacher().getId(),
                existingCourse.getTeacher().getUser().getFullName(),
                existingCourse.getStatus().name()
        );
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        CurrentUser currentUser = currentUserResolver.resolveCurrentUser();
        CourseEntity course = findCourse(courseId);
        requireCourseOwner(currentUser, course);

        course.cancel();
    }

    @Override
    @Transactional
    public EnrollmentDto enrollInCourse(Long courseId) {
        CurrentUser currentUser = currentUserResolver.resolveCurrentUser();
        StudentEntity student = findStudentForCurrentUser(currentUser);
        CourseEntity course = findCourse(courseId);

        if (!List.of(CourseStatus.ACTIVE, CourseStatus.PLANNED).contains(course.getStatus())) {
            throw new BadRequestException("Course is not available for enrollment");
        }
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new BadRequestException("Student is already enrolled in course");
        }

        enrollmentRepository.save(new EnrollmentEntity(student, course, EnrollmentStatus.ACTIVE));

        return new EnrollmentDto(course.getId(), student.getId(), ENROLLED_STATUS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getEnrolledCourses() {
        CurrentUser currentUser = currentUserResolver.resolveCurrentUser();
        StudentEntity student = findStudentForCurrentUser(currentUser);
        return enrollmentRepository.findByStudentIdAndStatusIn(student.getId(), MY_ENROLLMENT_STATUSES).stream()
                .map(EnrollmentEntity::getCourse)
                .map(course -> new CourseDto(
                        course.getId(),
                        course.getTitle(),
                        course.getDescription(),
                        course.getTeacher().getId(),
                        course.getTeacher().getUser().getFullName(),
                        course.getStatus().name()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getOwnedCourses() {
        CurrentUser currentUser = currentUserResolver.resolveCurrentUser();
        TeacherEntity teacher = findTeacherForCurrentUser(currentUser);
        return courseRepository.findByTeacherIdAndStatusNotOrderByIdAsc(teacher.getId(), CourseStatus.CANCELLED).stream()
                .map(course -> new CourseDto(
                        course.getId(),
                        course.getTitle(),
                        course.getDescription(),
                        course.getTeacher().getId(),
                        course.getTeacher().getUser().getFullName(),
                        course.getStatus().name()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseStudentDto> getCourseStudents(Long courseId) {
        CurrentUser currentUser = currentUserResolver.resolveCurrentUser();
        CourseEntity course = findCourse(courseId);
        requireCourseOwner(currentUser, course);

        return enrollmentRepository.findByCourseIdAndStatusIn(course.getId(), MY_ENROLLMENT_STATUSES).stream()
                .map(EnrollmentEntity::getStudent)
                .map(student -> new CourseStudentDto(
                        student.getId(),
                        student.getUser().getFullName(),
                        student.getUser().getEmail(),
                        student.getUser().getRole().name()
                ))
                .toList();
    }

    private List<CourseStatus> resolveStatuses(String statuses) {
        if (statuses == null) {
            return DEFAULT_STATUSES;
        }
        if (statuses.isBlank()) {
            throw new BadRequestException("statuses must not be empty");
        }

        try {
            return Arrays.stream(statuses.split(","))
                    .map(String::trim)
                    .peek(this::requireNotEmptyStatus)
                    .map(CourseStatus::fromValue)
                    .distinct()
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }

    private void requireNotEmptyStatus(String status) {
        if (status.isEmpty()) {
            throw new IllegalArgumentException("statuses must not contain empty values");
        }
    }

    private CourseEntity findCourse(Long courseId) {
        return courseRepository.findWithTeacherById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private TeacherEntity findTeacherForCurrentUser(CurrentUser currentUser) {
        requireRole(currentUser, UserRole.TEACHER, TEACHER_ROLE_REQUIRED);
        return teacherRepository.findByUserId(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
    }

    private StudentEntity findStudentForCurrentUser(CurrentUser currentUser) {
        requireRole(currentUser, UserRole.STUDENT, STUDENT_ROLE_REQUIRED);
        return studentRepository.findByUserId(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private void requireCourseOwner(CurrentUser currentUser, CourseEntity course) {
        requireRole(currentUser, UserRole.TEACHER, TEACHER_ROLE_REQUIRED);
        if (!course.getTeacher().getUser().getId().equals(currentUser.id())) {
            throw new ForbiddenException(COURSE_ACCESS_DENIED);
        }
    }

    private void requireRole(CurrentUser currentUser, UserRole expectedRole, String message) {
        if (currentUser.role() != expectedRole) {
            throw new ForbiddenException(message);
        }
    }

}
