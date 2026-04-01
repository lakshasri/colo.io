package com.coloio.srms.domain.user;

import com.coloio.srms.domain.enums.Permission;
import com.coloio.srms.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class Technician extends AbstractUser {

    private List<String> certifications;

    public Technician() {}

    public Technician(Long userId, String username, String email,
                      String passwordHash, LocalDateTime createdAt, boolean isActive,
                      List<String> certifications) {
        super(userId, username, email, passwordHash, createdAt, isActive);
        this.certifications = certifications;
    }

    @Override
    public UserRole getRole() {
        return UserRole.TECHNICIAN;
    }

    @Override
    public Set<Permission> getPermissions() {
        return Set.of(
                Permission.VIEW_RACKS,
                Permission.VIEW_SERVERS,
                Permission.UPDATE_SERVER_STATUS,
                Permission.PERFORM_MAINTENANCE,
                Permission.VIEW_MAINTENANCE,
                Permission.VIEW_ZONES,
                Permission.ACKNOWLEDGE_ALERTS,
                Permission.VIEW_ALERTS,
                Permission.VIEW_METRICS
        );
    }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }
}
