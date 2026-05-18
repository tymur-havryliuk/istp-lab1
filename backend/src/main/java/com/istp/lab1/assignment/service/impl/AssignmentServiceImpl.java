package com.istp.lab1.assignment.service.impl;

import com.istp.lab1.assignment.dao.entity.AssignmentEntity;
import com.istp.lab1.assignment.dao.repository.AssignmentRepository;
import com.istp.lab1.assignment.service.api.AssignmentService;
import com.istp.lab1.assignment.service.dto.AssignmentDto;
import com.istp.lab1.assignment.service.dto.AssignmentSaveDto;
import com.istp.lab1.course.dao.entity.CourseEntity;
import com.istp.lab1.course.dao.repository.CourseRepository;
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
public class AssignmentServiceImpl implements AssignmentService {

    private static final int DEFAULT_MAX_SCORE = 100;
    private static final String TEACHER_ROLE_REQUIRED = "Teacher role is required";
    private static final String ASSIGNMENT_ACCESS_DENIED = "You do not have access to this assignment";

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final CurrentUserResolver currentUserResolver;

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentDto> getCourseAssignments(Long courseId) {
        CourseEntity course = findCourse(courseId);
        return assignmentRepository.findByCourseIdOrderByIdAsc(course.getId()).stream()
                .map(assignment -> new AssignmentDto(
                        assignment.getId(),
                        assignment.getCourse().getId(),
                        assignment.getTitle(),
                        assignment.getDescription(),
                        assignment.getDueDate()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentDto getAssignmentById(Long assignmentId) {
        AssignmentEntity assignment = findAssignment(assignmentId);
        return new AssignmentDto(
                assignment.getId(),
                assignment.getCourse().getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate()
        );
    }

    @Override
    @Transactional
    public AssignmentDto createAssignment(Long courseId, AssignmentSaveDto assignment) {
        CurrentUser currentUser = currentUser();
        CourseEntity course = findCourse(courseId);
        requireCourseOwner(currentUser, course);
        AssignmentEntity savedAssignment = assignmentRepository.save(new AssignmentEntity(
                course,
                assignment.title(),
                assignment.description(),
                assignment.deadline(),
                DEFAULT_MAX_SCORE
        ));

        return new AssignmentDto(
                savedAssignment.getId(),
                savedAssignment.getCourse().getId(),
                savedAssignment.getTitle(),
                savedAssignment.getDescription(),
                savedAssignment.getDueDate()
        );
    }

    @Override
    @Transactional
    public AssignmentDto updateAssignment(Long assignmentId, AssignmentSaveDto assignment) {
        CurrentUser currentUser = currentUser();
        AssignmentEntity existingAssignment = findAssignment(assignmentId);
        requireAssignmentOwner(currentUser, existingAssignment);
        existingAssignment.updateDetails(
                assignment.title(),
                assignment.description(),
                assignment.deadline()
        );

        return new AssignmentDto(
                existingAssignment.getId(),
                existingAssignment.getCourse().getId(),
                existingAssignment.getTitle(),
                existingAssignment.getDescription(),
                existingAssignment.getDueDate()
        );
    }

    @Override
    @Transactional
    public void deleteAssignment(Long assignmentId) {
        CurrentUser currentUser = currentUser();
        AssignmentEntity assignment = findAssignment(assignmentId);
        requireAssignmentOwner(currentUser, assignment);
        assignmentRepository.delete(assignment);
    }

    private CourseEntity findCourse(Long courseId) {
        return courseRepository.findWithTeacherById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private AssignmentEntity findAssignment(Long assignmentId) {
        return assignmentRepository.findWithCourseById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    }

    private void requireCourseOwner(CurrentUser currentUser, CourseEntity course) {
        requireTeacher(currentUser);
        if (!course.getTeacher().getUser().getId().equals(currentUser.id())) {
            throw new ForbiddenException(ASSIGNMENT_ACCESS_DENIED);
        }
    }

    private void requireAssignmentOwner(CurrentUser currentUser, AssignmentEntity assignment) {
        requireCourseOwner(currentUser, assignment.getCourse());
    }

    private void requireTeacher(CurrentUser currentUser) {
        if (currentUser.role() != UserRole.TEACHER) {
            throw new ForbiddenException(TEACHER_ROLE_REQUIRED);
        }
    }

    private CurrentUser currentUser() {
        return currentUserResolver.resolveCurrentUser();
    }

}
