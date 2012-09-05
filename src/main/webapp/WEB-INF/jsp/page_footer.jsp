<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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
