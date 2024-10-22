package com.example.kuby.security.models.enums;

public enum UserRoles {
    ADMIN("admin"),
    USER("user");

    private String role;

    UserRoles(String role) {
        this.role = role;
    }

    public String getValue() {
        return role;
    }
}
