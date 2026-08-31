<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <title>Log in</title>
</head>
<body>
<main class="card">
    <h1>Log in</h1>

    <c:if test="${param.registered == 'true'}">
        <p class="message success">Account created. You can log in now.</p>
    </c:if>
    <c:if test="${param.passwordReset == 'true'}">
        <p class="message success">Password reset. You can log in now.</p>
    </c:if>
    <c:if test="${not empty error}">
        <p class="message error"><c:out value="${error}" /></p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/login">
        <input name="username" type="text" value="<c:out value='${username}' />"
               placeholder="Username" required maxlength="50" autocomplete="username">
        <input name="password" type="password" placeholder="Password"
               required autocomplete="current-password">
        <button type="submit">Log in</button>
    </form>

    <p class="hint">Need an account? <a href="${pageContext.request.contextPath}/register">Register</a></p>
    <p class="hint"><a href="${pageContext.request.contextPath}/forgot-password">Forgot your password?</a></p>
</main>
</body>
</html>
