package com.spring.eventhub.config.security;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.spring.eventhub.common.ApiErrorResponse;
import com.spring.eventhub.common.filter.apikey.ApiKeyFilter;
import com.spring.eventhub.users.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public SecurityConfig(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {

        String pathId = "/events/{id}";
        String admin = "ADMIN";
        String organizer = "ORGANIZER";
        String attendee = "ATTENDEE";
        httpSecurity.addFilterBefore(new ApiKeyFilter(userService), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.GET, "/events").permitAll()
                        .requestMatchers(HttpMethod.GET, "/events/by-ids").authenticated()
                        .requestMatchers(HttpMethod.GET, pathId).permitAll()
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/events").hasAnyRole(organizer, admin)
                        .requestMatchers(HttpMethod.PATCH, "/events/{id}/publish").hasRole(admin)
                        .requestMatchers(HttpMethod.DELETE, pathId).hasRole(admin)
                        .requestMatchers(HttpMethod.PUT, pathId).hasAnyRole(organizer, admin)
                        .requestMatchers(HttpMethod.PATCH, pathId).hasAnyRole(organizer, admin)
                        .requestMatchers(HttpMethod.GET, "/bookings").hasRole(admin)
                        .requestMatchers(HttpMethod.DELETE, "/bookings/{id}").hasAnyRole(admin, attendee)
                        .requestMatchers(HttpMethod.POST, "/events/{eventId}/bookings").hasRole(attendee)
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable()).exceptionHandling(ex -> ex.authenticationEntryPoint(
                        authenticationEntryError(objectMapper, HttpStatus.UNAUTHORIZED,
                                "Unauthorized: Invalid api key"))
                        .accessDeniedHandler(accessDeniedHandler(objectMapper, HttpStatus.FORBIDDEN,
                                "Forbidden: The user does not have permissions")))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return httpSecurity.build();
    }

    private AuthenticationEntryPoint authenticationEntryError(ObjectMapper objectMapper, HttpStatus status,
            String message) {
        return (request, response, authException) -> {
            writeErrorResponse(objectMapper, status, message, response);
        };
    }

    private AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper, HttpStatus status, String message) {
        return (request, response, authException) -> {
            writeErrorResponse(objectMapper, status, message, response);
        };
    }

    private void writeErrorResponse(ObjectMapper objectMapper, HttpStatus status, String message,
            HttpServletResponse response) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiErrorResponse errorBody = new ApiErrorResponse(message, status.getReasonPhrase(), status.value());
        objectMapper.writeValue(response.getWriter(), errorBody);
    }
}
