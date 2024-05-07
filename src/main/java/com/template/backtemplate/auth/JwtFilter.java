package com.template.backtemplate.auth;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
        if (request.getRequestURI().matches("/api/public/.*")) {
            filterChain.doFilter(request, response);
        } else {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }
            if (request.getCookies() == null) {
                response.setStatus(401);
                return;
            }
            Cookie jwtCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("JWT_TOKEN"))
                    .findFirst().orElse(null);
            String csrf = request.getHeader("X-API-CSRF");
            if (jwtCookie == null || csrf == null) {
                response.setStatus(401);
                return;
            }
            Authentication authToken = jwtUtils.validateToken(jwtCookie.getValue(), csrf);
            if (authToken == null) {
                response.setStatus(401);
                return;
            }
            // Set the Authentication object to the SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authToken);

            request.authenticate(response);
        }
        filterChain.doFilter(request, response);
    }
}
