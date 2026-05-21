package com.lightbalance.health.service;

import com.lightbalance.health.domain.UserProfile;
import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.repo.UserProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");

    private final UserProfileRepository userProfileRepository;
    private final AccountBootstrapService accountBootstrapService;

    public AuthService(
        UserProfileRepository userProfileRepository,
        AccountBootstrapService accountBootstrapService
    ) {
        this.userProfileRepository = userProfileRepository;
        this.accountBootstrapService = accountBootstrapService;
    }

    @Transactional
    public AppDtos.AuthResponse register(AppDtos.RegisterRequest request) {
        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password());
        if (userProfileRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        UserProfile profile = new UserProfile();
        profile.setUsername(username);
        profile.setPasswordHash(hashPassword(password));
        profile.setSessionToken(generateToken());
        profile.setCreatedAt(LocalDateTime.now(APP_ZONE));
        userProfileRepository.save(profile);

        String displayName = normalizeName(request.name(), username);
        accountBootstrapService.seedForUser(profile, displayName);
        return toAuthResponse(userProfileRepository.save(profile));
    }

    @Transactional
    public AppDtos.AuthResponse login(AppDtos.AuthRequest request) {
        String username = normalizeUsername(request.username());
        String passwordHash = hashPassword(normalizePassword(request.password()));
        UserProfile profile = userProfileRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordHash.equals(profile.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        profile.setSessionToken(generateToken());
        return toAuthResponse(userProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public AppDtos.AuthResponse me(HttpServletRequest request) {
        return toAuthResponse(requireCurrentUser(request));
    }

    @Transactional
    public AppDtos.AuthResponse changePassword(HttpServletRequest request, AppDtos.ChangePasswordRequest passwordRequest) {
        UserProfile profile = requireCurrentUser(request);
        String currentPasswordHash = hashPassword(normalizePassword(passwordRequest.currentPassword()));
        if (!currentPasswordHash.equals(profile.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        profile.setPasswordHash(hashPassword(normalizePassword(passwordRequest.newPassword())));
        profile.setSessionToken(generateToken());
        return toAuthResponse(userProfileRepository.save(profile));
    }

    @Transactional
    public void recoverPassword(AppDtos.RecoverPasswordRequest request) {
        String username = normalizeUsername(request.username());
        String newPassword = normalizePassword(request.newPassword());
        UserProfile profile = userProfileRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String providedName = normalizeName(request.name(), "");
        if (!profile.getName().equalsIgnoreCase(providedName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Display name does not match this account");
        }

        profile.setPasswordHash(hashPassword(newPassword));
        profile.setSessionToken(generateToken());
        userProfileRepository.save(profile);
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        UserProfile profile = requireCurrentUser(request);
        profile.setSessionToken(generateToken());
        userProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public UserProfile requireCurrentUser(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first");
        }
        return userProfileRepository.findBySessionToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired, please log in again"));
    }

    private AppDtos.AuthResponse toAuthResponse(UserProfile profile) {
        return new AppDtos.AuthResponse(
            profile.getSessionToken(),
            new AppDtos.AuthUser(
                profile.getId(),
                profile.getUsername(),
                profile.getName(),
                profile.getHandleName(),
                profile.getGoal(),
                isAdmin(profile)
            )
        );
    }

    public void requireAdmin(UserProfile profile) {
        if (!isAdmin(profile)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    public boolean isAdmin(UserProfile profile) {
        return "admin".equalsIgnoreCase(profile.getUsername());
    }

    public String encodePassword(String password) {
        return hashPassword(normalizePassword(password));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }

    private String normalizeUsername(String value) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        String username = value.trim().toLowerCase();
        if (username.length() < 4 || username.length() > 24) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be 4 to 24 characters");
        }
        if (!username.matches("[a-z0-9_]+")) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Username only supports lowercase letters, numbers, and underscores"
            );
        }
        return username;
    }

    private String normalizePassword(String value) {
        if (value == null || value.trim().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }
        return value.trim();
    }

    private String normalizeName(String value, String username) {
        if (value == null || value.isBlank()) {
            return username;
        }
        return value.trim();
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }
}
