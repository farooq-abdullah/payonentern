<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <title>Edit role</title>
</head>
<body>
<main class="card">
    <h1>Edit role</h1>
    <c:if test="${not empty error}"><p class="message error"><c:out value="${error}" /></p></c:if>
    <form method="post" action="${pageContext.request.contextPath}/edit-role">
        <input name="roleId" type="hidden" value="${role.id}">
        <input name="roleName" type="text" value="<c:out value='${role.name}' />" placeholder="Role name"
               required minlength="3" maxlength="50" pattern="[A-Za-z0-9 _-]{3,50}">
        <fieldset>
            <legend>Functions</legend>
            <c:forEach var="function" items="${functions}">
                <label class="checkbox-label">
                    <input type="checkbox" name="functions" value="${function.code}"
                           <c:if test="${selectedFunctions[function.code]}">checked</c:if>>
                    <c:out value="${function.code}" />
                </label>
            </c:forEach>
        </fieldset>
        <button type="submit">Save role</button>
    </form>
    <p class="hint"><a href="${pageContext.request.contextPath}/roles">Back to roles</a></p>
</main>
</body>
</html>
