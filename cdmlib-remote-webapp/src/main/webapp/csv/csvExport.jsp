<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui-1.8.23.custom.min.js"></script>
<!-- <script type="text/javascript" src="../js/jquery.blockUI.js"></script> -->
<script src="http://code.jquery.com/jquery-1.8.2.js"></script>
<script src="http://code.jquery.com/ui/1.9.0/jquery-ui.js"></script>
<script type="text/javascript" src="../js/csv_export.js"></script>
<script type="text/javascript" src="../js/jquery.cookie.js"></script>
<script type="text/javascript" src="http://malsup.github.com/jquery.blockUI.js"></script>
<!-- <script type="text/javascript" src="../js/jquery.fileDownload.js"></script> -->
<link rel="stylesheet" href="http://code.jquery.com/ui/1.9.0/themes/base/jquery-ui.css" />
<link type="text/css" href="../css/jquery-ui/jquery-ui-1.8.23.custom.css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" href="../css/csv_export.css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>CDM DWCA Export</title>
</head>
<body>
	<h1>Export TSV Page</h1><!--exportRedlist  -->
	<form name="exportForm" method="post" action="export" onsubmit="return validateForm()">
	<!--  method="post" action="exportRedlist" onsubmit="return validateForm()" -->
		<div class="ui-widget" id="comboboxWidget" name="comboboxWidget">
			<label for="combobox">Classification</label> 
			<select id="combobox" name="classification" value="" title="type &quot;*&quot; to retrieve all entries"></select>
			<!--  <span class="showall ui-icon ui-icon-triangle-1-s" title="Show all">Show all </span> -->
			<input type="hidden" id="downloadTokenValueId" name="downloadTokenValueId"/>
		</div>
		<div id="csvExportOptions">
			<h4>Download Options:</h4>
		</div>
		<div id="button">
			<input type="submit" value="submit">
			<!--onclick="showCircular()"  -->
		</div>
	</form>
	<div id="dialog-message" title="Download complete" style="display: none;">
		<p>
			<span class="ui-icon ui-icon-info" style="float: left; margin: 0 7px 50px 0;"></span> 
		 	Please select a <b>classification</b>
		</p>
	</div>
</body>
</html>