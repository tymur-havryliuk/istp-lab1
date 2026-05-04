package com.istp.lab1.auth.controller;

import com.istp.lab1.auth.controller.request.InternalLoginRequest;
import com.istp.lab1.auth.controller.request.InternalRegisterRequest;
import com.istp.lab1.auth.controller.response.InternalUserResponse;
import com.istp.lab1.auth.service.api.InternalAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController {

    private final InternalAuthService internalAuthService;

    @PostMapping("/verify")
    public InternalUserResponse verify(@Valid @RequestBody InternalLoginRequest request) {
        return internalAuthService.verifyCredentials(request.email(), request.password());
    }

    @PostMapping("/register")
    public ResponseEntity<InternalUserResponse> register(@Valid @RequestBody InternalRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(internalAuthService.register(request));
    }
}
