package com.learning.api;

import com.learning.dao.AuditLogPage;
import com.learning.dao.UserPage;
import com.learning.model.Permission;
import com.learning.model.Role;
import com.learning.model.User;
import com.learning.service.AuthenticationResult;
import com.learning.service.AuthenticationService;
import com.learning.service.AuditLogService;
import com.learning.service.ForgotPasswordService;
import com.learning.service.PasswordManagementService;
import com.learning.service.PasswordOperationResult;
import com.learning.service.RoleManagementService;
import com.learning.service.ServiceResult;
import com.learning.service.UserDirectoryService;
import com.learning.service.UserManagementService;
import com.learning.servlet.LoginServlet;
import com.learning.util.PermissionAccess;
import com.learning.util.Permissions;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * JSON-only transport layer. It routes HTTP requests, calls services, and maps safe response DTOs.
 * It never performs password, validation, audit, or persistence business rules itself.
 */
@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {
    private final AuthenticationService authenticationService = new AuthenticationService();
    private final UserDirectoryService userDirectoryService = new UserDirectoryService();
    private final UserManagementService userManagementService = new UserManagementService();
    private final PasswordManagementService passwordManagementService = new PasswordManagementService();
    private final ForgotPasswordService forgotPasswordService = new ForgotPasswordService();
    private final RoleManagementService roleManagementService = new RoleManagementService();
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            String path = path(request);
            if ("/auth/me".equals(path)) {
                User actor = requireAuthenticated(request, response);
                if (actor != null) ApiResponses.write(response, HttpServletResponse.SC_OK, ApiViewMapper.user(actor));
                return;
            }
            if ("/users".equals(path)) {
                if (!requirePermission(request, response, Permissions.VIEW_USERS)) return;
                UserPage page = userDirectoryService.findUsers(request.getParameter("search"), request.getParameter("sort"),
                        request.getParameter("dir"), request.getParameter("page"));
                ApiResponses.write(response, HttpServletResponse.SC_OK, ApiViewMapper.userPage(page));
                return;
            }
            Long userId = idAfter(path, "/users/");
            if (userId != null) {
                if (!requirePermission(request, response, Permissions.VIEW_USERS)) return;
                var user = userManagementService.findById(userId);
                if (user.isEmpty()) {
                    ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND, "USER_NOT_FOUND", "User was not found.");
                } else {
                    ApiResponses.write(response, HttpServletResponse.SC_OK, ApiViewMapper.user(user.get()));
                }
                return;
            }
            if ("/roles".equals(path)) {
                if (!requirePermission(request, response, Permissions.MANAGE_ROLES)) return;
                ApiResponses.write(response, HttpServletResponse.SC_OK, Map.of("items", ApiViewMapper.roles(roleManagementService.findAll())));
                return;
            }
            Long roleId = idAfter(path, "/roles/");
            if (roleId != null) {
                if (!requirePermission(request, response, Permissions.MANAGE_ROLES)) return;
                var role = roleManagementService.findById(roleId);
                if (role.isEmpty()) {
                    ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND, "ROLE_NOT_FOUND", "Role was not found.");
                } else {
                    ApiResponses.write(response, HttpServletResponse.SC_OK, ApiViewMapper.role(role.get()));
                }
                return;
            }
            if ("/functions".equals(path)) {
                if (!requirePermission(request, response, Permissions.MANAGE_ROLES)) return;
                List<String> functions = roleManagementService.findAllFunctions().stream().map(Permission::getCode).toList();
                ApiResponses.write(response, HttpServletResponse.SC_OK, Map.of("items", functions));
                return;
            }
            if ("/audit-log".equals(path)) {
                if (!requirePermission(request, response, Permissions.VIEW_AUDIT_LOG)) return;
                AuditLogPage page = auditLogService.findEntries(request.getParameter("action"), request.getParameter("actor"),
                        request.getParameter("targetType"), request.getParameter("successful"), request.getParameter("page"));
                ApiResponses.write(response, HttpServletResponse.SC_OK, ApiViewMapper.auditPage(page));
                return;
            }
            notFound(response);
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "The request could not be completed.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            String path = path(request);
            switch (path) {
                case "/auth/login" -> login(request, response);
                case "/auth/logout" -> logout(request, response);
                case "/auth/register" -> register(request, response);
                case "/auth/forgot-password" -> requestPasswordReset(request, response);
                case "/auth/reset-password" -> completePasswordReset(request, response);
                case "/users" -> createUser(request, response);
                case "/users/me/password" -> changeOwnPassword(request, response);
                case "/roles" -> createRole(request, response);
                default -> postResourceAction(request, response, path);
            }
        } catch (ApiResponses.InvalidJsonException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_JSON", "Request body must be valid JSON.");
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "The request could not be completed.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            String path = path(request);
            Long userId = idAfter(path, "/users/");
            if (userId != null) {
                updateUser(request, response, userId);
                return;
            }
            Long roleId = idAfter(path, "/roles/");
            if (roleId != null) {
                updateRole(request, response, roleId);
                return;
            }
            notFound(response);
        } catch (ApiResponses.InvalidJsonException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_JSON", "Request body must be valid JSON.");
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "The request could not be completed.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            String path = path(request);
            Long userId = idAfter(path, "/users/");
            if (userId != null) {
                if (!requirePermission(request, response, Permissions.DELETE_USER)) return;
                User actor = signedInUser(request);
                ServiceResult<User> result = userManagementService.delete(actor, userId);
                writeServiceResult(response, result, HttpServletResponse.SC_OK, ApiViewMapper::user);
                if (result.successful() && actor.getId() == userId) request.getSession(false).invalidate();
                return;
            }
            Long roleId = idAfter(path, "/roles/");
            if (roleId != null) {
                if (!requirePermission(request, response, Permissions.MANAGE_ROLES)) return;
                ServiceResult<Role> result = roleManagementService.delete(signedInUser(request), roleId);
                writeServiceResult(response, result, HttpServletResponse.SC_OK, ApiViewMapper::role);
                return;
            }
            notFound(response);
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "The request could not be completed.");
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        LoginRequest body = ApiResponses.read(request, LoginRequest.class);
        if (blank(body.username()) || blank(body.password())) {
            ApiResponses.error(response, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Username and password are required.");
            return;
        }
        AuthenticationResult result = authenticationService.authenticate(body.username().trim(), body.password());
        if (result.status() == AuthenticationResult.Status.INVALID_CREDENTIALS) {
            ApiResponses.error(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid username or password.");
            return;
        }
        if (result.status() == AuthenticationResult.Status.LOCKED) {
            ApiResponses.error(response, HttpServletResponse.SC_FORBIDDEN, "ACCOUNT_LOCKED", "This account is temporarily locked.");
            return;
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(LoginServlet.LOGGED_IN_USER_ID, result.user().getId());
        session.setAttribute(LoginServlet.LOGGED_IN_USERNAME, result.user().getUsername());
        ApiResponses.write(response, HttpServletResponse.SC_OK, Map.of("user", ApiViewMapper.user(result.user())));
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        ApiResponses.write(response, HttpServletResponse.SC_OK, Map.of("message", "Logged out."));
    }

    private void register(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        RegisterRequest body = ApiResponses.read(request, RegisterRequest.class);
        ServiceResult<User> result = userManagementService.register(body.username(), body.email(), body.password());
        writeServiceResult(response, result, HttpServletResponse.SC_CREATED, ApiViewMapper::user);
    }

    private void createUser(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        if (!requirePermission(request, response, Permissions.EDIT_USER)) return;
        CreateUserRequest body = ApiResponses.read(request, CreateUserRequest.class);
        if (body.roleId() == null || body.roleId() <= 0) {
            ApiResponses.error(response, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "A valid roleId is required.");
            return;
        }
        ServiceResult<User> result = userManagementService.createByAdministrator(signedInUser(request), body.username(), body.email(),
                body.password(), body.roleId());
        writeServiceResult(response, result, HttpServletResponse.SC_CREATED, ApiViewMapper::user);
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response, long userId) throws IOException, SQLException {
        if (!requirePermission(request, response, Permissions.EDIT_USER)) return;
        UserUpdateRequest body = ApiResponses.read(request, UserUpdateRequest.class);
        if (body.roleId() == null || body.roleId() <= 0) {
            ApiResponses.error(response, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "A valid roleId is required.");
            return;
        }
        ServiceResult<User> result = userManagementService.updateProfile(signedInUser(request), userId, body.username(), body.email(), body.roleId());
        writeServiceResult(response, result, HttpServletResponse.SC_OK, ApiViewMapper::user);
    }

    private void changeOwnPassword(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        if (!requirePermission(request, response, Permissions.CHANGE_OWN_PASSWORD)) return;
        PasswordRequest body = ApiResponses.read(request, PasswordRequest.class);
        PasswordOperationResult result = passwordManagementService.changeOwnPassword(signedInUser(request).getId(),
                body.currentPassword(), body.newPassword(), body.confirmation());
        writePasswordResult(response, result);
    }

    private void postResourceAction(HttpServletRequest request, HttpServletResponse response, String path) throws IOException, SQLException {
        Long resetId = idBeforeSuffix(path, "/users/", "/reset-password");
        if (resetId != null) {
            if (!requirePermission(request, response, Permissions.RESET_PASSWORD)) return;
            PasswordRequest body = ApiResponses.read(request, PasswordRequest.class);
            PasswordOperationResult result = passwordManagementService.resetByAdministrator(signedInUser(request), resetId,
                    body.newPassword(), body.confirmation());
            writePasswordResult(response, result);
            return;
        }
        Long unlockId = idBeforeSuffix(path, "/users/", "/unlock");
        if (unlockId != null) {
            if (!requirePermission(request, response, Permissions.UNLOCK_USER)) return;
            boolean unlocked = authenticationService.unlock(signedInUser(request), unlockId);
            if (!unlocked) {
                ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND, "USER_NOT_FOUND", "User was not found.");
            } else {
                ApiResponses.write(response, HttpServletResponse.SC_OK, Map.of("message", "Account unlocked."));
            }
            return;
        }
        notFound(response);
    }

    private void requestPasswordReset(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        ForgotPasswordRequest body = ApiResponses.read(request, ForgotPasswordRequest.class);
        if (blank(body.email())) {
            ApiResponses.error(response, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Email is required.");
            return;
        }
        forgotPasswordService.requestReset(body.email().trim(), resetLinkPrefix(request));
        ApiResponses.write(response, HttpServletResponse.SC_OK,
                Map.of("message", "If an account uses that email address, a reset link has been sent."));
    }

    private void completePasswordReset(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        ResetPasswordRequest body = ApiResponses.read(request, ResetPasswordRequest.class);
        PasswordOperationResult result = forgotPasswordService.resetPassword(body.token(), body.newPassword(), body.confirmation());
        writePasswordResult(response, result);
    }

    private void createRole(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        if (!requirePermission(request, response, Permissions.MANAGE_ROLES)) return;
        RoleRequest body = ApiResponses.read(request, RoleRequest.class);
        ServiceResult<Role> result = roleManagementService.create(signedInUser(request), body.name(), safeFunctions(body.functions()));
        writeServiceResult(response, result, HttpServletResponse.SC_CREATED, ApiViewMapper::role);
    }

    private void updateRole(HttpServletRequest request, HttpServletResponse response, long roleId) throws IOException, SQLException {
        if (!requirePermission(request, response, Permissions.MANAGE_ROLES)) return;
        RoleRequest body = ApiResponses.read(request, RoleRequest.class);
        ServiceResult<Role> result = roleManagementService.update(signedInUser(request), roleId, body.name(), safeFunctions(body.functions()));
        writeServiceResult(response, result, HttpServletResponse.SC_OK, ApiViewMapper::role);
    }

    private void writePasswordResult(HttpServletResponse response, PasswordOperationResult result) throws IOException {
        if (result.successful()) {
            ApiResponses.write(response, HttpServletResponse.SC_OK, Map.of("message", "Password updated."));
        } else {
            int status = result.status() == PasswordOperationResult.Status.NOT_FOUND
                    ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_REQUEST;
            String code = switch (result.status()) {
                case NOT_FOUND -> "USER_NOT_FOUND";
                case INVALID_TOKEN -> "INVALID_RESET_TOKEN";
                case VALIDATION_ERROR -> "VALIDATION_ERROR";
                case SUCCESS -> "SUCCESS";
            };
            ApiResponses.error(response, status, code, result.error());
        }
    }

    private <T> void writeServiceResult(HttpServletResponse response, ServiceResult<T> result, int successStatus,
                                        Function<T, Object> mapper) throws IOException {
        if (result.successful()) {
            ApiResponses.write(response, successStatus, mapper.apply(result.value()));
            return;
        }
        int status = switch (result.status()) {
            case VALIDATION_ERROR -> HttpServletResponse.SC_BAD_REQUEST;
            case CONFLICT, PROTECTED -> HttpServletResponse.SC_CONFLICT;
            case NOT_FOUND -> HttpServletResponse.SC_NOT_FOUND;
            case SUCCESS -> successStatus;
        };
        String code = switch (result.status()) {
            case VALIDATION_ERROR -> "VALIDATION_ERROR";
            case CONFLICT -> "CONFLICT";
            case PROTECTED -> "PROTECTED_RESOURCE";
            case NOT_FOUND -> "NOT_FOUND";
            case SUCCESS -> "SUCCESS";
        };
        ApiResponses.error(response, status, code, result.error());
    }

    private boolean requirePermission(HttpServletRequest request, HttpServletResponse response, String permission) throws IOException {
        User user = requireAuthenticated(request, response);
        if (user == null) return false;
        if (!PermissionAccess.hasPermission(user, permission)) {
            ApiResponses.error(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "You do not have permission for this operation.");
            return false;
        }
        return true;
    }

    private User requireAuthenticated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = signedInUser(request);
        if (user == null) {
            ApiResponses.error(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required.");
        }
        return user;
    }

    private User signedInUser(HttpServletRequest request) {
        Object value = request.getAttribute("signedInUser");
        return value instanceof User user ? user : null;
    }

    private String path(HttpServletRequest request) {
        String value = request.getPathInfo();
        return value == null || value.isBlank() ? "/" : value;
    }

    private Long idAfter(String path, String prefix) {
        if (!path.startsWith(prefix)) return null;
        String suffix = path.substring(prefix.length());
        return suffix.contains("/") ? null : positiveLong(suffix);
    }

    private Long idBeforeSuffix(String path, String prefix, String suffix) {
        if (!path.startsWith(prefix) || !path.endsWith(suffix)) return null;
        return positiveLong(path.substring(prefix.length(), path.length() - suffix.length()));
    }

    private Long positiveLong(String value) {
        try {
            long id = Long.parseLong(value);
            return id > 0 ? id : null;
        } catch (NumberFormatException | NullPointerException exception) {
            return null;
        }
    }

    private Set<String> safeFunctions(List<String> functions) {
        if (functions == null) return Set.of();
        return functions.stream().filter(function -> function != null && !function.isBlank()).collect(java.util.stream.Collectors.toSet());
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private void notFound(HttpServletResponse response) throws IOException {
        ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND, "ENDPOINT_NOT_FOUND", "The requested endpoint was not found.");
    }

    private String resetLinkPrefix(HttpServletRequest request) throws SQLException {
        String clientUrl = System.getenv("PASSWORD_RESET_CLIENT_URL");
        if (clientUrl == null || clientUrl.isBlank()) {
            throw new SQLException("PASSWORD_RESET_CLIENT_URL must point to the client reset-password page.");
        }
        return clientUrl.trim() + (clientUrl.contains("?") ? "&token=" : "?token=");
    }

    private record LoginRequest(String username, String password) { }
    private record RegisterRequest(String username, String email, String password) { }
    private record CreateUserRequest(String username, String email, String password, Long roleId) { }
    private record UserUpdateRequest(String username, String email, Long roleId) { }
    private record PasswordRequest(String currentPassword, String newPassword, String confirmation) { }
    private record ForgotPasswordRequest(String email) { }
    private record ResetPasswordRequest(String token, String newPassword, String confirmation) { }
    private record RoleRequest(String name, List<String> functions) { }
}
