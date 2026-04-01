package com.coloio.srms.domain.user;

import com.coloio.srms.domain.enums.Permission;
import com.coloio.srms.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Set;

public class DCAdmin extends AbstractUser {

    public DCAdmin() {}

    public DCAdmin(Long userId, String username, String email,
                   String passwordHash, LocalDateTime createdAt, boolean isActive) {
        super(userId, username, email, passwordHash, createdAt, isActive);
    }

    @Override
    public UserRole getRole() {
        return UserRole.DC_ADMIN;
    }

    @Override
    public Set<Permission> getPermissions() {
        return Set.of(
                Permission.MANAGE_RACKS,
                Permission.MANAGE_SERVERS,
                Permission.VIEW_RACKS,
                Permission.VIEW_SERVERS,
                Permission.MANAGE_USERS,
                Permission.SCHEDULE_MAINTENANCE,
                Permission.VIEW_MAINTENANCE,
                Permission.MANAGE_ZONES,
                Permission.VIEW_ZONES,
                Permission.CONFIGURE_ALERTS,
                Permission.ACKNOWLEDGE_ALERTS,
                Permission.VIEW_ALERTS,
                Permission.VIEW_METRICS,
                Permission.VIEW_REPORTS,
                Permission.VIEW_AUDIT_LOG
        );
    }
}
