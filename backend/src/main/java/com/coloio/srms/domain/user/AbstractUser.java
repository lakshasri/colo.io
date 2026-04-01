package com.coloio.srms.domain.user;

import com.coloio.srms.domain.enums.Permission;
import com.coloio.srms.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Set;

public abstract class AbstractUser {

    protected Long userId;
    protected String username;
    protected String email;
    protected String passwordHash;
    protected LocalDateTime createdAt;
    protected boolean isActive;

    protected AbstractUser() {}

    protected AbstractUser(Long userId, String username, String email,
                           String passwordHash, LocalDateTime createdAt, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    public abstract UserRole getRole();

    public abstract Set<Permission> getPermissions();

    public boolean hasPermission(Permission permission) {
        return getPermissions().contains(permission);
    }

    // Getters
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setActive(boolean active) { isActive = active; }
}
