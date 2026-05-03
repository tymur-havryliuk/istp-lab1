package com.istp.gateway.security;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class RouteAccessService {

    private static final List<RouteRule> PUBLIC_ROUTES = List.of(
            new RouteRule("POST", Pattern.compile("^/api/v1/auth/login$")),
            new RouteRule("GET", Pattern.compile("^/api/v1/courses$")),
            new RouteRule("GET", Pattern.compile("^/api/v1/courses/\\d+$")),
            new RouteRule("GET", Pattern.compile("^/api/v1/courses/\\d+/assignments$")),
            new RouteRule("GET", Pattern.compile("^/api/v1/assignments/\\d+$")),
            new RouteRule("GET", Pattern.compile("^/api/v1/files/\\d+$")),
            new RouteRule("GET", Pattern.compile("^/swagger-ui\\.html$")),
            new RouteRule("GET", Pattern.compile("^/swagger-ui(?:/.*)?$")),
            new RouteRule("GET", Pattern.compile("^/v3/api-docs(?:/.*)?$")),
            new RouteRule("GET", Pattern.compile("^/favicon\\.ico$"))
    );

    private static final List<RouteRule> TEACHER_ROUTES = List.of(
            new RouteRule("POST", Pattern.compile("^/api/v1/courses$")),
            new RouteRule("PUT", Pattern.compile("^/api/v1/courses/\\d+$")),
            new RouteRule("DELETE", Pattern.compile("^/api/v1/courses/\\d+$")),
            new RouteRule("GET", Pattern.compile("^/api/v1/courses/owned$")),
            new RouteRule("GET", Pattern.compile("^/api/v1/courses/\\d+/students$")),
            new RouteRule("POST", Pattern.compile("^/api/v1/courses/\\d+/assignments$")),
            new RouteRule("PUT", Pattern.compile("^/api/v1/assignments/\\d+$")),
            new RouteRule("DELETE", Pattern.compile("^/api/v1/assignments/\\d+$")),
            new RouteRule("GET", Pattern.compile("^/api/v1/assignments/\\d+/submissions$")),
            new RouteRule("POST", Pattern.compile("^/api/v1/submissions/\\d+/grade$"))
    );

    private static final List<RouteRule> STUDENT_ROUTES = List.of(
            new RouteRule("POST", Pattern.compile("^/api/v1/courses/\\d+/enroll$")),
            new RouteRule("GET", Pattern.compile("^/api/v1/courses/enrolled$")),
            new RouteRule("POST", Pattern.compile("^/api/v1/assignments/\\d+/submissions$")),
            new RouteRule("GET", Pattern.compile("^/api/v1/submissions/submitted$")),
            new RouteRule("GET", Pattern.compile("^/api/v1/grades/student$")),
            new RouteRule("POST", Pattern.compile("^/api/v1/files$"))
    );

    private static final List<RouteRule> AUTHENTICATED_ROUTES = List.of(
            new RouteRule("GET", Pattern.compile("^/api/v1/submissions/\\d+$"))
    );

    public boolean isPublic(String method, String path) {
        return resolveAccessLevel(method, path) == AccessLevel.PUBLIC;
    }

    public boolean isAllowed(String method, String path, UserRole role) {
        return switch (resolveAccessLevel(method, path)) {
            case PUBLIC, AUTHENTICATED -> true;
            case STUDENT -> role == UserRole.STUDENT;
            case TEACHER -> role == UserRole.TEACHER;
        };
    }

    public boolean requiresAuthentication(String method, String path) {
        return resolveAccessLevel(method, path) != AccessLevel.PUBLIC;
    }

    private AccessLevel resolveAccessLevel(String method, String path) {
        if (matches(PUBLIC_ROUTES, method, path)) {
            return AccessLevel.PUBLIC;
        }
        if (matches(TEACHER_ROUTES, method, path)) {
            return AccessLevel.TEACHER;
        }
        if (matches(STUDENT_ROUTES, method, path)) {
            return AccessLevel.STUDENT;
        }
        if (matches(AUTHENTICATED_ROUTES, method, path)) {
            return AccessLevel.AUTHENTICATED;
        }
        if (path.startsWith("/api/v1/")) {
            return AccessLevel.AUTHENTICATED;
        }
        return AccessLevel.PUBLIC;
    }

    private boolean matches(List<RouteRule> routes, String method, String path) {
        return routes.stream().anyMatch(route -> route.matches(method, path));
    }

    private enum AccessLevel {
        PUBLIC,
        AUTHENTICATED,
        STUDENT,
        TEACHER
    }

    private record RouteRule(String method, Pattern pathPattern) {

        boolean matches(String requestMethod, String path) {
            return method.equalsIgnoreCase(requestMethod) && pathPattern.matcher(path).matches();
        }
    }
}
