package com.learning.servlet;

import com.learning.dao.HibernateUserDao;
import com.learning.dao.UserDao;
import com.learning.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private final UserDao userDao = new HibernateUserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("isAdmin", request.getAttribute("signedInUser") instanceof User user
                    && user.isAdmin());
            request.setAttribute("users", userDao.findAll());
        } catch (SQLException exception) {
            throw new ServletException("Could not load users", exception);
        }

        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
