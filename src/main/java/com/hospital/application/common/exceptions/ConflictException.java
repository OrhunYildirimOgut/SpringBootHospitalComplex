package com.hospital.application.common.exceptions;

// 409 Conflict
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}