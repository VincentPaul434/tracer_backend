package cit.nurse.tracer.core.security.service;

import cit.nurse.tracer.core.security.JwtUtils;
import cit.nurse.tracer.core.security.dto.AuthLoginRequest;
import cit.nurse.tracer.core.security.dto.AuthRegisterRequest;
import cit.nurse.tracer.core.security.dto.AuthResponse;
import cit.nurse.tracer.core.security.model.AppRole;
import cit.nurse.tracer.core.security.model.AppUser;
import cit.nurse.tracer.core.security.repository.AppUserRepository;
import java.time.Duration;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final Duration authTokenTtl;

    public AuthService(
        AppUserRepository appUserRepository,
        PasswordEncoder passwordEncoder,
        JwtUtils jwtUtils,
        @Value("${app.security.jwt.auth-ttl-minutes:1440}") long authTtlMinutes
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authTokenTtl = Duration.ofMinutes(authTtlMinutes);
    }

    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        String username = normalizeUsername(request.getUsername());

        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        AppRole role = AppRole.fromValue(request.getRole());

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        appUser.setRole(role);

        AppUser saved = appUserRepository.save(appUser);

        String token = jwtUtils.generateAuthToken(
            saved.getId(),
            saved.getUsername(),
            saved.getRole().asAuthority(),
            authTokenTtl
        );

        return new AuthResponse(token, saved.getUsername(), saved.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthLoginRequest request) {
        String username = normalizeUsername(request.getUsername());

        AppUser appUser = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        if (!passwordEncoder.matches(request.getPassword(), appUser.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        String token = jwtUtils.generateAuthToken(
            appUser.getId(),
            appUser.getUsername(),
            appUser.getRole().asAuthority(),
            authTokenTtl
        );

        return new AuthResponse(token, appUser.getUsername(), appUser.getRole().name());
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
