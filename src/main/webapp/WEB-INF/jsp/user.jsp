<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<!-- Credits for icons from http://www.fatcow.com/free-icons/ under http://creativecommons.org/licenses/by/3.0/us/-->
<html xmlns:v="urn:schemas-microsoft-com:vml">
   <head>
      <title>ANVGL Portal</title>
      <script type="text/javascript">
         var WEB_CONTEXT = '<%= request.getContextPath() %>';
         var NEW_SESSION = "${isNewSession}";
      </script>
      
      <style type="text/css">
	       .important-button .x-btn-inner-default-toolbar-large {
			    color: white;
			}
			
			.important-button.x-btn-default-toolbar-large {
			    background: rgb(30,87,153); /* Old browsers */
			    background: -moz-linear-gradient(top,  rgba(30,87,153,1) 0%, rgba(41,137,216,1) 50%, rgba(32,124,202,1) 51%, rgba(125,185,232,1) 100%); /* FF3.6+ */
			    background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(30,87,153,1)), color-stop(50%,rgba(41,137,216,1)), color-stop(51%,rgba(32,124,202,1)), color-stop(100%,rgba(125,185,232,1))); /* Chrome,Safari4+ */
			    background: -webkit-linear-gradient(top,  rgba(30,87,153,1) 0%,rgba(41,137,216,1) 50%,rgba(32,124,202,1) 51%,rgba(125,185,232,1) 100%); /* Chrome10+,Safari5.1+ */
			    background: -o-linear-gradient(top,  rgba(30,87,153,1) 0%,rgba(41,137,216,1) 50%,rgba(32,124,202,1) 51%,rgba(125,185,232,1) 100%); /* Opera 11.10+ */
			    background: -ms-linear-gradient(top,  rgba(30,87,153,1) 0%,rgba(41,137,216,1) 50%,rgba(32,124,202,1) 51%,rgba(125,185,232,1) 100%); /* IE10+ */
			    background: linear-gradient(to bottom,  rgba(30,87,153,1) 0%,rgba(41,137,216,1) 50%,rgba(32,124,202,1) 51%,rgba(125,185,232,1) 100%); /* W3C */
			    filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#1e5799', endColorstr='#7db9e8',GradientType=0 ); /* IE6-9 */
			}
			
			.important-button.x-btn.x-btn-menu-active.x-btn-default-toolbar-large, .important-button.x-btn.x-btn-pressed.x-btn-default-toolbar-large{
			    background: rgb(10,77,153); /* Old browsers */
			    background: -moz-linear-gradient(top,  rgba(10,77,153,1) 0%, rgba(4,119,214,1) 49%, rgba(0,110,201,1) 52%, rgba(61,156,229,1) 100%); /* FF3.6+ */
			    background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(10,77,153,1)), color-stop(49%,rgba(4,119,214,1)), color-stop(52%,rgba(0,110,201,1)), color-stop(100%,rgba(61,156,229,1))); /* Chrome,Safari4+ */
			    background: -webkit-linear-gradient(top,  rgba(10,77,153,1) 0%,rgba(4,119,214,1) 49%,rgba(0,110,201,1) 52%,rgba(61,156,229,1) 100%); /* Chrome10+,Safari5.1+ */
			    background: -o-linear-gradient(top,  rgba(10,77,153,1) 0%,rgba(4,119,214,1) 49%,rgba(0,110,201,1) 52%,rgba(61,156,229,1) 100%); /* Opera 11.10+ */
			    background: -ms-linear-gradient(top,  rgba(10,77,153,1) 0%,rgba(4,119,214,1) 49%,rgba(0,110,201,1) 52%,rgba(61,156,229,1) 100%); /* IE10+ */
			    background: linear-gradient(to bottom,  rgba(10,77,153,1) 0%,rgba(4,119,214,1) 49%,rgba(0,110,201,1) 52%,rgba(61,156,229,1) 100%); /* W3C */
			    filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#0a4d99', endColorstr='#3d9ce5',GradientType=0 ); /* IE6-9 */
			        
			}
	    </style>
      
      <%-- JS imports - relative paths back to the webapp directory --%>
      <jsp:include page="../../portal-core/jsimports.htm"/>
      
      <%-- CSS imports - relative paths back to the webapp directory--%>
      <jsp:include page="../../portal-core/cssimports.htm"/>
      <jsp:include page="../../cssimports.htm"/>

      <script src="js/vegl/models/ANVGLUser.js" type="text/javascript"></script>
      <script src="js/vegl/widgets/ANVGLUserPanel.js" type="text/javascript"></script>
      <script src="js/vegl/widgets/TermsConditionsWindow.js" type="text/javascript"></script>
      <script src="js/vegl/User-UI.js" type="text/javascript"></script>
   </head>

   <body>
      <!-- Include Navigation Header -->
      <%@ include file="page_header.jsp" %>
      <%@ include file="page_footer.jsp" %>
   </body>

</html>