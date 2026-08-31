<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <title>Forgot password</title>
</head>
<body>
<main class="card">
    <h1>Reset your password</h1>
    <c:if test="${param.sent == 'true'}">
        <p class="message success">If an account uses that email, a reset link has been sent.</p>
    </c:if>
    <c:if test="${not empty error}"><p class="message error"><c:out value="${error}" /></p></c:if>
    <form method="post" action="${pageContext.request.contextPath}/forgot-password">
        <label for="email">Email</label>
        <input id="email" name="email" type="email" required maxlength="254" autocomplete="email">
        <button type="submit">Email reset link</button>
    </form>
    <p class="hint"><a href="${pageContext.request.contextPath}/login">Back to log in</a></p>
</main>
</body>
</html>
