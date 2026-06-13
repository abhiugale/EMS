package com.ems.modules.auth.service;

import com.ems.common.enums.Role;
import com.ems.common.exception.ResourceNotFoundException;
import com.ems.common.exception.UnauthorizedException;
import com.ems.common.exception.ValidationException;
import com.ems.modules.auth.dto.*;
import com.ems.modules.factory.entity.Factory;
import com.ems.modules.factory.repository.FactoryRepository;
import com.ems.modules.user.dto.UserDto;
import com.ems.modules.user.entity.User;
import com.ems.modules.user.repository.UserRepository;
import com.ems.security.JwtTokenProvider;
import com.ems.security.SecurityAuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);


    private final UserRepository userRepository;
    private final FactoryRepository factoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final SecurityAuditLogger auditLogger;

    @Value("${jwt.expiration-ms:3600000}")
    private long jwtExpirationInMs;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditLogger.logLoginFailure(request.getEmail(), "User not found");
                    return new UnauthorizedException("Invalid email or password");
                });

        if (!user.isActive()) {
            auditLogger.logLoginFailure(request.getEmail(), "Account is inactive");
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditLogger.logLoginFailure(request.getEmail(), "Invalid password credentials");
            throw new UnauthorizedException("Invalid email or password");
        }

        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.emptyList()
        );

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails, user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        auditLogger.logLoginSuccess(user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional
    public UserDto register(RegisterRequest request, String registeredByEmail) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email address already registered");
        }

        Factory factory = factoryRepository.findById(request.getFactoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Target factory not found"));

        User newUser = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .factory(factory)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(newUser);

        auditLogger.logRegistration(savedUser.getEmail(), savedUser.getRole().name(), registeredByEmail);

        return mapToUserDto(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid refresh token credentials");
        }

        String email = jwtTokenProvider.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid user context"));

        if (!user.isActive()) {
            throw new UnauthorizedException("User account is disabled");
        }

        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.emptyList()
        );

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails, user.getRole().name());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .user(mapToUserDto(user))
                .build();
    }

    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String jwt = authHeader.substring(7);
        String email = jwtTokenProvider.extractUsername(jwt);

        // Put token in blacklist with expiration TTL matching token expiry duration
        try {
            redisTemplate.opsForValue().set("blacklist:" + jwt, "true", jwtExpirationInMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Redis is offline or unreachable. Skipping token blacklisting: {}", e.getMessage());
        }

        auditLogger.logLogout(email);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .factoryId(user.getFactory() != null ? user.getFactory().getId() : null)
                .build();
    }
}
