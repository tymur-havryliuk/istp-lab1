package com.istp.lab1.security;

import com.istp.lab1.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private static final String INVALID_CONTEXT_MESSAGE = "Missing or invalid user context";

    public CurrentUser resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException(INVALID_CONTEXT_MESSAGE);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CurrentUser currentUser) {
            return currentUser;
        }
        throw new UnauthorizedException(INVALID_CONTEXT_MESSAGE);
    }
}
