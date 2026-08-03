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
        <input name="username" type="text">
        <input name="email" type="email">
        <input name="password" type="password">
        <button type="submit">Create account</button>
    </form>

    <p class="hint">Already registered? <a href="${pageContext.request.contextPath}/login">Log in</a></p>
</main>
</body>
</html>
