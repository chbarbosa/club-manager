package com.clubmanager.exception;

public class LoginRateLimitException extends RuntimeException {

    public LoginRateLimitException() {
        super("Too many login attempts. Try again later.");
    }
}
