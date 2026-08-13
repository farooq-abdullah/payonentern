<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <title>Edit user</title>
</head>
<body>
<main class="card">
    <h1>Edit user</h1>
    <c:if test="${not empty error}">
        <p class="message error"><c:out value="${error}" /></p>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/edit-user">
        <input name="userId" type="hidden" value="${user.id}">
        <input name="username" type="text" value="<c:out value='${user.username}' />"
               placeholder="Username" required minlength="3" maxlength="50"
               pattern="[A-Za-z0-9._-]{3,50}"
               title="Use 3 to 50 letters, numbers, dots, underscores, or hyphens."
               autocomplete="username">
        <input name="email" type="email" value="<c:out value='${user.email}' />"
               placeholder="Email" required maxlength="254"
               pattern="[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,63}"
               title="Enter a valid email address."
               autocomplete="email">
        <button type="submit">Save changes</button>
    </form>
    <p class="hint"><a href="${pageContext.request.contextPath}/home">Back to users</a></p>
</main>
</body>
</html>
