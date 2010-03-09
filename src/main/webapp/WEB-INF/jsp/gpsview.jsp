<html xmlns:v="urn:schemas-microsoft-com:vml">
   <head>
      <title>AuScope Discovery Portal</title>
      
      <!-- Page Style -->
      <link rel="stylesheet" type="text/css" href="css/menu.css"> 
      <link rel="stylesheet" type="text/css" href="css/styles.css">
      <link rel="stylesheet" type="text/css" href="css/grid-examples.css">

      <STYLE type="text/css">
         #nav-example-02 a {
            background: url("/img/navigation.gif") -100px -38px no-repeat;
         }
         /* for IE */
         v\:* {
            behavior: url(#default#VML);
         }        
      </STYLE>
      
      <link rel="stylesheet" type="text/css" href="js/external/ext-2.2/resources/css/ext-all.css">
   </head>
   <body>
   		<div id="menu-container" style="height: 100px">
			<%@ include file="page_header.jsp" %>
		</div>
			
		<div id="frame-container" style="width: 100%; top: 105px; bottom: 0; position: absolute;">
			<iframe  src="http://apacsrv1.arrc.csiro.au/wms_v0.9dev/" style="width:100%;height:100%" frameborder="0"></iframe>
		</div>
		
   </body>
   
   
   
</html>