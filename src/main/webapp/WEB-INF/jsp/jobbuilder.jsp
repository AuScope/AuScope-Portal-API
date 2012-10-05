<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
    <title>VGL Portal - Build Job</title>
    <link rel="stylesheet" type="text/css" href="css/styles.css">
    <link rel="stylesheet" type="text/css" href="css/menu.css">
    <link rel="stylesheet" type="text/css" href="css/grid-examples.css">
    <link rel="stylesheet" type="text/css" href="portal-core/css/styles.css">

    <style type="text/css">
      #sitenav-03 a {
        background: url( "img/navigation.gif" ) -200px -38px no-repeat;
      }
    </style>

    <%-- Code Mirror inclusions --%>
    <link href="CodeMirror-2.33/lib/codemirror.css" type="text/css" rel="stylesheet" />
    <script type="text/javascript" src="CodeMirror-2.33/lib/codemirror.js"></script>
    <script type="text/javascript" src="CodeMirror-2.33/mode/python/python.js"></script>
    <script type="text/javascript" src="CodeMirror-2.33/lib/util/formatting.js"></script>
    <script type="text/javascript" src="CodeMirror-2.33/lib/util/simple-hint.js"></script>

    <!-- Portal Core Includes -->
    <link rel="stylesheet" type="text/css" href="portal-core/js/extjs-4.1.0-rc1/resources/css/ext-all.css">
    <link rel="stylesheet" type="text/css" href="portal-core/js/extjs-4.1.0-rc1/examples/ux/css/CheckHeader.css">
    <script type="text/javascript" src="portal-core/js/extjs-4.1.0-rc1/ext-all-debug.js"></script>
    <script type="text/javascript" src="portal-core/js/extjs-4.1.0-rc1/examples/ux/RowExpander.js"></script>
    <script type="text/javascript" src="portal-core/js/portal/util/FileDownloader.js"></script>
    <script type="text/javascript" src="portal-core/js/portal/util/ObservableMap.js"></script>
    <script type="text/javascript" src="portal-core/js/portal/widgets/window/ErrorWindow.js"></script>


    <!-- component includes -->
    <script type="text/javascript" src="mzExt/ux/form/field/Ext.ux.form.field.CodeMirror.411.js"></script>

    <script type="text/javascript" src="js/vegl/models/FileRecord.js"></script>
    <script type="text/javascript" src="js/vegl/models/Download.js"></script>
    <script type="text/javascript" src="js/vegl/models/Parameter.js"></script>
    <script type="text/javascript" src="js/vegl/models/Job.js"></script>
    <script type="text/javascript" src="js/vegl/models/Series.js"></script>
    <script type="text/javascript" src="js/vegl/models/MachineImage.js"></script>

    <script src="portal-core/js/portal/util/UnimplementedFunction.js" type="text/javascript"></script>
    <script src="portal-core/js/portal/widgets/grid/plugin/RowContextMenu.js" type="text/javascript"></script>

    <script src="js/ScriptBuilder/templates/BaseTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/UbcGravityTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/UbcMagneticTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/EScriptGravityTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/Components.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/ComponentTreePanel.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/InsertionPromptWindow.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/ScriptBuilder.js" type="text/javascript"></script>

    <script type="text/javascript" src="js/vegl/widgets/JobInputFilesPanel.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/BaseJobWizardForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobObjectForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobSeriesForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobUploadForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/ScriptBuilderForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobSubmitForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/DuplicateJobForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/JobWizard.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobsPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/MachineImageCombo.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobInputFileWindow.js"></script>

    <script type="text/javascript" src="js/vegl/JobBuilder.js"></script>
</head>

<body>
    <%@ include file="page_header.jsp" %>
    <div id="body"></div>
    <%@ include file="page_footer.jsp" %>
</body>

</html>

