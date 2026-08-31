package com.learning.servlet;

import com.learning.dao.HibernateRoleDao;
import com.learning.dao.HibernateUserDao;
import com.learning.dao.RoleDao;
import com.learning.dao.UserDao;
import com.learning.model.Role;
import com.learning.util.FullAdminProtection;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import com.learning.util.RoleInputValidator;
import com.learning.service.AuditService;
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
    private final UserDao userDao = new HibernateUserDao();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.MANAGE_ROLES)) {
            return;
        }

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!PermissionAccess.require(request, response, Permissions.MANAGE_ROLES)) {
            return;
        }

        Long roleId = parseId(request.getParameter("roleId"));
        String roleName = trimmedParameter(request, "roleName");
        Set<String> functionCodes = RolesServlet.selectedFunctionCodes(request);
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

            String error = RoleInputValidator.validationError(roleName);
            Set<String> allFunctionCodes = roleDao.findAllFunctionCodes();
            boolean removesFullAdministration = FullAdminProtection.isFullAdministrator(found.get(), allFunctionCodes)
                    && !FullAdminProtection.selectedFunctionsAreFullAdministrative(functionCodes, allFunctionCodes);
            if (error == null && removesFullAdministration
                    && FullAdminProtection.countFullAdministrators(roleDao, userDao) <= userDao.countByRoleId(roleId)) {
                error = "At least one user must keep full administrative permissions.";
            }

            if (error != null) {
                Role formRole = found.get();
                formRole.setName(roleName);
                request.setAttribute("error", error);
                request.setAttribute("role", formRole);
                request.setAttribute("selectedFunctions", RolesServlet.selectedFunctionMap(functionCodes));
                showForm(request, response);
                return;
            }

            roleDao.update(roleId, roleName, functionCodes);
            auditService.record((com.learning.model.User) request.getAttribute("signedInUser"), "ROLE_UPDATED", "ROLE", roleId,
                    roleName, true, "Assigned " + functionCodes.size() + " functions");
            response.sendRedirect(request.getContextPath() + "/roles?message=roleUpdated");
        } catch (SQLException exception) {
            throw new ServletException("Could not update role", exception);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
