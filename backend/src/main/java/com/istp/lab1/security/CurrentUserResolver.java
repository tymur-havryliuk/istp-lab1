package com.istp.lab1.security;

import com.istp.lab1.exception.UnauthorizedException;
import com.istp.lab1.user.dao.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String INVALID_CONTEXT_MESSAGE = "Missing or invalid user context";

    public CurrentUser resolve(HttpServletRequest request) {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String email = request.getHeader(USER_EMAIL_HEADER);
        String roleHeader = request.getHeader(USER_ROLE_HEADER);

        if (isBlank(userIdHeader) || isBlank(email) || isBlank(roleHeader)) {
            throw new UnauthorizedException(INVALID_CONTEXT_MESSAGE);
        }

        try {
            return new CurrentUser(
                    Long.valueOf(userIdHeader),
                    email,
                    UserRole.valueOf(roleHeader)
            );
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException(INVALID_CONTEXT_MESSAGE);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
