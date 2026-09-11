package com.learning.service;

import com.learning.dao.HibernateRoleDao;
import com.learning.dao.HibernateUserDao;
import com.learning.dao.RoleDao;
import com.learning.dao.UserDao;
import com.learning.model.Role;
import com.learning.model.Permission;
import com.learning.model.User;
import com.learning.util.FullAdminProtection;
import com.learning.util.RoleInputValidator;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;
import java.util.List;

/** Business rules for role/function administration. */
public class RoleManagementService {
    private final RoleDao roleDao;
    private final UserDao userDao;
    private final AuditService auditService;

    public RoleManagementService() {
        this(new HibernateRoleDao(), new HibernateUserDao(), new AuditService());
    }

    RoleManagementService(RoleDao roleDao, UserDao userDao, AuditService auditService) {
        this.roleDao = roleDao;
        this.userDao = userDao;
        this.auditService = auditService;
    }

    public ServiceResult<Role> create(User actor, String name, Set<String> functionCodes) throws SQLException {
        String error = RoleInputValidator.validationError(trim(name));
        if (error != null) return ServiceResult.validationError(error);
        if (roleDao.findByName(trim(name)).isPresent()) return ServiceResult.conflict("That role name is already in use.");
        if (!roleDao.findAllFunctionCodes().containsAll(functionCodes)) {
            return ServiceResult.validationError("One or more selected functions do not exist.");
        }
        roleDao.create(trim(name), functionCodes);
        Role role = roleDao.findByName(trim(name)).orElseThrow();
        auditService.record(actor, "ROLE_CREATED", "ROLE", role.getId(), role.getName(), true,
                "Assigned " + functionCodes.size() + " functions");
        return ServiceResult.success(role);
    }

    public ServiceResult<Role> update(User actor, long roleId, String name, Set<String> functionCodes) throws SQLException {
        Optional<Role> found = roleDao.findById(roleId);
        if (found.isEmpty()) return ServiceResult.notFound("Role was not found.");
        String error = RoleInputValidator.validationError(trim(name));
        if (error != null) return ServiceResult.validationError(error);
        Optional<Role> sameName = roleDao.findByName(trim(name));
        if (sameName.isPresent() && sameName.get().getId() != roleId) {
            return ServiceResult.conflict("That role name is already in use.");
        }

        Set<String> allFunctions = roleDao.findAllFunctionCodes();
        if (!allFunctions.containsAll(functionCodes)) {
            return ServiceResult.validationError("One or more selected functions do not exist.");
        }
        boolean removesFullAdministration = FullAdminProtection.isFullAdministrator(found.get(), allFunctions)
                && !FullAdminProtection.selectedFunctionsAreFullAdministrative(functionCodes, allFunctions);
        if (removesFullAdministration
                && FullAdminProtection.countFullAdministrators(roleDao, userDao) <= userDao.countByRoleId(roleId)) {
            return ServiceResult.protectedOperation("At least one user must keep full administrative permissions.");
        }
        roleDao.update(roleId, trim(name), functionCodes);
        Role updated = roleDao.findById(roleId).orElseThrow();
        auditService.record(actor, "ROLE_UPDATED", "ROLE", roleId, updated.getName(), true,
                "Assigned " + functionCodes.size() + " functions");
        return ServiceResult.success(updated);
    }

    public ServiceResult<Role> delete(User actor, long roleId) throws SQLException {
        Optional<Role> found = roleDao.findById(roleId);
        if (found.isEmpty()) return ServiceResult.notFound("Role was not found.");
        if (found.get().isDefaultRole()) return ServiceResult.protectedOperation("The default role cannot be deleted.");
        if (userDao.countByRoleId(roleId) > 0) return ServiceResult.conflict("This role is assigned to one or more users.");
        Role deleted = found.get();
        roleDao.deleteById(roleId);
        auditService.record(actor, "ROLE_DELETED", "ROLE", roleId, deleted.getName(), true, null);
        return ServiceResult.success(deleted);
    }

    public List<Role> findAll() throws SQLException {
        return roleDao.findAll();
    }

    public Optional<Role> findById(long roleId) throws SQLException {
        return roleDao.findById(roleId);
    }

    public List<Permission> findAllFunctions() throws SQLException {
        return roleDao.findAllFunctions();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
