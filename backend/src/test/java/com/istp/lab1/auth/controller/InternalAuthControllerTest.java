package com.istp.lab1.auth.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.istp.lab1.auth.controller.response.InternalUserResponse;
import com.istp.lab1.auth.service.api.InternalAuthService;
import com.istp.lab1.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalAuthController.class)
class InternalAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InternalAuthService internalAuthService;

    @Test
    void verifyReturnsInternalUser() throws Exception {
        when(internalAuthService.verifyCredentials("teacher@example.com", "password123"))
                .thenReturn(new InternalUserResponse(1001L, "Gateway Teacher", "teacher@example.com", "TEACHER"));

        mockMvc.perform(post("/internal/auth/verify")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "teacher@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.role").value("TEACHER"));

        verify(internalAuthService).verifyCredentials("teacher@example.com", "password123");
    }

    @Test
    void verifyReturnsUnauthorizedForBadCredentials() throws Exception {
        when(internalAuthService.verifyCredentials("teacher@example.com", "wrong"))
                .thenThrow(new UnauthorizedException("Invalid email or password"));

        mockMvc.perform(post("/internal/auth/verify")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "teacher@example.com",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void verifyRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/internal/auth/verify")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(internalAuthService);
    }
}
