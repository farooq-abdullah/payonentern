package com.learning.util;

import com.learning.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class PermissionAccess {
    private PermissionAccess() {
    }

    public static boolean require(HttpServletRequest request, HttpServletResponse response, String permission)
            throws IOException {
        Object value = request.getAttribute("signedInUser");
        if (!(value instanceof User user)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        if (!hasPermission(user, permission)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    public static boolean hasPermission(User user, String permission) {
        return user != null
                && user.getRole() != null
                && user.getRole().getFunctions().stream()
                .anyMatch(function -> permission.equals(function.getCode()));
    }
}
