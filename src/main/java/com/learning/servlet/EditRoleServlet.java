package com.learning.servlet;

import com.learning.dao.HibernateRoleDao;
import com.learning.dao.RoleDao;
import com.learning.model.Role;
import com.learning.model.User;
import com.learning.service.RoleManagementService;
import com.learning.service.ServiceResult;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

@WebServlet("/edit-role")
public class EditRoleServlet extends HttpServlet {
    private final RoleDao roleDao = new HibernateRoleDao();
    private final RoleManagementService roleManagementService = new RoleManagementService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.MANAGE_ROLES)) return;
        Long roleId = parseId(request.getParameter("id"));
        if (roleId == null) {
            response.sendRedirect(request.getContextPath() + "/roles");
            return;
        }
        try {
            Optional<Role> found = roleDao.findById(roleId);
            if (found.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/roles");
                return;
            }
            request.setAttribute("role", found.get());
            request.setAttribute("selectedFunctions", RolesServlet.selectedFunctionMap(
                    found.get().getFunctions().stream().map(function -> function.getCode()).collect(java.util.stream.Collectors.toSet())));
            showForm(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Could not load role", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.MANAGE_ROLES)) return;
        Long roleId = parseId(request.getParameter("roleId"));
        String roleName = trimmedParameter(request, "roleName");
        Set<String> functionCodes = RolesServlet.selectedFunctionCodes(request);
        if (roleId == null) {
            response.sendRedirect(request.getContextPath() + "/roles");
            return;
        }
        try {
            ServiceResult<Role> result = roleManagementService.update((User) request.getAttribute("signedInUser"), roleId, roleName, functionCodes);
            if (!result.successful()) {
                if (result.status() == ServiceResult.Status.NOT_FOUND) {
                    response.sendRedirect(request.getContextPath() + "/roles");
                    return;
                }
                Role formRole = roleDao.findById(roleId).orElse(null);
                if (formRole == null) {
                    response.sendRedirect(request.getContextPath() + "/roles");
                    return;
                }
                formRole.setName(roleName);
                request.setAttribute("error", result.error());
                request.setAttribute("role", formRole);
                request.setAttribute("selectedFunctions", RolesServlet.selectedFunctionMap(functionCodes));
                showForm(request, response);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/roles?message=roleUpdated");
        } catch (SQLException exception) {
            throw new ServletException("Could not update role", exception);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("functions", roleDao.findAllFunctions());
            request.getRequestDispatcher("/WEB-INF/views/edit-role.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Could not load functions", exception);
        }
    }

    private Long parseId(String value) {
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException | NullPointerException exception) {
            return null;
        }
    }

    private String trimmedParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
