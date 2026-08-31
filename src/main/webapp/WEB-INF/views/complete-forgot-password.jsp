<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <title>Choose a new password</title>
</head>
<body>
<main class="card">
    <h1>Choose a new password</h1>
    <c:if test="${not empty error}"><p class="message error"><c:out value="${error}" /></p></c:if>
    <form method="post" action="${pageContext.request.contextPath}/complete-forgot-password">
        <input name="token" type="hidden" value="<c:out value='${param.token}' />">
        <label for="newPassword">New password</label>
        <input id="newPassword" name="newPassword" type="password" required minlength="8" autocomplete="new-password">
        <label for="confirmation">Confirm new password</label>
        <input id="confirmation" name="confirmation" type="password" required minlength="8" autocomplete="new-password">
        <button type="submit">Reset password</button>
    </form>
</main>
</body>
</html>
