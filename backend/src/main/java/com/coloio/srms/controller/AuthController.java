package com.coloio.srms.controller;

import com.coloio.srms.config.JwtUtil;
import com.coloio.srms.dto.request.LoginRequest;
import com.coloio.srms.dto.response.AuthResponse;
import com.coloio.srms.entity.UserEntity;
import com.coloio.srms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Login and token refresh")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserEntity user = userService.loadEntityByUsername(request.getUsername());
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, user.getUsername(), user.getRole()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String bearerToken) {
        String refreshToken = bearerToken.substring(7);

        if (jwtUtil.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserEntity user = userService.loadEntityByUsername(username);

        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken, user.getUsername(), user.getRole()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Client discards tokens; stateless — no server-side invalidation needed
        return ResponseEntity.noContent().build();
    }
}
