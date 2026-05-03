package com.istp.lab1.assignment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.istp.lab1.assignment.controller.mapper.AssignmentMapperImpl;
import com.istp.lab1.assignment.service.api.AssignmentService;
import com.istp.lab1.assignment.service.dto.AssignmentDto;
import com.istp.lab1.assignment.service.dto.AssignmentSaveDto;
import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.security.CurrentUser;
import com.istp.lab1.security.CurrentUserResolver;
import com.istp.lab1.user.dao.entity.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AssignmentController.class)
@Import(AssignmentMapperImpl.class)
class AssignmentControllerTest {

    private static final String API_URL = "/api/v1";
    private static final LocalDateTime DEADLINE = LocalDateTime.of(2026, 5, 1, 23, 59);
    private static final CurrentUser TEACHER_USER = new CurrentUser(1L, "teacher@example.com", UserRole.TEACHER);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssignmentService assignmentService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    @Test
    void getCourseAssignmentsReturnsMappedAssignments() throws Exception {
        when(assignmentService.getCourseAssignments(1L)).thenReturn(List.of(new AssignmentDto(
                10L,
                1L,
                "ER Model Design",
                "Design an ER diagram",
                DEADLINE
        )));

        mockMvc.perform(get(API_URL + "/courses/1/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].deadline").value("2026-05-01T23:59:00"));

        verify(assignmentService).getCourseAssignments(1L);
    }

    @Test
    void createAssignmentReturnsCreatedAssignment() throws Exception {
        AssignmentSaveDto saveDto = new AssignmentSaveDto("ER Model Design", "Design an ER diagram", DEADLINE);
        when(currentUserResolver.resolve(any())).thenReturn(TEACHER_USER);
        when(assignmentService.createAssignment(TEACHER_USER, 1L, saveDto)).thenReturn(new AssignmentDto(
                10L,
                1L,
                "ER Model Design",
                "Design an ER diagram",
                DEADLINE
        ));

        mockMvc.perform(post(API_URL + "/courses/1/assignments")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "ER Model Design",
                                  "description": "Design an ER diagram",
                                  "deadline": "2026-05-01T23:59:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        verify(assignmentService).createAssignment(TEACHER_USER, 1L, saveDto);
    }

    @Test
    void updateAssignmentReturnsUpdatedAssignment() throws Exception {
        AssignmentSaveDto saveDto = new AssignmentSaveDto("Updated title", "Updated description", DEADLINE);
        when(currentUserResolver.resolve(any())).thenReturn(TEACHER_USER);
        when(assignmentService.updateAssignment(TEACHER_USER, 10L, saveDto)).thenReturn(new AssignmentDto(
                10L,
                1L,
                "Updated title",
                "Updated description",
                DEADLINE
        ));

        mockMvc.perform(put(API_URL + "/assignments/10")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title",
                                  "description": "Updated description",
                                  "deadline": "2026-05-01T23:59:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));

        verify(assignmentService).updateAssignment(TEACHER_USER, 10L, saveDto);
    }

    @Test
    void deleteAssignmentReturnsContractMessage() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(TEACHER_USER);

        mockMvc.perform(delete(API_URL + "/assignments/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Assignment deleted successfully"));

        verify(assignmentService).deleteAssignment(TEACHER_USER, 10L);
    }

    @Test
    void createAssignmentWithInvalidBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post(API_URL + "/courses/1/assignments")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "Design an ER diagram",
                                  "deadline": "2026-05-01T23:59:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("title: must not be blank"));

        verifyNoInteractions(assignmentService);
    }

    @Test
    void getAssignmentByIdReturnsNotFound() throws Exception {
        when(assignmentService.getAssignmentById(404L))
                .thenThrow(new ResourceNotFoundException("Assignment not found"));

        mockMvc.perform(get(API_URL + "/assignments/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Assignment not found"));
    }
}
