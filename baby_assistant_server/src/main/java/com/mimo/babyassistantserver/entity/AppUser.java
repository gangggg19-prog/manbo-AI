package com.mimo.babyassistantserver.entity;

import java.time.Instant;
import java.util.UUID;

/** A person who can sign in to Manbo. Passwords are stored only as BCrypt hashes. */
public class AppUser {
    private UUID id;
    private String username;
    private String displayName;
    private String passwordHash;
    private Instant createdAt;

    public static AppUser create(
            String username,
            String displayName,
            String passwordHash) {
        AppUser user = new AppUser();
        user.id = UUID.randomUUID();
        user.username = username;
        user.displayName = displayName;
        user.passwordHash = passwordHash;
        user.createdAt = Instant.now();
        return user;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}