<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<title>TAXON NAMES</title>
</head>
<body>
<h1>  <c:out value="${name}"/>  </h1>

<hr/>
<p align="center">	The time on the server is <c:out value="${time}"/>  </p>
<p align="center">  path: <c:out value="${path}"/>  </p>
<p align="center">  pmap: <c:out value="${pmap}"/>  </p>
<p align="center">  ctype: <c:out value="${ctype}"/>  </p>

<p><img src="<c:url value="/images/poweredBySpring.gif"/>" alt="Powered By Spring"/></p>
</body>
</html>
