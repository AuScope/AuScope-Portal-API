<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix='security' uri='http://www.springframework.org/security/tags' %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Geodesy Debug Page</title>
		
		<!-- Page Style -->
      	<link rel="stylesheet" type="text/css" href="../css/menu.css"> 
      	<link rel="stylesheet" type="text/css" href="../css/styles.css">
      	<link rel="stylesheet" type="text/css" href="../css/grid-examples.css">
      	
      	<link rel="stylesheet" type="text/css" href="../js/external/ext-2.2/resources/css/ext-all.css">
    	<script type="text/javascript" src="../js/external/ext-2.2/adapter/ext/ext-base.js"></script>
    	<script type="text/javascript" src="../js/external/ext-2.2/ext-all-debug.js"></script>
      	<script type="text/javascript" src="../js/Debug.js" ></script>
	</head>
	<body>
		
		<STYLE type="text/css">
	        H2 { text-align: center}
	        #nav-example-02 a {
	            background: url("../img/navigation.gif") -100px -38px no-repeat;
	        }
      	</STYLE>
		
		<div id="menu">
        	<ul >
        		<li ><a href="../gmap.html">Return<span></span></a></li>
        		
        		<security:authorize ifAllGranted="ROLE_ANONYMOUS">
	            	<li ><a href="dologin.html">Login<span></span></a></li>
	            </security:authorize>
	            
	            <security:authorize ifNotGranted="ROLE_ANONYMOUS">
	            	<li ><a href="../j_spring_security_logout">Logout<span></span></a></li>
	            </security:authorize>
         	</ul>
        </div>
		
		<div id="body"></div>
	</body>
</html>