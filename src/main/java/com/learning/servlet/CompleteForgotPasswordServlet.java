package com.learning.servlet;

import com.learning.service.ForgotPasswordService;
import com.learning.service.PasswordOperationResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/complete-forgot-password")
public class CompleteForgotPasswordServlet extends HttpServlet {
    private final ForgotPasswordService forgotPasswordService = new ForgotPasswordService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        showForm(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        try {
            PasswordOperationResult result = forgotPasswordService.resetPassword(token, request.getParameter("newPassword"),
                    request.getParameter("confirmation"));
            if (result.successful()) {
                response.sendRedirect(request.getContextPath() + "/login?passwordReset=true");
                return;
            }
            request.setAttribute("error", result.error());
            showForm(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Could not reset password", exception);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/complete-forgot-password.jsp").forward(request, response);
    }
}
