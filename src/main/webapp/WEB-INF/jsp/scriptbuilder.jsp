<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="include.jsp" %>

<html>

<head>
    <title>VEGL Portal - Scriptbuilder</title>
	<link rel="stylesheet" type="text/css" href="css/styles.css"> 
    <link rel="stylesheet" type="text/css" href="css/menu.css">
    <link rel="stylesheet" type="text/css" href="css/grid-examples.css">
    <link rel="stylesheet" type="text/css" href="js/external/extjs/resources/css/ext-all.css">
    
    <link rel="stylesheet" type="text/css" href="css/scriptbuilder.css">
    
	<style type="text/css">
      #sitenav-03 a {
        background: url( "img/navigation.gif" ) -200px -38px no-repeat;
      }
    </style>
    
    <script type="text/javascript" src="js/external/extjs/adapter/ext/ext-base.js"></script>
    <script type="text/javascript" src="js/external/extjs/ext-all.js"></script>
    <script type="text/javascript" src="js/geoscimlwfs/main.js"></script>
    <script type="text/javascript" src="js/geoscimlwfs/global_variables.js"></script>
    <script type="text/javascript" src="js/ScriptBuilder/ComponentLoader.js"></script>
    <script type="text/javascript" src="js/ScriptBuilder/XmlTreeLoader.js"></script>

    <!-- component includes -->
    <%
    String[] comps = { "BaseComponent", "SimContainer", "AWSUpload", "AWSDownload", "MPIRun",
    		"ChangeDir"};
    for (String c : comps) {
    %>
    <script type="text/javascript" src="js/ScriptBuilder/components/<%= c %>.js"></script>
    <%
    }
    // ScriptBuilder.js must come last!
    %>
    <script type="text/javascript" src="js/ScriptBuilder/ScriptBuilder.js"></script>
</head>

<body>
    <%@ include file="page_header.jsp" %>
    <div id="body"></div>
</body>

</html>

