package com.istp.gateway.auth.service;

import tools.jackson.databind.ObjectMapper;
import com.istp.gateway.auth.request.LoginRequest;
import com.istp.gateway.auth.request.RegisterRequest;
import com.istp.gateway.auth.response.AuthResponse;
import com.istp.gateway.auth.response.UserResponse;
import com.istp.gateway.exception.BadRequestException;
import com.istp.gateway.exception.UnauthorizedException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    public AuthResponse login(LoginRequest request) {
        UserResponse user = executeAuthRequest("/internal/auth/verify", request, "Backend auth verification failed");
        return new AuthResponse(jwtService.generateToken(user), user);
    }

    public AuthResponse register(RegisterRequest request) {
        UserResponse user = executeAuthRequest("/internal/auth/register", request, "Backend auth registration failed");
        return new AuthResponse(jwtService.generateToken(user), user);
    }

    private RestClient buildRestClient() {
        return restClientBuilder.baseUrl(backendBaseUrl).build();
    }

    private byte[] readBody(org.springframework.http.client.ClientHttpResponse response) throws IOException {
        if (response.getBody() == null) {
            return new byte[0];
        }
        return StreamUtils.copyToByteArray(response.getBody());
    }

    private UserResponse executeAuthRequest(String uri, Object requestBody, String failureMessage) {
        return buildRestClient().post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange((clientRequest, clientResponse) -> {
                    try {
                        int status = clientResponse.getStatusCode().value();
                        byte[] body = readBody(clientResponse);

                        if (status == 200 || status == 201) {
                            return objectMapper.readValue(body, UserResponse.class);
                        }
                        if (status == 400) {
                            throw new BadRequestException(extractMessage(body, "Invalid request"));
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

    private String extractMessage(byte[] body, String fallback) {
        if (body.length == 0) {
            return fallback;
        }

        try {
            return objectMapper.readTree(body).path("message").asText(fallback);
        } catch (Exception exception) {
            return fallback;
        }
    }
}
