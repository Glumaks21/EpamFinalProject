<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="paramName" type="java.lang.String" required="true" %>
<%@ attribute name="paramValue" type="java.lang.String" required="true" %>

<c:url value="">
    <c:forEach items="${param}" var="entry">
        <c:if test="${entry.key != paramName}">
            <c:param name="${entry.key}" value="${entry.value}" />
        </c:if>
    </c:forEach>
    <c:param name="${paramName}" value="${paramValue}" />
</c:url>

