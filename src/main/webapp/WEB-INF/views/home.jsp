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
    <c:if test="${param.message == 'lastAdminProtected'}">
        <p class="message error">The last user with full administrative permissions cannot be deleted.</p>
    </c:if>

    <c:choose>
        <c:when test="${empty users}">
            <p class="message">No users are registered yet.</p>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                <tr>
                    <th>ID</th><th>Username</th><th>Email</th><th>Role</th><th>Created</th>
                    <c:if test="${canEditUser or canResetPassword or canDeleteUser}"><th>Actions</th></c:if>
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
                        <c:if test="${canEditUser or canResetPassword or canDeleteUser}">
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
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
