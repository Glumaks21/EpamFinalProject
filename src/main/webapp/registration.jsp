<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Registration</title>
</head>
<body>
<a href="/">Home</a>

<h1>Registration</h1>
<form method="POST" action="/profile/registration">
    <label>
        <input type="text" name="login">
    </label>
    <label>
        <input type="password" name="password">
    </label>
    <label>
        <input type="password" name="password_confirmation">
    </label>
    <input type="submit"/>
</form>

<c:if test="${message != null}">
    <h1>${message}</h1>
</c:if>

<a href="/profile/login">I haven an account</a>
</body>
</body>
</html>
