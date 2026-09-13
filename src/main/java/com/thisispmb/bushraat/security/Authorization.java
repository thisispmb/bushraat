package com.thisispmb.bushraat.security;

import com.thisispmb.bushraat.model.User;
import com.thisispmb.bushraat.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.sql.SQLException;

public final class Authorization {

    private Authorization() {
    }

    public static User currentUser(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object id = session.getAttribute("userId");
        if (!(id instanceof Number number)) {
            return null;
        }

        return new UserRepository().findById(number.longValue());
    }

    public static Role currentRole(HttpServletRequest request) throws SQLException {
        User user = currentUser(request);
        return user == null ? null : Role.from(user.getRole());
    }

    public static boolean isSuperAdmin(HttpServletRequest request) throws SQLException {
        return currentRole(request) == Role.SUPER_ADMIN;
    }

    public static boolean canAccessAdmin(HttpServletRequest request) throws SQLException {
        Role role = currentRole(request);
        return role != null && role.canAccessAdmin();
    }
}
