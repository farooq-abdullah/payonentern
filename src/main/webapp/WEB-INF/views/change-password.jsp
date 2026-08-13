<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <title>Change password</title>
</head>
<body>
<main class="card">
    <h1>Change password</h1>
    <c:if test="${param.required == 'true'}">
        <p class="message error">Your password must be changed before you can continue.</p>
    </c:if>
    <c:if test="${not empty error}">
        <p class="message error"><c:out value="${error}" /></p>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/change-password">
        <input name="currentPassword" type="password" placeholder="Current password"
               required autocomplete="current-password">
        <input name="newPassword" type="password" placeholder="New password" required minlength="8"
               pattern="(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9 ]).{8,}"
               title="Use at least 8 characters with uppercase, lowercase, digit, and special characters."
               autocomplete="new-password">
        <input name="confirmation" type="password" placeholder="Confirm new password"
               required minlength="8" autocomplete="new-password">
        <button type="submit">Change password</button>
    </form>
    <p class="hint">Use at least 8 characters with uppercase, lowercase, digit, and special characters.</p>
</main>
</body>
</html>
