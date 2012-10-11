<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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

    <%-- Code Mirror inclusions --%>
    <link href="CodeMirror-2.33/lib/codemirror.css" type="text/css" rel="stylesheet" />
    <script type="text/javascript" src="CodeMirror-2.33/lib/codemirror.js"></script>
    <script type="text/javascript" src="CodeMirror-2.33/mode/python/python.js"></script>
    <script type="text/javascript" src="CodeMirror-2.33/lib/util/formatting.js"></script>
    <script type="text/javascript" src="CodeMirror-2.33/lib/util/simple-hint.js"></script>

    <!-- Portal Core Includes -->
    <link rel="stylesheet" type="text/css" href="portal-core/css/styles.css">
    <link rel="stylesheet" type="text/css" href="portal-core/js/extjs-4.1.0-rc1/resources/css/ext-all.css">
    <link rel="stylesheet" type="text/css" href="portal-core/js/extjs-4.1.0-rc1/examples/ux/css/CheckHeader.css">
    <script type="text/javascript" src="portal-core/js/extjs-4.1.0-rc1/ext-all-debug.js"></script>
    <script type="text/javascript" src="portal-core/js/portal/util/FileDownloader.js"></script>
    <script type="text/javascript" src="portal-core/js/portal/widgets/grid/plugin/RowContextMenu.js"></script>
    <script type="text/javascript" src="portal-core/js/extjs-4.1.0-rc1/examples/ux/RowExpander.js"></script>
    <script type="text/javascript" src="portal-core/js/portal/util/FileDownloader.js"></script>
    <script type="text/javascript" src="portal-core/js/portal/util/ObservableMap.js"></script>
    <script type="text/javascript" src="portal-core/js/extjs-ux-externals/form/plugin/FieldHelpText.js"></script>
    <link rel="stylesheet" href="portal-core/js/extjs-ux-externals/form/plugin/FieldHelpText.css">
    <script type="text/javascript" src="portal-core/js/portal/widgets/window/ErrorWindow.js"></script>

    <script type="text/javascript" src="mzExt/ux/form/field/Ext.ux.form.field.CodeMirror.411.js"></script>

    <script type="text/javascript" src="js/vegl/models/FileRecord.js"></script>
    <script type="text/javascript" src="js/vegl/models/Job.js"></script>
    <script type="text/javascript" src="js/vegl/models/Series.js"></script>
    <script type="text/javascript" src="js/vegl/models/MachineImage.js"></script>
    <script type="text/javascript" src="js/vegl/models/Parameter.js"></script>
    <script type="text/javascript" src="js/vegl/models/Download.js"></script>

    <script type="text/javascript" src="js/vegl/widgets/JobDetailsPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobFilesPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobInputFilesPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobInputFileWindow.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/MachineImageCombo.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobsPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/SeriesPanel.js"></script>

    <script type="text/javascript" src="js/vegl/jobwizard/forms/BaseJobWizardForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/DuplicateJobForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobObjectForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobUploadForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobSubmitForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/ScriptBuilderForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/JobWizard.js"></script>

    <script src="js/ScriptBuilder/templates/BaseTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/UbcGravityTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/UbcMagneticTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/EScriptGravityTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/UnderworldGocadTemplate.js" type="text/javascript"></script>
    <script type="text/javascript" src="js/ScriptBuilder/ScriptBuilder.js"></script>
    <script type="text/javascript" src="js/ScriptBuilder/InsertionPromptWindow.js"></script>
    <script type="text/javascript" src="js/ScriptBuilder/Components.js"></script>
    <script type="text/javascript" src="js/ScriptBuilder/ComponentTreePanel.js"></script>

    <script type="text/javascript" src="js/vegl/JobList.js"></script>
</head>

<body>
    <%@ include file="page_header.jsp" %>
    <div id="body"></div>
    <%@ include file="page_footer.jsp" %>
</body>

</html>