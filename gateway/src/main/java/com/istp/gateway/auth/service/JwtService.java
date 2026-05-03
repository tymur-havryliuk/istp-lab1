package com.istp.gateway.auth.service;

import com.istp.gateway.auth.response.UserResponse;
import com.istp.gateway.exception.UnauthorizedException;
import com.istp.gateway.security.CurrentUser;
import com.istp.gateway.security.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String INVALID_TOKEN_MESSAGE = "Missing or invalid token";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserResponse user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.id()))
                .claim("email", user.email())
                .claim("role", user.role())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    public CurrentUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new CurrentUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("email", String.class),
                    UserRole.valueOf(claims.get("role", String.class))
            );
        } catch (JwtException | IllegalArgumentException | NullPointerException exception) {
            throw new UnauthorizedException(INVALID_TOKEN_MESSAGE);
        }
    }
}
