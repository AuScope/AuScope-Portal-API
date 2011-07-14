<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="include.jsp" %>

<html>

<head>
    <title>VEGL Portal - Build Job</title>
    <link rel="stylesheet" type="text/css" href="css/styles.css">
    <link rel="stylesheet" type="text/css" href="css/menu.css">
    <link rel="stylesheet" type="text/css" href="css/grid-examples.css">

    <style type="text/css">
      #sitenav-03 a {
        background: url( "img/navigation.gif" ) -200px -38px no-repeat;
      }
    </style>
    <link rel="stylesheet" type="text/css" href="js/external/extjs/resources/css/ext-all.css">
    <script type="text/javascript" src="js/external/extjs/adapter/ext/ext-base.js"></script>
    <script type="text/javascript" src="js/external/extjs/ext-all.js"></script>

    <script type="text/javascript" src="js/ScriptBuilder/ComponentLoader.js"></script>
    <script type="text/javascript" src="js/ScriptBuilder/XmlTreeLoader.js"></script>

    <!-- component includes -->
    <%
    String[] comps = { "BaseComponent", "BasePythonComponent", "SimContainer", "AWSUpload", "AWSDownload", "MPIRun",
            "ChangeDir", "VEGLJobObject", "VEGLUtils", "AWSUtils", "VEGLStep1", "VEGLStep2", "VEGLStep3", "VEGLStep4", "VEGLStep5",
            "VEGLStep6", "VEGLStep7", "VEGLStep8", "VEGLStep9", "DefinePythonFunc", "DefineMainFunc" };
    for (String c : comps) {
    %>
    <script type="text/javascript" src="js/ScriptBuilder/components/<%= c %>.js"></script>
    <%
    }
    %>

    <script type="text/javascript" src="js/JobWizard/forms/BaseJobWizardForm.js"></script>
    <script type="text/javascript" src="js/JobWizard/forms/JobObjectForm.js"></script>
    <script type="text/javascript" src="js/JobWizard/forms/JobSeriesForm.js"></script>
    <script type="text/javascript" src="js/JobWizard/forms/JobUploadForm.js"></script>
    <script type="text/javascript" src="js/JobWizard/forms/ScriptBuilderForm.js"></script>
    <script type="text/javascript" src="js/JobWizard/forms/JobSubmitForm.js"></script>
    <script type="text/javascript" src="js/JobWizard/JobWizard.js"></script>

    <script type="text/javascript" src="js/JobBuilder.js"></script>
</head>

<body>
    <%@ include file="page_header.jsp" %>
    <div id="body"></div>
</body>

</html>

