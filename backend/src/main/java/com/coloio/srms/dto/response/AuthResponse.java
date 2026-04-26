package com.coloio.srms.dto.response;

import com.coloio.srms.domain.enums.UserRole;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String username;
    private UserRole role;

    public AuthResponse(String accessToken, String refreshToken, String username, UserRole role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
}
