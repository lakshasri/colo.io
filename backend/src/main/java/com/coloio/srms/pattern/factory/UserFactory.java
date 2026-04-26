package com.coloio.srms.pattern.factory;

import com.coloio.srms.domain.user.*;
import com.coloio.srms.dto.request.CreateUserRequest;

import java.time.LocalDateTime;

public class UserFactory {

    private UserFactory() {}

    public static AbstractUser create(CreateUserRequest request, String encodedPassword) {
        LocalDateTime now = LocalDateTime.now();

        return switch (request.getRole()) {
            case DC_ADMIN -> new DCAdmin(
                    null,
                    request.getUsername(),
                    request.getEmail(),
                    encodedPassword,
                    now,
                    true
            );
            case TECHNICIAN -> new Technician(
                    null,
                    request.getUsername(),
                    request.getEmail(),
                    encodedPassword,
                    now,
                    true,
                    request.getCertifications() != null ? request.getCertifications() : java.util.List.of()
            );
            case CUSTOMER -> new Customer(
                    null,
                    request.getUsername(),
                    request.getEmail(),
                    encodedPassword,
                    now,
                    true,
                    request.getCompanyName(),
                    request.getContactPhone()
            );
            case MANAGER -> new Manager(
                    null,
                    request.getUsername(),
                    request.getEmail(),
                    encodedPassword,
                    now,
                    true
            );
        };
    }
}
