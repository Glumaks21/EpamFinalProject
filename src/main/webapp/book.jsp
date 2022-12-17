<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link rel="icon" type="image/x-icon" href="static/images/title_icon.png">
    <title>${book.title}</title>
</head>
<body>
    <table>
        <thead>
            <tr>
                <th>
                    Test
                </th>
                <th>
                    Description
                </th>
            </tr>
        </thead>
        <tbody>
        <tr>
            <td>
                Picture
            </td>
            <td>
                ${book.description}
            </td>
        </tr>
        </tbody>
    </table>
</body>
</html>
