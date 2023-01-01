<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         isELIgnored="false" session="false"%>
<html>
<head>
    <title>Login</title>
</head>
<body>
<a href="<c:url value="/"/>">Home</a>

<c:if test="${requestScope.message != null}">
    <h1>${requestScope.message}</h1>
</c:if>

<h1>Login</h1>
<form method="post" action="<c:url value="/profile/login"/>">
    <label>
        <input type="text" name="login">
    </label>
    <label>
        <input type="password" name="password">
    </label>
    <input type="submit"/>
</form>
    <a href="<c:url value="/profile/registration"/>">I haven't an account</a>
</body>
</html>

