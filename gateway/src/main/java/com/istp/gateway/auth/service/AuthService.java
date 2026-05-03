package com.istp.gateway.auth.service;

import tools.jackson.databind.ObjectMapper;
import com.istp.gateway.auth.request.LoginRequest;
import com.istp.gateway.auth.response.AuthResponse;
import com.istp.gateway.auth.response.UserResponse;
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
        UserResponse user = buildRestClient().post()
                .uri("/internal/auth/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange((clientRequest, clientResponse) -> {
                    try {
                        int status = clientResponse.getStatusCode().value();
                        byte[] body = readBody(clientResponse);

                        if (status == 200) {
                            return objectMapper.readValue(body, UserResponse.class);
                        }
                        if (status == 401) {
                            throw new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE);
                        }
                        throw new IllegalStateException("Backend auth verification failed");
                    } catch (IOException exception) {
                        throw new IllegalStateException("Backend auth verification failed", exception);
                    }
                });

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
}
