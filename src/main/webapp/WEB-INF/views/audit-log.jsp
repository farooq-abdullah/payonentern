<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <title>Audit log</title>
</head>
<body>
<main class="wide-card">
    <header class="page-header"><h1>Audit log</h1><a class="button secondary" href="${pageContext.request.contextPath}/home">Back</a></header>
    <form class="filters" method="get" action="${pageContext.request.contextPath}/audit-log">
        <input name="actor" placeholder="Actor username" value="<c:out value='${param.actor}' />">
        <input name="action" placeholder="Action, e.g. LOGIN_FAILED" value="<c:out value='${param.action}' />">
        <input name="targetType" placeholder="Target type, e.g. USER" value="<c:out value='${param.targetType}' />">
        <select name="successful"><option value="">All outcomes</option><option value="true" ${param.successful == 'true' ? 'selected' : ''}>Successful</option><option value="false" ${param.successful == 'false' ? 'selected' : ''}>Failed</option></select>
        <button type="submit">Filter</button>
    </form>
    <table>
        <thead><tr><th>When</th><th>Actor</th><th>Action</th><th>Target</th><th>Outcome</th><th>Details</th></tr></thead>
        <tbody>
        <c:forEach var="entry" items="${auditPage.entries}">
            <tr><td><c:out value="${entry.createdAt}" /></td><td><c:out value="${entry.actorUsername}" /></td>
                <td><c:out value="${entry.action}" /></td><td><c:out value="${entry.targetType}" /> #<c:out value="${entry.targetId}" /> <c:out value="${entry.targetLabel}" /></td>
                <td><c:out value="${entry.successful ? 'Success' : 'Failed'}" /></td><td><c:out value="${entry.details}" /></td></tr>
        </c:forEach>
        </tbody>
    </table>
    <c:if test="${auditPage.totalPages > 1}"><nav class="pagination">
        <c:if test="${auditPage.page > 1}"><a href="${pageContext.request.contextPath}/audit-log?action=${param.action}&actor=${param.actor}&targetType=${param.targetType}&successful=${param.successful}&page=${auditPage.page - 1}">Previous</a></c:if>
        <span>Page <c:out value="${auditPage.page}" /> of <c:out value="${auditPage.totalPages}" /></span>
        <c:if test="${auditPage.page < auditPage.totalPages}"><a href="${pageContext.request.contextPath}/audit-log?action=${param.action}&actor=${param.actor}&targetType=${param.targetType}&successful=${param.successful}&page=${auditPage.page + 1}">Next</a></c:if>
    </nav></c:if>
</main>
</body>
</html>
