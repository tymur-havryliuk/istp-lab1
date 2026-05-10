package com.istp.gateway.auth.service.impl;

import tools.jackson.databind.ObjectMapper;
import com.istp.gateway.auth.request.LoginRequest;
import com.istp.gateway.auth.request.RegisterRequest;
import com.istp.gateway.auth.response.AuthResponse;
import com.istp.gateway.auth.response.UserResponse;
import com.istp.gateway.auth.service.JwtService;
import com.istp.gateway.auth.service.api.AuthService;
import com.istp.gateway.exception.BadRequestException;
import com.istp.gateway.exception.UnauthorizedException;
import java.io.IOException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private RestClient restClient;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    @PostConstruct
    void init() {
        this.restClient = restClientBuilder.baseUrl(backendBaseUrl).build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UserResponse user = executeAuthRequest("/internal/auth/verify", request, "Backend auth verification failed");
        return new AuthResponse(jwtService.generateToken(user), user);
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        UserResponse user = executeAuthRequest("/internal/auth/register", request, "Backend auth registration failed");
        return new AuthResponse(jwtService.generateToken(user), user);
    }

    private UserResponse executeAuthRequest(String uri, Object requestBody, String failureMessage) {
        return restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange((_, clientResponse) -> {
                    try {
                        int status = clientResponse.getStatusCode().value();
                        byte[] body = StreamUtils.copyToByteArray(clientResponse.getBody());

                        if (status == 200 || status == 201) {
                            return objectMapper.readValue(body, UserResponse.class);
                        }
                        if (status == 400) {
                            throw new BadRequestException(extractMessage(body));
                        }
                        if (status == 401) {
                            throw new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE);
                        }
                        throw new IllegalStateException(failureMessage);
                    } catch (IOException exception) {
                        throw new IllegalStateException(failureMessage, exception);
                    }
                });
    }

    private String extractMessage(byte[] body) {
        if (body.length == 0) {
            return "Invalid request";
        }

        try {
            return objectMapper.readTree(body).path("message").asText("Invalid request");
        } catch (Exception exception) {
            return "Invalid request";
        }
    }
}
