package com.mimo.babyassistantserver.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimo.babyassistantserver.entity.AppUser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Issues and verifies compact HS256 JWTs for the demo.
 * Only the user id and expiry are placed in the token.
 */
@Service
public class JwtService {
    private static final Base64.Encoder URL_ENCODER =
            Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER =
            Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirySeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.expiry-seconds}") long expirySeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret.isBlank()
                ? generateEphemeralSecret()
                : secret.getBytes(StandardCharsets.UTF_8);
        this.expirySeconds = expirySeconds;
    }

    public IssuedToken issue(AppUser user) {
        Instant expiresAt = Instant.now().plusSeconds(expirySeconds);
        try {
            String header = encode(objectMapper.writeValueAsBytes(Map.of(
                    "alg", "HS256",
                    "typ", "JWT")));
            String payload = encode(objectMapper.writeValueAsBytes(Map.of(
                    "sub", user.getId().toString(),
                    "exp", expiresAt.getEpochSecond())));
            String unsignedToken = header + "." + payload;
            return new IssuedToken(
                    unsignedToken + "." + encode(sign(unsignedToken)),
                    expiresAt);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not issue access token", exception);
        }
    }

    public UUID requireSubject(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw unauthorized();
        }
        String token = authorizationHeader.substring(7).trim();
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw unauthorized();
        }

        try {
            String unsignedToken = parts[0] + "." + parts[1];
            byte[] expectedSignature = sign(unsignedToken);
            byte[] providedSignature = URL_DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
                throw unauthorized();
            }

            JsonNode payload = objectMapper.readTree(URL_DECODER.decode(parts[1]));
            String subject = payload.path("sub").asText("");
            long expiresAt = payload.path("exp").asLong(0);
            if (subject.isBlank() || expiresAt <= Instant.now().getEpochSecond()) {
                throw unauthorized();
            }
            return UUID.fromString(subject);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw unauthorized();
        }
    }

    private byte[] sign(String unsignedToken) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Keeps local demos usable without committing a predictable JWT secret.
     * Tokens are intentionally invalidated whenever the process restarts.
     */
    private byte[] generateEphemeralSecret() {
        byte[] generated = new byte[32];
        new SecureRandom().nextBytes(generated);
        return generated;
    }

    private String encode(byte[] bytes) {
        return URL_ENCODER.encodeToString(bytes);
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "A valid Bearer access token is required");
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}
