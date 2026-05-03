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
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.user.dao.entity.StudentEntity;
import com.istp.lab1.user.dao.entity.TeacherEntity;
import com.istp.lab1.user.dao.repository.StudentRepository;
import com.istp.lab1.user.dao.repository.TeacherRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseServiceImpl implements CourseService {

    private static final String ENROLLED_STATUS = "ENROLLED";
    private static final List<CourseStatus> DEFAULT_STATUSES = List.of(CourseStatus.ACTIVE, CourseStatus.PLANNED);
    private static final List<EnrollmentStatus> MY_ENROLLMENT_STATUSES = List.of(
            EnrollmentStatus.ACTIVE,
            EnrollmentStatus.COMPLETED
    );

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public CourseServiceImpl(
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            TeacherRepository teacherRepository,
            StudentRepository studentRepository
    ) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getCourses(String statuses) {
        List<CourseStatus> resolvedStatuses = resolveStatuses(statuses);

        return courseRepository.findByStatusInOrderByIdAsc(resolvedStatuses).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long courseId) {
        return toDto(findCourse(courseId));
    }

    @Override
    @Transactional
    public CourseDto createCourse(CourseCreateDto course) {
        TeacherEntity teacher = findTeacher(course.teacherId());
        CourseEntity savedCourse = courseRepository.save(new CourseEntity(
                course.title(),
                course.description(),
                teacher,
                CourseStatus.ACTIVE
        ));

        return toDto(savedCourse);
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long courseId, CourseSaveDto course) {
        CourseEntity existingCourse = findCourse(courseId);

        existingCourse.updateDetails(course.title(), course.description());

        return toDto(existingCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        CourseEntity course = findCourse(courseId);

        course.cancel();
    }

    @Override
    @Transactional
    public EnrollmentDto enrollInCourse(Long studentId, Long courseId) {
        StudentEntity student = findStudent(studentId);
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
    public List<CourseDto> getEnrolledCourses(Long studentId) {
        StudentEntity student = findStudent(studentId);
        return enrollmentRepository.findByStudentIdAndStatusIn(student.getId(), MY_ENROLLMENT_STATUSES).stream()
                .map(EnrollmentEntity::getCourse)
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getOwnedCourses(Long teacherId) {
        TeacherEntity teacher = findTeacher(teacherId);
        return courseRepository.findByTeacherIdAndStatusNotOrderByIdAsc(teacher.getId(), CourseStatus.CANCELLED).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseStudentDto> getCourseStudents(Long courseId) {
        CourseEntity course = findCourse(courseId);

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

    private TeacherEntity findTeacher(Long teacherId) {
        if (teacherId == null) {
            throw new BadRequestException("teacherId is required");
        }
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
    }

    private StudentEntity findStudent(Long studentId) {
        if (studentId == null) {
            throw new BadRequestException("studentId is required");
        }
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private CourseDto toDto(CourseEntity course) {
        return new CourseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getTeacher().getId(),
                course.getTeacher().getUser().getFullName(),
                course.getStatus().name()
        );
    }
}
