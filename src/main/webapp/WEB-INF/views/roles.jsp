<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <title>Manage roles</title>
</head>
<body>
<main class="wide-card">
    <h1>Manage roles</h1>
    <c:if test="${param.message == 'roleCreated'}"><p class="message success">Role created.</p></c:if>
    <c:if test="${param.message == 'roleUpdated'}"><p class="message success">Role updated.</p></c:if>
    <c:if test="${param.message == 'roleDeleted'}"><p class="message success">Role deleted.</p></c:if>
    <c:if test="${param.message == 'defaultRoleProtected'}"><p class="message error">The default registration role cannot be deleted.</p></c:if>
    <c:if test="${param.message == 'roleInUse'}"><p class="message error">Assign users to another role before deleting this role.</p></c:if>
    <c:if test="${not empty error}"><p class="message error"><c:out value="${error}" /></p></c:if>

    <h2>Create role</h2>
    <form method="post" action="${pageContext.request.contextPath}/roles">
        <input name="roleName" type="text" value="<c:out value='${roleName}' />" placeholder="Role name"
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
        <button type="submit">Create role</button>
    </form>

    <h2>Existing roles</h2>
    <table>
        <thead><tr><th>Role</th><th>Functions</th><th>Actions</th></tr></thead>
        <tbody>
        <c:forEach var="role" items="${roles}">
            <tr>
                <td><c:out value="${role.name}" /><c:if test="${role.defaultRole}"> (default)</c:if></td>
                <td>
                    <c:forEach var="function" items="${role.functions}">
                        <div><c:out value="${function.code}" /></div>
                    </c:forEach>
                </td>
                <td class="actions">
                    <a href="${pageContext.request.contextPath}/edit-role?id=${role.id}">Edit</a>
                    <form method="post" action="${pageContext.request.contextPath}/delete-role">
                        <input name="roleId" type="hidden" value="${role.id}">
                        <button type="submit">Delete</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <p class="hint"><a href="${pageContext.request.contextPath}/home">Back to users</a></p>
</main>
</body>
</html>
