package com.viladev.fundshare.auth;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.viladev.fundshare.service.CustomUserDetailsService;

import io.jsonwebtoken.*;

@Component
public class JwtUtils {
    @Value("${auth.jwt.secret}")
    private String jwtSecret;

    @Value("${auth.jwt.expiration}")
    private long expirationTime;

    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JwtUtils(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    public String generateToken(String username, UUID csrfToken) {
        return Jwts.builder()
                .setSubject(username)
                .claim("csrf", csrfToken.toString())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean isExpired(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    public String extractCSRFToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .get("csrf", String.class);
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Authentication validateToken(String token, String csrf) {
        try {
            if (isExpired(token)) {
                return null;
            }
            String username = extractUsername(token);
            String csrfToken = extractCSRFToken(token);
            if (username == null || csrfToken == null) {
                return null;
            }
            if (!extractCSRFToken(token).equals(csrf)) {
                return null;
            }
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null,
                    userDetails.getAuthorities());
        } catch (Exception e) {
            return null;
        }
    }
}
