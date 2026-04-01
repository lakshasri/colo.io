package com.coloio.srms.domain.user;

import com.coloio.srms.domain.enums.Permission;
import com.coloio.srms.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Set;

public class Customer extends AbstractUser {

    private String companyName;
    private String contactPhone;

    public Customer() {}

    public Customer(Long userId, String username, String email,
                    String passwordHash, LocalDateTime createdAt, boolean isActive,
                    String companyName, String contactPhone) {
        super(userId, username, email, passwordHash, createdAt, isActive);
        this.companyName = companyName;
        this.contactPhone = contactPhone;
    }

    @Override
    public UserRole getRole() {
        return UserRole.CUSTOMER;
    }

    @Override
    public Set<Permission> getPermissions() {
        return Set.of(
                Permission.VIEW_OWN_SERVERS,
                Permission.VIEW_METRICS,
                Permission.VIEW_ALERTS,
                Permission.VIEW_MAINTENANCE
        );
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}
