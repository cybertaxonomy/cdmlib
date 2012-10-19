<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<html>
<head><title>JSP with Javabeans</title></head>
<body bgcolor="#ffccff">
<h1>JSP using DwcA example</h1>

<jsp:useBean id="sample" class=".bean" scope="page">
<jsp:setProperty name="sample" property="*"/>
</jsp:useBean>
<form name="form1" method="POST">
  ID&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
   <input type="text" name ="msgid"> <br>
   Message<input type="text" name ="message"> <br>
   <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
   <input type = "submit" value="Submit">
</form>
</body>
</html>