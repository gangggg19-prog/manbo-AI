package com.mimo.babyassistantserver.service.impl;

import java.util.Locale;

import com.mimo.babyassistantserver.dto.auth.AuthResponse;
import com.mimo.babyassistantserver.dto.auth.AuthUserResponse;
import com.mimo.babyassistantserver.dto.auth.LoginRequest;
import com.mimo.babyassistantserver.dto.auth.RegisterRequest;
import com.mimo.babyassistantserver.entity.AppUser;
import com.mimo.babyassistantserver.mapper.AppUserMapper;
import com.mimo.babyassistantserver.security.JwtService;
import com.mimo.babyassistantserver.service.AuthService;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Password checking and JWT creation live here instead of in controllers. */
@Service
public class AuthServiceImpl implements AuthService {
    private final AppUserMapper userMapper;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthServiceImpl(AppUserMapper userMapper, JwtService jwtService) {
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        if (userMapper.selectByUsername(username) != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This username is already registered");
        }

        AppUser user = AppUser.create(
                username,
                request.displayName().trim(),
                passwordEncoder.encode(request.password()));
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This username is already registered");
        }
        return responseFor(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = userMapper.selectByUsername(
                normalizeUsername(request.username()));
        if (user == null
                || !passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Username or password is incorrect");
        }
        return responseFor(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser requireUser(String authorizationHeader) {
        AppUser user = userMapper.selectById(
                jwtService.requireSubject(authorizationHeader));
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "The signed-in account no longer exists");
        }
        return user;
    }

    private AuthResponse responseFor(AppUser user) {
        JwtService.IssuedToken token = jwtService.issue(user);
        return new AuthResponse(
                token.value(),
                "Bearer",
                token.expiresAt(),
                new AuthUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getDisplayName()));
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}