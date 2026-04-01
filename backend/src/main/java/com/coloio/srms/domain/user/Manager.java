package com.coloio.srms.domain.user;

import com.coloio.srms.domain.enums.Permission;
import com.coloio.srms.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Set;

public class Manager extends AbstractUser {

    public Manager() {}

    public Manager(Long userId, String username, String email,
                   String passwordHash, LocalDateTime createdAt, boolean isActive) {
        super(userId, username, email, passwordHash, createdAt, isActive);
    }

    @Override
    public UserRole getRole() {
        return UserRole.MANAGER;
    }

    @Override
    public Set<Permission> getPermissions() {
        return Set.of(
                Permission.VIEW_RACKS,
                Permission.VIEW_SERVERS,
                Permission.VIEW_ZONES,
                Permission.VIEW_ALERTS,
                Permission.VIEW_METRICS,
                Permission.VIEW_MAINTENANCE,
                Permission.APPROVE_MAINTENANCE,
                Permission.VIEW_REPORTS
        );
    }
}
