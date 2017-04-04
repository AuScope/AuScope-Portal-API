<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
    <title>VGL Portal - Setup Required</title>
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
	        <h2>Terms and Conditions not yet accepted!</h2>You haven't accepted the latest version of the terms and conditions.<br>
	        You will need to visit the setup page to accept the latest terms.<br>
	        <div name="setup-btn" class="setup-medium-icon" title="Visit setup page" onclick="location.href='user.html';"></div>
        </div>
    </div>
    <%@ include file="page_footer.jsp" %>
</body>

</html>