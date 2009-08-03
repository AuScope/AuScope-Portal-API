<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
   <title>Place holder</title>
   <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

   <!-- Page Style -->
   <link rel="stylesheet" type="text/css" href="css/menu.css"> 
   <link rel="stylesheet" type="text/css" href="css/styles.css">
   <link rel="stylesheet" type="text/css" href="css/grid-examples.css">
   
   <STYLE type="text/css">
      H2 { text-align: center}
      #nav-example-02 a {
         background: url( "/img/navigation.gif" ) -100px -38px no-repeat;
      }
   </STYLE>
</head>
<body>
   <!-- Include Navigation Header -->
   <%@ taglib prefix='security' uri='http://www.springframework.org/security/tags' %>
   <div id="header-container">
      <div id="logo">
         <h1>
            <a href="#" onclick="window.open('about.html','AboutWin','toolbar=no, menubar=no,location=no,resizable=no,scrollbars=yes,statusbar=no,top=100,left=200,height=650,width=450');return false"><img alt="" src="/img/img-auscope-banner.gif" /></a>
            <!-- <a href="login.html"><img alt="" src="/img/img-auscope-banner.gif" /></a> -->
         </h1>
      </div>                            
      <security:authorize ifAllGranted="ROLE_ADMINISTRATOR">
         <a href="admin.html"><span>Administration</span></a>
      </security:authorize>
      <div id="menu">
         <ul >
            <li ><a href="gmap.html">Map Client<span></span></a></li>
            <li ><a href="place_holder_DST.html">Data Service<span></span></a></li>
            <li ><a href="place_holder_JS.html">Submit Jobs<span></span></a></li>
            <li class="current"><a href="place_holder_MJ.html">Monitor Jobs<span></span></a></li>
            <li ><a href="http://apacsrv1.arrc.csiro.au/wms_v0.9dev/">GPS View<span></span></a></li>
         </ul>
      </div>
   </div>
   <div>
	   <br> 
	   <br>
	   <H2>The page is not yet implemented.</H2>
   </div>
</body>
</html>