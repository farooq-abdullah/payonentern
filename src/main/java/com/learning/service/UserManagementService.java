package com.learning.service;

import com.learning.dao.HibernateRoleDao;
import com.learning.dao.HibernateUserDao;
import com.learning.dao.RoleDao;
import com.learning.dao.UserDao;
import com.learning.model.Role;
import com.learning.model.User;
import com.learning.util.FullAdminProtection;
import com.learning.util.PasswordHasher;
import com.learning.util.PasswordPolicy;
import com.learning.util.UserInputValidator;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

/** Business rules for creating, editing, and deleting users. */
public class UserManagementService {
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final AuditService auditService;

    public UserManagementService() {
        this(new HibernateUserDao(), new HibernateRoleDao(), new AuditService());
    }

    UserManagementService(UserDao userDao, RoleDao roleDao, AuditService auditService) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.auditService = auditService;
    }

    public ServiceResult<User> register(String username, String email, String password) throws SQLException {
        String inputError = UserInputValidator.validationError(trim(username), trim(email));
        if (inputError != null) return ServiceResult.validationError(inputError);
        String passwordError = PasswordPolicy.validationError(password);
        if (passwordError != null) return ServiceResult.validationError(passwordError);
        if (userDao.existsByUsernameOrEmail(trim(username), trim(email))) {
            return ServiceResult.conflict("That username or email is already registered.");
        }

        Optional<Role> role = userDao.countAll() == 0
                ? FullAdminProtection.findFullAdministratorRole(roleDao)
                : roleDao.findDefaultRole();
        if (role.isEmpty()) return ServiceResult.validationError("No role is available for new users.");

        User user = new User();
        user.setUsername(trim(username));
        user.setEmail(trim(email));
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setRole(role.get());
        userDao.create(user);
        auditService.record(user, "USER_CREATED", "USER", user.getId(), user.getUsername(), true,
                "Self registration with role " + role.get().getName());
        return ServiceResult.success(user);
    }

    /** Creates a user with an explicitly selected role for an administrator-managed API client. */
    public ServiceResult<User> createByAdministrator(User actor, String username, String email, String password, long roleId)
            throws SQLException {
        String inputError = UserInputValidator.validationError(trim(username), trim(email));
        if (inputError != null) return ServiceResult.validationError(inputError);
        String passwordError = PasswordPolicy.validationError(password);
        if (passwordError != null) return ServiceResult.validationError(passwordError);
        if (userDao.existsByUsernameOrEmail(trim(username), trim(email))) {
            return ServiceResult.conflict("That username or email is already registered.");
        }
        Optional<Role> role = roleDao.findById(roleId);
        if (role.isEmpty()) return ServiceResult.validationError("The selected role does not exist.");

        User user = new User();
        user.setUsername(trim(username));
        user.setEmail(trim(email));
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setRole(role.get());
        userDao.create(user);
        auditService.record(actor, "USER_CREATED", "USER", user.getId(), user.getUsername(), true,
                "Administrator created user with role " + role.get().getName());
        return ServiceResult.success(user);
    }

    public ServiceResult<User> updateProfile(User actor, long userId, String username, String email, long roleId)
            throws SQLException {
        Optional<User> existing = userDao.findById(userId);
        if (existing.isEmpty()) return ServiceResult.notFound("User was not found.");
        Optional<Role> newRole = roleDao.findById(roleId);
        if (newRole.isEmpty()) return ServiceResult.validationError("The selected role does not exist.");

        String inputError = UserInputValidator.validationError(trim(username), trim(email));
        if (inputError != null) return ServiceResult.validationError(inputError);
        if (userDao.existsByUsernameOrEmailExceptId(trim(username), trim(email), userId)) {
            return ServiceResult.conflict("That username or email is already registered.");
        }

        Set<String> allFunctions = roleDao.findAllFunctionCodes();
        boolean losesFullAdministration = FullAdminProtection.isFullAdministrator(existing.get().getRole(), allFunctions)
                && !FullAdminProtection.isFullAdministrator(newRole.get(), allFunctions);
        if (losesFullAdministration && FullAdminProtection.countFullAdministrators(roleDao, userDao) <= 1) {
            return ServiceResult.protectedOperation("At least one user must keep full administrative permissions.");
        }

        User updated = existing.get();
        updated.setUsername(trim(username));
        updated.setEmail(trim(email));
        updated.setRole(newRole.get());
        userDao.updateProfile(updated);
        auditService.record(actor, "USER_UPDATED", "USER", userId, updated.getUsername(), true,
                "Role set to " + newRole.get().getName());
        return ServiceResult.success(updated);
    }

    public ServiceResult<User> delete(User actor, long userId) throws SQLException {
        Optional<User> found = userDao.findById(userId);
        if (found.isEmpty()) return ServiceResult.notFound("User was not found.");
        if (FullAdminProtection.isFullAdministrator(found.get().getRole(), roleDao.findAllFunctionCodes())
                && FullAdminProtection.countFullAdministrators(roleDao, userDao) <= 1) {
            return ServiceResult.protectedOperation("At least one user must keep full administrative permissions.");
        }
        User deleted = found.get();
        userDao.deleteById(userId);
        auditService.record(actor, "USER_DELETED", "USER", userId, deleted.getUsername(), true, null);
        return ServiceResult.success(deleted);
    }

    public Optional<User> findById(long userId) throws SQLException {
        return userDao.findById(userId);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
