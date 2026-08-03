<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <title>Register</title>
</head>
<body>
<main class="card">
    <h1>Create account</h1>

    <c:if test="${not empty error}">
        <p class="message error"><c:out value="${error}" /></p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/register">
        <label for="username">Username</label>
        <input id="username" name="username" maxlength="50" required value="<c:out value='${username}' />">

        <label for="email">Email</label>
        <input id="email" name="email" type="email" maxlength="254" required value="<c:out value='${email}' />">

        <label for="password">Password</label>
        <input id="password" name="password" type="password" minlength="8" required>

        <button type="submit">Register</button>
    </form>

    <p class="hint">Already registered? <a href="${pageContext.request.contextPath}/login">Log in</a></p>
</main>
</body>
</html>
