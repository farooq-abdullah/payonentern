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
            <button class="secondary" type="submit">Log out</button>
        </form>
    </header>

    <c:choose>
        <c:when test="${empty users}">
            <p class="message">No users are registered yet.</p>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                <tr><th>ID</th><th>Username</th><th>Email</th><th>Created</th></tr>
                </thead>
                <tbody>
                <c:forEach var="user" items="${users}">
                    <tr>
                        <td><c:out value="${user.id}" /></td>
                        <td><c:out value="${user.username}" /></td>
                        <td><c:out value="${user.email}" /></td>
                        <td><c:out value="${user.createdAt}" /></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
