package com.hospital.application.common.exceptions;

// 403 Forbidden
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}