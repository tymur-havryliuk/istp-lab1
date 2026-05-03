package com.istp.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, GatewayAuthFilter gatewayAuthFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(new RegexRequestMatcher("^/api/v1/courses$", HttpMethod.GET.name())).permitAll()
                        .requestMatchers(new RegexRequestMatcher("^/api/v1/courses/\\d+$", HttpMethod.GET.name())).permitAll()
                        .requestMatchers(new RegexRequestMatcher("^/api/v1/courses/\\d+/assignments$", HttpMethod.GET.name())).permitAll()
                        .requestMatchers(new RegexRequestMatcher("^/api/v1/assignments/\\d+$", HttpMethod.GET.name())).permitAll()
                        .requestMatchers(new RegexRequestMatcher("^/api/v1/files/\\d+$", HttpMethod.GET.name())).permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/favicon.ico").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
