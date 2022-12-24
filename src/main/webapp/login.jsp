<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Login</title>
</head>
<body>
<a href="/">Home</a>

<c:if test="${message != null}">
    <h1>${message}</h1>
</c:if>

<h1>Login</h1>
<form method="POST" action="/profile/login">
    <label>
        <input type="text" name="login">
    </label>
    <label>
        <input type="password" name="password">
    </label>
    <input type="submit"/>
</form>
    <a href="/profile/registration">I haven't an account</a>
</body>
</html>

