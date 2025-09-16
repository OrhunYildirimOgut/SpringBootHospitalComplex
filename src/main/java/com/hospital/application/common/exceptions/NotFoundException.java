package com.hospital.application.common.exceptions;

// 404 Not Found
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}