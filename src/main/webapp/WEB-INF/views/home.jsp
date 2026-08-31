<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <title>Registered users</title>
</head>
<body>
<main class="wide-card">
    <header class="page-header">
        <div>
            <h1>Registered users</h1>
            <p>Signed in as <strong><c:out value="${sessionScope.loggedInUsername}" /></strong></p>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/logout">
            <button type="submit">Log out</button>
        </form>
    </header>

    <c:if test="${canChangeOwnPassword}">
        <p><a class="button" href="${pageContext.request.contextPath}/change-password">Change password</a></p>
    </c:if>
    <c:if test="${canManageRoles}">
        <p><a class="button" href="${pageContext.request.contextPath}/roles">Manage roles</a></p>
    </c:if>
    <c:if test="${canViewAuditLog}"><p><a class="button" href="${pageContext.request.contextPath}/audit-log">View audit log</a></p></c:if>

    <c:if test="${param.message == 'passwordChanged'}">
        <p class="message success">Password changed.</p>
    </c:if>
    <c:if test="${param.message == 'profileUpdated'}">
        <p class="message success">User profile updated.</p>
    </c:if>
    <c:if test="${param.message == 'passwordReset'}">
        <p class="message success">Password reset.</p>
    </c:if>
    <c:if test="${param.message == 'userDeleted'}">
        <p class="message success">User deleted.</p>
    </c:if>
    <c:if test="${param.message == 'userUnlocked'}"><p class="message success">Account unlocked.</p></c:if>
    <c:if test="${param.message == 'lastAdminProtected'}">
        <p class="message error">The last user with full administrative permissions cannot be deleted.</p>
    </c:if>

    <form class="filters" method="get" action="${pageContext.request.contextPath}/home">
        <input name="search" value="<c:out value='${param.search}' />" placeholder="Search username or email">
        <button type="submit">Search</button>
    </form>

    <c:set var="nextDir" value="${param.dir == 'asc' ? 'desc' : 'asc'}" />
    <c:choose>
        <c:when test="${empty users}">
            <p class="message">No users are registered yet.</p>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                <tr>
                    <th><a href="${pageContext.request.contextPath}/home?search=${param.search}&sort=id&dir=${nextDir}">ID</a></th>
                    <th><a href="${pageContext.request.contextPath}/home?search=${param.search}&sort=username&dir=${nextDir}">Username</a></th>
                    <th><a href="${pageContext.request.contextPath}/home?search=${param.search}&sort=email&dir=${nextDir}">Email</a></th><th>Role</th>
                    <th><a href="${pageContext.request.contextPath}/home?search=${param.search}&sort=created&dir=${nextDir}">Created</a></th>
                    <c:if test="${canEditUser or canResetPassword or canDeleteUser or canUnlockUser}"><th>Actions</th></c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="user" items="${users}">
                    <tr>
                        <td><c:out value="${user.id}" /></td>
                        <td><c:out value="${user.username}" /></td>
                        <td><c:out value="${user.email}" /></td>
                        <td><c:out value="${user.role.name}" /></td>
                        <td><c:out value="${user.createdAt}" /></td>
                        <c:if test="${canEditUser or canResetPassword or canDeleteUser or canUnlockUser}">
                            <td class="actions">
                                <c:if test="${canEditUser}">
                                    <a href="${pageContext.request.contextPath}/edit-user?id=${user.id}">Edit</a>
                                </c:if>
                                <c:if test="${canResetPassword}">
                                    <a href="${pageContext.request.contextPath}/reset-password?id=${user.id}">Reset password</a>
                                </c:if>
                                <c:if test="${canDeleteUser}">
                                    <form method="post" action="${pageContext.request.contextPath}/delete-user">
                                        <input name="userId" type="hidden" value="${user.id}">
                                        <button type="submit">Delete</button>
                                    </form>
                                </c:if>
                                <c:if test="${canUnlockUser and user.lockedUntil != null}">
                                    <form method="post" action="${pageContext.request.contextPath}/unlock-user"><input name="userId" type="hidden" value="${user.id}"><button type="submit">Unlock</button></form>
                                </c:if>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
    <c:if test="${userPage.totalPages > 1}"><nav class="pagination">
        <c:if test="${userPage.page > 1}"><a href="${pageContext.request.contextPath}/home?search=${param.search}&sort=${param.sort}&dir=${param.dir}&page=${userPage.page - 1}">Previous</a></c:if>
        <span>Page <c:out value="${userPage.page}" /> of <c:out value="${userPage.totalPages}" /> (<c:out value="${userPage.totalUsers}" /> users)</span>
        <c:if test="${userPage.page < userPage.totalPages}"><a href="${pageContext.request.contextPath}/home?search=${param.search}&sort=${param.sort}&dir=${param.dir}&page=${userPage.page + 1}">Next</a></c:if>
    </nav></c:if>
</main>
</body>
</html>
