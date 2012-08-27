<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<!-- Credits for icons from http://www.fatcow.com/free-icons/ under http://creativecommons.org/licenses/by/3.0/us/-->
<html xmlns:v="urn:schemas-microsoft-com:vml">
   <head>
      <title>VGL Portal</title>

      <meta name="description" content="Access geoscientific information from around Australia, via AuScopes national e-Research infrastructure.">
      <meta name="keywords" content="AuScope, Discovery, Resources, GeoSciML, Mineral Occurrence, Geologic Unit, Australia">
      <meta name="author" content="AuScope">

      <%-- Google Maps imports --%>
      <script src="http://maps.google.com/maps?file=api&amp;v=2.X&amp;key=${googleKey}" type="text/javascript"></script>
      <script src="http://gmaps-utility-library.googlecode.com/svn/trunk/markermanager/release/src/markermanager.js" type="text/javascript"></script>

      <%-- Open Layers Imports --%>
      <link rel="stylesheet" href="portal-core/js/OpenLayers-2.11/theme/default/style.css" type="text/css">
      <script src="portal-core/js/OpenLayers-2.11/OpenLayers.js" type="text/javascript"></script>

      <script type="text/javascript">
         var VOCAB_SERVICE_URL = "${vocabServiceUrl}";
         var NVCL_WEB_SERVICE_IP = "${nvclWebServiceIP}";
         var WEB_CONTEXT = '<%= request.getContextPath() %>';
      </script>


      <%-- CSS imports - relative paths back to the webapp directory--%>
      <jsp:include page="../../portal-core/cssimports.htm"/>
      <%-- JS imports - relative paths back to the webapp directory --%>
      <jsp:include page="../../portal-core/jsimports.htm"/>
      <jsp:include page="../../jsimports.htm"/>

      <%-- Google Analytics --%>
      <c:if test="${not empty analyticKey}">
          <script type="text/javascript">

          var _gaq = _gaq || [];
          _gaq.push(['_setAccount', '${analyticKey}']);
          _gaq.push(['_trackPageview']);

            (function() {
                var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
                ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
                var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
              })();

          </script>
      </c:if>

      <!-- To enable text selection within a Ext JS GridPanel -->
      <style type="text/css">
        .x-selectable, .x-selectable * {
            -moz-user-select: text!important;
            -khtml-user-select: text!important;
        }
        </style>

      <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />
   </head>

   <body onunload="GUnload()">
      <!-- Include Navigation Header -->
      <%@ include file="page_header.jsp" %>
   </body>

</html>