package com.template.backtemplate.auth;

import java.util.UUID;

public class AuthResult {

    private String jwtToken;
    private UUID csrfToken;

    public AuthResult(String jwtToken, UUID csrfToken) {
        this.jwtToken = jwtToken;
        this.csrfToken = csrfToken;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public UUID getCsrfToken() {
        return csrfToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public void setCsrfToken(UUID csrfToken) {
        this.csrfToken = csrfToken;
    }

}
