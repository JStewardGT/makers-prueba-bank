package com.makers.loansystem.domain.exception;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException loan(Long id) {
        return new ResourceNotFoundException("No se encontró el préstamo con ID: " + id);
    }

    public static ResourceNotFoundException user(Long id) {
        return new ResourceNotFoundException("No se encontró el usuario con ID: " + id);
    }

    public static ResourceNotFoundException userByEmail(String email) {
        return new ResourceNotFoundException("No se encontró el usuario con email: " + email);
    }
}
