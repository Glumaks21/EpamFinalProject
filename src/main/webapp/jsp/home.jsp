<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" tagdir="/WEB-INF/tags" %>

<html>
<head>
    <title>Library</title>
</head>
<body>
    <c:if test="${sessionScope.userId == null}">
        <a href="<c:url value="/profile/login"/>">Login</a>
        <a href="<c:url value="/profile/registration"/>">Registration</a>
    </c:if>
    <c:if test="${sessionScope.userId != null}">
        Hello ${sessionScope.role}
        <a href="<c:url value="/profile/logout"/>">Logout</a>
    </c:if>

    <table>
        <thead>
            <tr>
                <th>
                    Title
                    <my:sort category="title"/>
                </th>
                <th>
                    Author
                    <my:sort category="author"/>
                </th>
                <th>
                    Publisher
                    <my:sort category="publisher"/>
                </th>
                <th>
                    Date
                    <my:sort category="date"/>
                </th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="book" items="${requestScope.books}">
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
