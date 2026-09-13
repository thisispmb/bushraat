package com.thisispmb.bushraat.security;

public enum Role {
    USER,
    LIBRARIAN,
    SUPER_ADMIN;

    public static Role from(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return Role.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean canAccessAdmin() {
        return this == LIBRARIAN || this == SUPER_ADMIN;
    }

    public boolean canManageBooks() {
        return canAccessAdmin();
    }

    public boolean canManageCategories() {
        return canAccessAdmin();
    }

    public boolean canManageUsers() {
        return canAccessAdmin();
    }

    public boolean canManageRoles() {
        return this == SUPER_ADMIN;
    }
}
