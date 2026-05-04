package com.istp.lab1.auth.service.api;

import com.istp.lab1.auth.controller.request.InternalRegisterRequest;
import com.istp.lab1.auth.controller.response.InternalUserResponse;

public interface InternalAuthService {

    InternalUserResponse verifyCredentials(String email, String password);

    InternalUserResponse register(InternalRegisterRequest request);
}
