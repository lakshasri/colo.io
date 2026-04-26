package com.coloio.srms.service;

import com.coloio.srms.domain.enums.UserRole;
import com.coloio.srms.domain.user.AbstractUser;
import com.coloio.srms.dto.request.CreateUserRequest;
import com.coloio.srms.dto.response.UserResponse;
import com.coloio.srms.entity.UserEntity;
import com.coloio.srms.pattern.factory.UserFactory;
import com.coloio.srms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        String encoded = passwordEncoder.encode(request.getPassword());
        AbstractUser domainUser = UserFactory.create(request, encoded);

        UserEntity entity = toEntity(domainUser, request);
        entity = userRepository.save(entity);

        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findAllByRole(role).stream().map(this::toResponse).toList();
    }

    public UserResponse updateUser(Long id, CreateUserRequest request) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        entity.setEmail(request.getEmail());
        if (request.getCompanyName() != null) entity.setCompanyName(request.getCompanyName());
        if (request.getContactPhone() != null) entity.setContactPhone(request.getContactPhone());
        if (request.getCertifications() != null) entity.setCertifications(request.getCertifications());

        return toResponse(userRepository.save(entity));
    }

    public void deactivateUser(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        entity.setActive(false);
        userRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public UserEntity loadEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    // --- Mappers ---

    private UserEntity toEntity(AbstractUser domain, CreateUserRequest request) {
        UserEntity e = new UserEntity();
        e.setUsername(domain.getUsername());
        e.setEmail(domain.getEmail());
        e.setPasswordHash(domain.getPasswordHash());
        e.setRole(domain.getRole());
        e.setActive(domain.isActive());
        e.setCreatedAt(domain.getCreatedAt());
        e.setCompanyName(request.getCompanyName());
        e.setContactPhone(request.getContactPhone());
        e.setCertifications(request.getCertifications());
        return e;
    }

    public UserResponse toResponse(UserEntity e) {
        return new UserResponse(
                e.getUserId(),
                e.getUsername(),
                e.getEmail(),
                e.getRole(),
                e.getCompanyName(),
                e.getContactPhone(),
                e.getCreatedAt(),
                e.isActive()
        );
    }
}
