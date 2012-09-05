<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
    <title>VGL Portal - Access Error</title>

    <link rel="stylesheet" type="text/css" href="css/styles.css">
    <link rel="stylesheet" type="text/css" href="css/menu.css">
    <link rel="stylesheet" type="text/css" href="css/grid-examples.css">
    <link rel="stylesheet" type="text/css" href="portal-core/css/styles.css">

    <style type="text/css">
      #sitenav-03 a {
        background: url( "img/navigation.gif" ) -200px -38px no-repeat;
      }
    </style>

    <!-- Portal Core Includes -->
    <link rel="stylesheet" type="text/css" href="portal-core/js/extjs-4.1.0-rc1/resources/css/ext-all.css">

</head>
<body>
    <%@ include file="page_header.jsp" %>

    <div style="margin-top: 150px;">
        <p>You do not have adequate permissions to access the specified resource.</p>
    </div>

    <%@ include file="page_footer.jsp" %>
</body>
</html>
