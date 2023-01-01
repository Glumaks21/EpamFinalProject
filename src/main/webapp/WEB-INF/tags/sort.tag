<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" tagdir="/WEB-INF/tags" %>

<%@ attribute name="category" type="java.lang.String" required="true" %>

<c:if test="${param.sort == category.concat('_asc') ||
    (param.sort != category.concat('_asc') && param.sort != category.concat('_desc'))}">
    <a href="<my:change_param paramName="sort" paramValue="${category.concat('_desc')}"/>">
        <img src="<c:url value="/static/images/arrow_down.png"/>">
    </a>
</c:if>

<c:if test="${param.sort == category.concat('_desc')}">
    <a href="<my:change_param paramName="sort" paramValue="${category.concat('_asc')}"/>">
        <img src="<c:url value="/static/images/arrow_up.png"/>">
    </a>
</c:if>

