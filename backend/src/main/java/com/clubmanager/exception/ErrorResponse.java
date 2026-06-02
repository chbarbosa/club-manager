package com.clubmanager.exception;

public record ErrorResponse(String error, String message, String traceId) {
}

