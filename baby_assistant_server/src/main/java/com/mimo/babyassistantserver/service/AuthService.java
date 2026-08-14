package com.mimo.babyassistantserver.service;

import com.mimo.babyassistantserver.dto.auth.AuthResponse;
import com.mimo.babyassistantserver.dto.auth.LoginRequest;
import com.mimo.babyassistantserver.dto.auth.RegisterRequest;
import com.mimo.babyassistantserver.entity.AppUser;

/** Business boundary for registering, signing in and authenticating requests. */
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AppUser requireUser(String authorizationHeader);
}