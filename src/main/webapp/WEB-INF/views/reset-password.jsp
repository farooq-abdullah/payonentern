<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <title>Reset password</title>
</head>
<body>
<main class="card">
    <h1>Reset password</h1>
    <p><c:out value="${user.username}" /></p>
    <c:if test="${not empty error}">
        <p class="message error"><c:out value="${error}" /></p>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/reset-password">
        <input name="userId" type="hidden" value="${user.id}">
        <input name="newPassword" type="password" placeholder="Temporary password" required minlength="8"
               pattern="(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9 ]).{8,}"
               title="Use at least 8 characters with uppercase, lowercase, digit, and special characters."
               autocomplete="new-password">
        <input name="confirmation" type="password" placeholder="Confirm temporary password"
               required minlength="8" autocomplete="new-password">
        <button type="submit">Reset password</button>
    </form>
    <p class="hint">The user must change this password after logging in.</p>
</main>
</body>
</html>
