package cit.nurse.tracer.core.security.model;

public enum AppRole {
    USER,
    ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }

    public static AppRole fromValue(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }

        String normalized = value.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        try {
            return AppRole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Role must be either USER or ADMIN.");
        }
    }
}