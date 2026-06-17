package com.clubmanager.service;

import com.clubmanager.config.AppSecurityConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminPasswordPolicyService {

    private static final int MAX_LENGTH = 128;

    private final AppSecurityConfig appSecurityConfig;



    public void validate(String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Password is required");
        }
        if (!password.equals(password.trim()) || password.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("Password must not contain whitespace");
        }
        int minLength = appSecurityConfig.password().minLength();
        if (password.length() < minLength || password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Password must be between " + minLength + " and " + MAX_LENGTH + " characters");
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            throw new IllegalArgumentException("Password must include an uppercase letter");
        }
        if (password.chars().noneMatch(Character::isLowerCase)) {
            throw new IllegalArgumentException("Password must include a lowercase letter");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Password must include a digit");
        }
    }
}
