package com.learning.servlet;

import com.learning.service.ForgotPasswordService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
    private final ForgotPasswordService forgotPasswordService = new ForgotPasswordService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        if (email == null || email.isBlank()) {
            request.setAttribute("error", "Email is required.");
            doGet(request, response);
            return;
        }
        try {
            forgotPasswordService.requestReset(email.trim(), resetLinkPrefix(request));
            response.sendRedirect(request.getContextPath() + "/forgot-password?sent=true");
        } catch (SQLException exception) {
            throw new ServletException("Could not request password reset", exception);
        }
    }

    private String resetLinkPrefix(HttpServletRequest request) {
        String port = request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort();
        return request.getScheme() + "://" + request.getServerName() + port + request.getContextPath()
                + "/complete-forgot-password?token=";
    }
}
