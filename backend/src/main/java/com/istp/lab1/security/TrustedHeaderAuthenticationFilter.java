package com.istp.lab1.security;

import com.istp.lab1.exception.UnauthorizedException;
import com.istp.lab1.user.dao.entity.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
public class TrustedHeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String INVALID_CONTEXT_MESSAGE = "Missing or invalid user context";

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            applyAuthenticationFromTrustedHeaders(request);
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            handlerExceptionResolver.resolveException(request, response, null, exception);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void applyAuthenticationFromTrustedHeaders(HttpServletRequest request) {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String email = request.getHeader(USER_EMAIL_HEADER);
        String roleHeader = request.getHeader(USER_ROLE_HEADER);

        if (isBlank(userIdHeader) && isBlank(email) && isBlank(roleHeader)) {
            return;
        }
        if (isBlank(userIdHeader) || isBlank(email) || isBlank(roleHeader)) {
            throw new UnauthorizedException(INVALID_CONTEXT_MESSAGE);
        }

        try {
            CurrentUser currentUser = new CurrentUser(
                    Long.valueOf(userIdHeader),
                    email,
                    UserRole.valueOf(roleHeader)
            );
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    currentUser,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + currentUser.role().name()))
            ));
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException(INVALID_CONTEXT_MESSAGE);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
