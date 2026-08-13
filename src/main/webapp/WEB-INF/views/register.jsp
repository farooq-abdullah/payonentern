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
        <input name="username" type="text" value="<c:out value='${username}' />"
               placeholder="Username" required minlength="3" maxlength="50"
               pattern="[A-Za-z0-9._-]{3,50}"
               title="Use 3 to 50 letters, numbers, dots, underscores, or hyphens."
               autocomplete="username">
        <input name="email" type="email" value="<c:out value='${email}' />"
               placeholder="Email" required maxlength="254"
               pattern="[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,63}"
               title="Enter a valid email address."
               autocomplete="email">
        <input name="password" type="password" placeholder="Password" required minlength="8"
               pattern="(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9 ]).{8,}"
               title="Use at least 8 characters with uppercase, lowercase, digit, and special characters."
               autocomplete="new-password">
        <button type="submit">Create account</button>
    </form>

    <p class="hint">Passwords need at least 8 characters with uppercase, lowercase, digit, and special characters.</p>
    <p class="hint">Already registered? <a href="${pageContext.request.contextPath}/login">Log in</a></p>
</main>
</body>
</html>
