<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="include.jsp" %>

<html>

<head>
    <title>AuScope VGL Workflow  - Monitor Jobs</title>
    <link rel="stylesheet" type="text/css" href="css/styles.css">
    <link rel="stylesheet" type="text/css" href="css/menu.css">
    <link rel="stylesheet" type="text/css" href="css/grid-examples.css">
    <style type="text/css">
      #sitenav-01 a {
        background: url( "img/navigation.gif" ) 0px -38px no-repeat;
      }
    </style>

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

    <!-- Portal Core Includes -->
    <link rel="stylesheet" type="text/css" href="portal-core/css/styles.css">
    <link rel="stylesheet" type="text/css" href="portal-core/js/extjs-4.1.0-rc1/resources/css/ext-all.css">
    <link rel="stylesheet" type="text/css" href="portal-core/js/extjs-4.1.0-rc1/examples/ux/css/CheckHeader.css">
    <script type="text/javascript" src="portal-core/js/extjs-4.1.0-rc1/ext-all-debug.js"></script>
    <script type="text/javascript" src="portal-core/js/portal/util/FileDownloader.js"></script>
    <script src="portal-core/js/portal/widgets/grid/plugin/RowContextMenu.js" type="text/javascript"></script>


    <script type="text/javascript" src="js/vegl/models/FileRecord.js"></script>
    <script type="text/javascript" src="js/vegl/models/Job.js"></script>
    <script type="text/javascript" src="js/vegl/models/Series.js"></script>

    <script type="text/javascript" src="js/vegl/widgets/JobDetailsPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobFilesPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobsPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/SeriesPanel.js"></script>

    <script type="text/javascript" src="js/vegl/jobwizard/forms/BaseJobWizardForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/DuplicateJobForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobObjectForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobUploadForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobSubmitForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/JobWizard.js"></script>

    <script type="text/javascript" src="js/vegl/JobList.js"></script>
</head>

<body>
    <%@ include file="page_header.jsp" %>
    <div id="body"></div>
</body>

</html>

