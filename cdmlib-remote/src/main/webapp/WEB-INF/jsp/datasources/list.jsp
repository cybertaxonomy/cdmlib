<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
	<title>Error</title>
</head>
<body>
<h1>HTTP Error <%= request.getHeader("status") %></h1>
${exception.message}
</body>
</html>
