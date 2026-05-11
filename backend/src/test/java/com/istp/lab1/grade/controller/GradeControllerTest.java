package com.istp.lab1.grade.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.istp.lab1.exception.ResourceNotFoundException;
import com.istp.lab1.grade.controller.mapper.GradeMapperImpl;
import com.istp.lab1.grade.service.api.GradeService;
import com.istp.lab1.grade.service.dto.GradeDto;
import com.istp.lab1.grade.service.dto.GradeSaveDto;
import com.istp.lab1.submission.controller.mapper.SubmissionMapperImpl;
import com.istp.lab1.submission.service.dto.SubmissionDto;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GradeController.class)
@Import({GradeMapperImpl.class, SubmissionMapperImpl.class})
class GradeControllerTest {

    private static final String API_URL = "/api/v1";
    private static final LocalDateTime SUBMITTED_AT = LocalDateTime.of(2026, 4, 20, 18, 30);
    private static final LocalDateTime GRADED_AT = LocalDateTime.of(2026, 4, 21, 12, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GradeService gradeService;

    @Test
    void gradeSubmissionReturnsUpdatedSubmission() throws Exception {
        GradeSaveDto grade = new GradeSaveDto(95, "Good work");
        when(gradeService.gradeSubmission(100L, grade)).thenReturn(new SubmissionDto(
                100L,
                1L,
                2L,
                "Lin Student",
                "Done",
                10L,
                SUBMITTED_AT,
                95,
                "Good work"
        ));

        mockMvc.perform(post(API_URL + "/submissions/100/grade")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "grade": 95,
                                  "feedback": "Good work"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.grade").value(95));

        verify(gradeService).gradeSubmission(100L, grade);
    }

    @Test
    void getStudentGradesReturnsGrades() throws Exception {
        when(gradeService.getStudentGrades()).thenReturn(List.of(new GradeDto(
                100L,
                1L,
                "ER Model Design",
                3L,
                "Database Systems",
                95,
                "Good work",
                GRADED_AT
        )));

        mockMvc.perform(get(API_URL + "/grades/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].submissionId").value(100))
                .andExpect(jsonPath("$[0].gradedAt").value("2026-04-21T12:00:00"));

        verify(gradeService).getStudentGrades();
    }

    @Test
    void gradeSubmissionWithInvalidBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post(API_URL + "/submissions/100/grade")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "feedback": "Good work"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("grade: must not be null"));

        verifyNoInteractions(gradeService);
    }

    @Test
    void gradeSubmissionReturnsNotFound() throws Exception {
        when(gradeService.gradeSubmission(404L, new GradeSaveDto(95, "Good work")))
                .thenThrow(new ResourceNotFoundException("Submission not found"));

        mockMvc.perform(post(API_URL + "/submissions/404/grade")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "grade": 95,
                                  "feedback": "Good work"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Submission not found"));
    }
}
