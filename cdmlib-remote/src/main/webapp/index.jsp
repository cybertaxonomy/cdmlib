<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<title>CDM Community Server</title>
</head>
<body>
<div>
<form name="input" action="find/taxon" method="get">
<TABLE>
<TR>
  <th>Scientific name:</th>
  <td> <input type="text" name="q"> </td>
</TR>
<TR>
  <th>Match anywhere:</th>
  <td> <input type="checkbox" name="matchAnywhere" value="true"> </td>
</TR>
<TR>
  <th>Accepted only:</th>
  <td> <input type="checkbox" name="acceptedOnly" value="true"> </td>
</TR>
<TR>
  <th>Sec UUID:</th>
  <td> <input type="text" name="sec"> </td>
</TR>
<TR>
  <th>Higher taxon:</th>
  <td> <input type="text" name="higherTaxa"> </td>
</TR>
<TR>
  <th>Page/Pagesize:</th>
  <td> <input type="text" size="3" name="page"> / <input type="text" size="3" name="pagesize"> </td>
</TR>
<TR>
  <th></th>
  <td><input type="submit" value="Submit"></td>
</TR>


</TABLE>
</form>
</div>
</body>
</html>
