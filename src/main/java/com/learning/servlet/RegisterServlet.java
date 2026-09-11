package com.learning.servlet;

import com.learning.model.User;
import com.learning.service.ServiceResult;
import com.learning.service.UserManagementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserManagementService userManagementService = new UserManagementService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showForm(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trimmedParameter(request, "username");
        String email = trimmedParameter(request, "email");
        String password = request.getParameter("password");
        request.setAttribute("username", username);
        request.setAttribute("email", email);

        try {
            ServiceResult<User> result = userManagementService.register(username, email, password);
            if (!result.successful()) {
                request.setAttribute("error", result.error());
                showForm(request, response);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/login?registered=true");
        } catch (SQLException exception) {
            throw new ServletException("Could not create account", exception);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    private String trimmedParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
