package com.coloio.srms.pattern.factory;

import com.coloio.srms.domain.enums.UserRole;
import com.coloio.srms.domain.user.*;
import com.coloio.srms.dto.request.CreateUserRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserFactoryTest {

    private CreateUserRequest request(UserRole role) {
        CreateUserRequest r = new CreateUserRequest();
        r.setUsername("testuser");
        r.setEmail("test@example.com");
        r.setPassword("password123");
        r.setRole(role);
        return r;
    }

    @Test
    void createDCAdmin_returnsCorrectType() {
        AbstractUser user = UserFactory.create(request(UserRole.DC_ADMIN), "hashed");
        assertInstanceOf(DCAdmin.class, user);
        assertEquals(UserRole.DC_ADMIN, user.getRole());
        assertFalse(user.getPermissions().isEmpty());
    }

    @Test
    void createTechnician_returnsCorrectType() {
        AbstractUser user = UserFactory.create(request(UserRole.TECHNICIAN), "hashed");
        assertInstanceOf(Technician.class, user);
        assertEquals(UserRole.TECHNICIAN, user.getRole());
    }

    @Test
    void createCustomer_returnsCorrectType() {
        AbstractUser user = UserFactory.create(request(UserRole.CUSTOMER), "hashed");
        assertInstanceOf(Customer.class, user);
        assertEquals(UserRole.CUSTOMER, user.getRole());
    }

    @Test
    void createManager_returnsCorrectType() {
        AbstractUser user = UserFactory.create(request(UserRole.MANAGER), "hashed");
        assertInstanceOf(Manager.class, user);
        assertEquals(UserRole.MANAGER, user.getRole());
    }

    @Test
    void createdUser_hasCorrectUsername() {
        AbstractUser user = UserFactory.create(request(UserRole.DC_ADMIN), "hashed");
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.isActive());
    }
}
