package com.spring.eventhub.common.filter.apikey;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.spring.eventhub.users.dto.UserResponseDto;
import com.spring.eventhub.users.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyFilter extends OncePerRequestFilter {
    private final UserService userService;

    public ApiKeyFilter(UserService userService) {
        this.userService = userService;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            List<String> apiKeys = Collections.list(request.getHeaders("x-api-key"));
            if (isValid(apiKeys)) {

                Optional<UserResponseDto> useropt = userService.getUser(apiKeys.get(0));

                if (useropt.isPresent()) {
                    UserResponseDto user = useropt.get();
                    List<SimpleGrantedAuthority> authorities = user.roles() != null
                            ? user.roles().stream().map(role -> new SimpleGrantedAuthority(role.toString())).toList()
                            : Collections.emptyList();

                    PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(user,
                            null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }

    }

    private boolean isValid(List<String> apiKeys) {

        return apiKeys.size() == 1 && apiKeys.get(0) != null && !apiKeys.get(0).isBlank();
    }

}
