package com.example.kuby.security.models.enums;

public enum TokenActionType {
    UPDATE(1),
    DELETE(2);
    private final int priority;

    TokenActionType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
