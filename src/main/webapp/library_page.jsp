<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false"%>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <a href="profile/login">Login</a>
    <a href="profile/registration">Registration</a>

    <table>
        <thead>
            <tr>
                <th>Title</th>
                <th>Author</th>
                <th>Publisher</th>
                <th>Date</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="book" items="${books}">
                <tr>
                    <td>${book.title}</td>
                    <td>${book.author.name} ${book.author.surname}</td>
                    <td>${book.publisher.name}</td>
                    <td>${book.date}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</body>
</html>
