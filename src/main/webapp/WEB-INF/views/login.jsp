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
    <c:if test="${not empty error}">
        <p class="message error"><c:out value="${error}" /></p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/login">
        <label for="username">Username</label>
        <input id="username" name="username" required
               value="<c:out value='${username}' />">

        <label for="password">Password</label>
        <input id="password" name="password" type="password" required>

        <button type="submit">Log in</button>
    </form>

    <p class="hint">Need an account? <a href="${pageContext.request.contextPath}/register">Register</a></p>
</main>
</body>
</html>
