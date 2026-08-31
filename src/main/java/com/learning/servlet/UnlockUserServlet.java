package com.learning.servlet;

import com.learning.model.User;
import com.learning.service.AuthenticationService;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/unlock-user")
public class UnlockUserServlet extends HttpServlet {
    private final AuthenticationService authenticationService = new AuthenticationService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.UNLOCK_USER)) return;
        Long userId = positiveLong(request.getParameter("userId"));
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        try {
            User actor = (User) request.getAttribute("signedInUser");
            boolean unlocked = authenticationService.unlock(actor, userId);
            response.sendRedirect(request.getContextPath() + "/home?message=" + (unlocked ? "userUnlocked" : "userMissing"));
        } catch (SQLException exception) {
            throw new ServletException("Could not unlock account", exception);
        }
    }

    private Long positiveLong(String value) {
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException | NullPointerException exception) {
            return null;
        }
    }
}
