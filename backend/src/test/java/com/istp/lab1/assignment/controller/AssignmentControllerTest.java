package com.istp.lab1.assignment.controller;

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

import com.istp.lab1.assignment.controller.mapper.AssignmentMapper;
import com.istp.lab1.assignment.service.api.AssignmentService;
import com.istp.lab1.assignment.service.dto.AssignmentDto;
import com.istp.lab1.assignment.service.dto.AssignmentSaveDto;
import com.istp.lab1.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AssignmentController.class)
@Import(AssignmentMapper.class)
class AssignmentControllerTest {

    private static final String API_URL = "/api/v1";
    private static final LocalDateTime DEADLINE = LocalDateTime.of(2026, 5, 1, 23, 59);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssignmentService assignmentService;

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
                .andExpect(jsonPath("$[0].courseId").value(1))
                .andExpect(jsonPath("$[0].title").value("ER Model Design"))
                .andExpect(jsonPath("$[0].description").value("Design an ER diagram"))
                .andExpect(jsonPath("$[0].deadline").value("2026-05-01T23:59:00"));

        verify(assignmentService).getCourseAssignments(1L);
    }

    @Test
    void getAssignmentByIdReturnsMappedAssignment() throws Exception {
        when(assignmentService.getAssignmentById(10L)).thenReturn(new AssignmentDto(
                10L,
                1L,
                "ER Model Design",
                "Design an ER diagram",
                DEADLINE
        ));

        mockMvc.perform(get(API_URL + "/assignments/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.deadline").value("2026-05-01T23:59:00"));

        verify(assignmentService).getAssignmentById(10L);
    }

    @Test
    void getAssignmentByIdReturnsNotFound() throws Exception {
        when(assignmentService.getAssignmentById(404L))
                .thenThrow(new ResourceNotFoundException("Assignment not found"));

        mockMvc.perform(get(API_URL + "/assignments/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Assignment not found"))
                .andExpect(jsonPath("$.path").value(API_URL + "/assignments/404"));
    }

    @Test
    void createAssignmentReturnsCreatedAssignment() throws Exception {
        AssignmentSaveDto saveDto = new AssignmentSaveDto(
                "ER Model Design",
                "Design an ER diagram",
                DEADLINE
        );
        when(assignmentService.createAssignment(1L, saveDto)).thenReturn(new AssignmentDto(
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
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.deadline").value("2026-05-01T23:59:00"));

        verify(assignmentService).createAssignment(1L, saveDto);
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
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("title: must not be blank"));

        verifyNoInteractions(assignmentService);
    }

    @Test
    void updateAssignmentReturnsUpdatedAssignment() throws Exception {
        AssignmentSaveDto saveDto = new AssignmentSaveDto(
                "Updated title",
                "Updated description",
                DEADLINE
        );
        when(assignmentService.updateAssignment(10L, saveDto)).thenReturn(new AssignmentDto(
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
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(assignmentService).updateAssignment(10L, saveDto);
    }

    @Test
    void deleteAssignmentReturnsContractMessage() throws Exception {
        mockMvc.perform(delete(API_URL + "/assignments/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Assignment deleted successfully"));

        verify(assignmentService).deleteAssignment(10L);
    }
}
