<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui-1.8.23.custom.min.js"></script>
<script src="http://code.jquery.com/jquery-1.8.2.js"></script>
<script src="http://code.jquery.com/ui/1.9.0/jquery-ui.js"></script>
<script type="text/javascript" src="../js/rl_export.js"></script>
<link rel="stylesheet" href="http://code.jquery.com/ui/1.9.0/themes/base/jquery-ui.css" />
<link type="text/css" href="../css/jquery-ui-1.8.23.custom.css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" href="../css/rl_export.css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>CDM DWCA Export</title>
</head>
<body>
	<h1>Export TSV Page</h1>
	<form method="post" action="getDB">
		<div class="ui-widget">
			<label for="combobox">Classification</label> 
			<select id="combobox" name="combobox" value="" title="type &quot;*&quot; to retrieve all entries"></select>
			<!--  <span class="showall ui-icon ui-icon-triangle-1-s" title="Show all">Show all </span> -->
		</div>
		<div id="dwcaDlOptions">
			<h4>Download Options:</h4>
		</div>
		<div id="button">
			<input type="submit" value="Submit" onclick="">
		</div>
	</form>
</body>
</html>