<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
    <title>ANVGL Portal - Login</title>
    <link rel="stylesheet" type="text/css" href="css/vl-styles.css">
    
    <style type="text/css">
      #sitenav-03 a {
        background: url( "img/navigation.gif" ) -200px -38px no-repeat;
      }
    </style>

    <!-- Portal Core Includes -->
    <jsp:include page="../../portal-core/cssimports.jsp"/>
    <jsp:include page="../../cssimports.htm"/>
</head>
<body>
    <%@ include file="page_header.jsp" %>
    <div id="body">
        <div class="login-message">
	        <h2>Authorisation Required</h2>You are not authorised to view the page you have selected.<br>
	        Did you forgot to log in? Please log in using one of the options below.<br>
	        <div name="google-login" class="google-medium-icon" title="Log in with Google" onclick="location.href='oauth/google_login.html';"></div>
	        <div name="aaf-login" class="aaf-medium-icon" title="Log in with AAF"></div>
        </div>
    </div>
    <%@ include file="page_footer.jsp" %>
</body>

</html>