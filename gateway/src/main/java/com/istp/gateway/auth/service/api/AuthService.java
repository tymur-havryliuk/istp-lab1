package com.istp.gateway.auth.service.api;

import com.istp.gateway.auth.request.LoginRequest;
import com.istp.gateway.auth.request.RegisterRequest;
import com.istp.gateway.auth.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);
}
