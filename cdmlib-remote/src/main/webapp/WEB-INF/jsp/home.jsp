<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<title>Spring MVC Tutorial (Home Page)</title>
</head>
<body>
<h1>Good <c:out value="${greeting}"/>! Welcome to Spring MVC Tutorial</h1>
<p align="center">
	The time on the server is <c:out value="${time}"/>
</p>
<p align="center">
	<b>Here are ten random integers:</b><br/>
</p>
<p><img src="<c:url value="/images/poweredBySpring.gif"/>" alt="Powered By Spring"/></p>
</body>
</html>
