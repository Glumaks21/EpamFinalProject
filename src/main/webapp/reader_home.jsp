<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false"%>
<html>
<head>
    <link rel="icon" type="image/x-icon" href="static/images/title_icon.png">
    <title>Library</title>
</head>
<body>
    <c:if test="${user != null}">
        Hello ${user.role}
    </c:if>

    Select Language
    <select name="language">
        <c:forEach var="language" items="${languages}">
            <option>${language}</option>
        </c:forEach>
    </select>

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
                    <td>
                        <a href="book?id=${book.id}">${book.title}</a>
                    </td>
                    <td>
                        <a href="?author_id=${book.author.id}">${book.author.name} ${book.author.surname}</a>
                    </td>
                    <td>
                        <a href="?publisher_name=${book.publisher.name}">${book.publisher.name}</a>
                    </td>
                    <td>${book.date}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</body>
</html>
