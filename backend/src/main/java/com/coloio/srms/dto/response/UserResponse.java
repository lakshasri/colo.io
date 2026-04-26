package com.coloio.srms.dto.response;

import com.coloio.srms.domain.enums.UserRole;

import java.time.LocalDateTime;

public class UserResponse {

    private Long userId;
    private String username;
    private String email;
    private UserRole role;
    private String companyName;
    private String contactPhone;
    private LocalDateTime createdAt;
    private boolean isActive;

    public UserResponse() {}

    public UserResponse(Long userId, String username, String email, UserRole role,
                        String companyName, String contactPhone,
                        LocalDateTime createdAt, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.companyName = companyName;
        this.contactPhone = contactPhone;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public String getCompanyName() { return companyName; }
    public String getContactPhone() { return contactPhone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return isActive; }
}
