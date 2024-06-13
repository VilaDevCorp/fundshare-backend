package com.viladev.fundshare.auth;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladev.fundshare.utils.ApiResponse;
import com.viladev.fundshare.utils.CodeErrors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Autowired
    public JwtFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("Request: " + request.getRequestURI());
        ObjectMapper objectMapper = new ObjectMapper();
        if (!request.getRequestURI().matches("/api/public/.*")) {
            if (request.getCookies() == null) {
                ApiResponse<Void> apiResponse = new ApiResponse<>(CodeErrors.NOT_JWT_TOKEN, "Not cookies present");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                return;
            }
            Cookie jwtCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("JWT_TOKEN"))
                    .findFirst().orElse(null);
            String csrf = request.getHeader("X-API-CSRF");
            if (jwtCookie == null) {
                ApiResponse<Void> apiResponse = new ApiResponse<>(CodeErrors.NOT_JWT_TOKEN, "Not JWT token present");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                return;
            }
            if (csrf == null) {
                ApiResponse<Void> apiResponse = new ApiResponse<>(CodeErrors.NOT_CSRF_TOKEN, "Not CSRF token present");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                return;
            }
            Authentication authToken = jwtUtils.validateToken(jwtCookie.getValue(), csrf);
            if (authToken == null) {
                ApiResponse<Void> apiResponse = new ApiResponse<>(CodeErrors.INVALID_TOKEN, "Invalid jwt token");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                return;
            }

            // Set the Authentication object to the SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authToken);
            request.authenticate(response);
        }
        filterChain.doFilter(request, response);
    }
}
