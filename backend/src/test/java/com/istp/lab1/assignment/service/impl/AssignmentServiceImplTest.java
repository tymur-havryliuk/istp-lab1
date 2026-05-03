package com.istp.lab1.assignment.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.istp.lab1.assignment.dao.entity.AssignmentEntity;
import com.istp.lab1.assignment.dao.repository.AssignmentRepository;
import com.istp.lab1.assignment.service.dto.AssignmentDto;
import com.istp.lab1.assignment.service.dto.AssignmentSaveDto;
import com.istp.lab1.course.dao.entity.CourseEntity;
import com.istp.lab1.course.dao.entity.CourseStatus;
import com.istp.lab1.course.dao.repository.CourseRepository;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.user.dao.entity.TeacherEntity;
import com.istp.lab1.user.dao.entity.UserEntity;
import com.istp.lab1.user.dao.entity.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    private static final LocalDateTime DEADLINE = LocalDateTime.of(2026, 5, 1, 23, 59);

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    @Test
    void getCourseAssignmentsReturnsAssignmentsForExistingCourse() {
        CourseEntity course = course(1L);
        AssignmentEntity assignment = assignment(10L, course, "ER Model Design", "Design an ER diagram", DEADLINE, 100);
        when(courseRepository.findWithTeacherById(1L)).thenReturn(Optional.of(course));
        when(assignmentRepository.findByCourseIdOrderByIdAsc(1L)).thenReturn(List.of(assignment));

        List<AssignmentDto> result = assignmentService.getCourseAssignments(1L);

        assertThat(result).containsExactly(new AssignmentDto(
                10L,
                1L,
                "ER Model Design",
                "Design an ER diagram",
                DEADLINE
        ));
    }

    @Test
    void getCourseAssignmentsThrowsWhenCourseMissing() {
        when(courseRepository.findWithTeacherById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.getCourseAssignments(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course not found");
    }

    @Test
    void getAssignmentByIdReturnsAssignment() {
        CourseEntity course = course(1L);
        AssignmentEntity assignment = assignment(10L, course, "ER Model Design", "Design an ER diagram", DEADLINE, 100);
        when(assignmentRepository.findWithCourseById(10L)).thenReturn(Optional.of(assignment));

        AssignmentDto result = assignmentService.getAssignmentById(10L);

        assertThat(result).isEqualTo(new AssignmentDto(
                10L,
                1L,
                "ER Model Design",
                "Design an ER diagram",
                DEADLINE
        ));
    }

    @Test
    void getAssignmentByIdThrowsWhenMissing() {
        when(assignmentRepository.findWithCourseById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.getAssignmentById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Assignment not found");
    }

    @Test
    void createAssignmentUsesCourseAndDefaultMaxScore() {
        CourseEntity course = course(1L);
        when(courseRepository.findWithTeacherById(1L)).thenReturn(Optional.of(course));
        when(assignmentRepository.save(any(AssignmentEntity.class))).thenAnswer(invocation -> {
            AssignmentEntity savedAssignment = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedAssignment, "id", 10L);
            return savedAssignment;
        });

        AssignmentDto result = assignmentService.createAssignment(1L, new AssignmentSaveDto(
                "ER Model Design",
                "Design an ER diagram",
                DEADLINE
        ));

        assertThat(result).isEqualTo(new AssignmentDto(
                10L,
                1L,
                "ER Model Design",
                "Design an ER diagram",
                DEADLINE
        ));

        ArgumentCaptor<AssignmentEntity> assignmentCaptor = ArgumentCaptor.forClass(AssignmentEntity.class);
        verify(assignmentRepository).save(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue().getCourse()).isSameAs(course);
        assertThat(assignmentCaptor.getValue().getMaxScore()).isEqualTo(100);
    }

    @Test
    void createAssignmentThrowsWhenCourseMissing() {
        when(courseRepository.findWithTeacherById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.createAssignment(404L, new AssignmentSaveDto(
                "ER Model Design",
                "Design an ER diagram",
                DEADLINE
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course not found");
    }

    @Test
    void updateAssignmentChangesDetails() {
        CourseEntity course = course(1L);
        AssignmentEntity assignment = assignment(
                10L,
                course,
                "Old title",
                "Old description",
                DEADLINE.minusDays(1),
                100
        );
        when(assignmentRepository.findWithCourseById(10L)).thenReturn(Optional.of(assignment));

        AssignmentDto result = assignmentService.updateAssignment(10L, new AssignmentSaveDto(
                "Updated title",
                "Updated description",
                DEADLINE
        ));

        assertThat(result.title()).isEqualTo("Updated title");
        assertThat(result.description()).isEqualTo("Updated description");
        assertThat(result.deadline()).isEqualTo(DEADLINE);
        assertThat(assignment.getMaxScore()).isEqualTo(100);
    }

    @Test
    void deleteAssignmentDeletesExistingAssignment() {
        CourseEntity course = course(1L);
        AssignmentEntity assignment = assignment(10L, course, "ER Model Design", "Design an ER diagram", DEADLINE, 100);
        when(assignmentRepository.findWithCourseById(10L)).thenReturn(Optional.of(assignment));

        assignmentService.deleteAssignment(10L);

        verify(assignmentRepository).delete(assignment);
    }

    private CourseEntity course(Long id) {
        UserEntity user = org.mockito.Mockito.mock(UserEntity.class);
        org.mockito.Mockito.lenient().when(user.getId()).thenReturn(1L);
        org.mockito.Mockito.lenient().when(user.getRole()).thenReturn(UserRole.TEACHER);
        org.mockito.Mockito.lenient().when(user.getFullName()).thenReturn("Ada Teacher");

        TeacherEntity teacher = org.mockito.Mockito.mock(TeacherEntity.class);
        org.mockito.Mockito.lenient().when(teacher.getId()).thenReturn(1L);
        org.mockito.Mockito.lenient().when(teacher.getUser()).thenReturn(user);

        CourseEntity course = new CourseEntity("Java Basics", "Intro course", teacher, CourseStatus.ACTIVE);
        ReflectionTestUtils.setField(course, "id", id);
        return course;
    }

    private AssignmentEntity assignment(
            Long id,
            CourseEntity course,
            String title,
            String description,
            LocalDateTime dueDate,
            Integer maxScore
    ) {
        AssignmentEntity assignment = new AssignmentEntity(course, title, description, dueDate, maxScore);
        ReflectionTestUtils.setField(assignment, "id", id);
        return assignment;
    }
}
