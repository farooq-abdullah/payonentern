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

    <p><a class="button" href="${pageContext.request.contextPath}/change-password">Change password</a></p>

    <c:if test="${param.message == 'profileUpdated'}">
        <p class="message success">User profile updated.</p>
    </c:if>
    <c:if test="${param.message == 'passwordChanged'}">
        <p class="message success">Password changed.</p>
    </c:if>
    <c:if test="${param.message == 'passwordReset'}">
        <p class="message success">Password reset. The user must change it after logging in.</p>
    </c:if>
    <c:if test="${param.message == 'userDeleted'}">
        <p class="message success">User deleted.</p>
    </c:if>

    <c:choose>
        <c:when test="${empty users}">
            <p class="message">No users are registered yet.</p>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                <tr><th>ID</th><th>Username</th><th>Email</th><th>Created</th><th>Actions</th></tr>
                </thead>
                <tbody>
                <c:forEach var="user" items="${users}">
                    <tr>
                        <td><c:out value="${user.id}" /></td>
                        <td><c:out value="${user.username}" /></td>
                        <td><c:out value="${user.email}" /></td>
                        <td><c:out value="${user.createdAt}" /></td>
                        <td class="actions">
                            <a href="${pageContext.request.contextPath}/edit-user?id=${user.id}">Edit</a>
                            <a href="${pageContext.request.contextPath}/reset-password?id=${user.id}">Reset password</a>
                            <form method="post" action="${pageContext.request.contextPath}/delete-user">
                                <input name="userId" type="hidden" value="${user.id}">
                                <button type="submit">Delete</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
