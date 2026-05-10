package com.istp.gateway;

import com.istp.gateway.auth.request.LoginRequest;
import com.istp.gateway.auth.response.AuthResponse;
import com.istp.gateway.auth.response.UserResponse;
import com.istp.gateway.auth.service.JwtService;
import com.istp.gateway.auth.service.api.AuthService;
import com.istp.gateway.proxy.BackendClient;
import com.istp.gateway.security.CurrentUser;
import com.istp.gateway.security.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayApplicationTests {

    private static final String TEACHER_TOKEN = "teacher-token";
    private static final String STUDENT_TOKEN = "student-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private BackendClient backendClient;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void loginReturnsTokenAndDoesNotHitGenericProxy() throws Exception {
        when(authService.login(new LoginRequest("teacher@example.com", "password123")))
                .thenReturn(new AuthResponse(
                        TEACHER_TOKEN,
                        new UserResponse(1L, "Olena", "teacher@example.com", "TEACHER")
                ));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "teacher@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TEACHER_TOKEN))
                .andExpect(jsonPath("$.user.role").value("TEACHER"));

        verify(backendClient, never()).forward(any(), any(), any());
    }

    @Test
    void coursesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid token"));
    }

    @Test
    void protectedRouteWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Test",
                                  "description": "Test"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid token"));
    }

    @Test
    void studentCannotAccessTeacherRoute() throws Exception {
        when(jwtService.parseToken(STUDENT_TOKEN))
                .thenReturn(new CurrentUser(2L, "student@example.com", UserRole.STUDENT));

        mockMvc.perform(post("/api/v1/courses")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(STUDENT_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Test",
                                  "description": "Test"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void teacherCannotAccessStudentRoute() throws Exception {
        when(jwtService.parseToken(TEACHER_TOKEN))
                .thenReturn(new CurrentUser(1L, "teacher@example.com", UserRole.TEACHER));
        when(backendClient.forward(any(), any(), any()))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[]".getBytes()));

        mockMvc.perform(get("/api/v1/courses/enrolled")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(TEACHER_TOKEN)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void literalStudentRouteIsNotTreatedAsPublicCourseId() throws Exception {
        mockMvc.perform(get("/api/v1/courses/enrolled"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void gatewayOverwritesSpoofedHeaders() throws Exception {
        when(jwtService.parseToken(STUDENT_TOKEN))
                .thenReturn(new CurrentUser(2L, "student@example.com", UserRole.STUDENT));
        when(backendClient.forward(any(), any(), any()))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[]".getBytes()));

        mockMvc.perform(get("/api/v1/grades/student")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(STUDENT_TOKEN))
                        .header("X-User-Id", "999")
                        .header("X-User-Role", "TEACHER"))
                .andExpect(status().isOk());

        verify(backendClient).forward(any(), any(), any());
    }

    @Test
    void multipartUploadRouteWorksForStudentToken() throws Exception {
        when(jwtService.parseToken(STUDENT_TOKEN))
                .thenReturn(new CurrentUser(2L, "student@example.com", UserRole.STUDENT));
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        when(backendClient.forwardMultipart(any(), any(), any()))
                .thenReturn(new ResponseEntity<>("""
                        {"id":1,"fileName":"hello.txt","contentType":"text/plain","size":5,"url":"/api/v1/files/1"}
                        """.getBytes(), headers, HttpStatus.CREATED));

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(STUDENT_TOKEN)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("hello.txt"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }
}
