<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
    <title>ANVGL Portal - Build Job</title>
    
    <link rel="stylesheet" type="text/css" href="css/vl-styles.css">
    
    <style type="text/css">
      #sitenav-03 a {
        background: url( "img/navigation.gif" ) -200px -38px no-repeat;
      }
    </style>

    <%-- Code Mirror inclusions --%>
    <link href="CodeMirror-5.16/lib/codemirror.css" type="text/css" rel="stylesheet" />    
    <script type="text/javascript" src="CodeMirror-5.16/lib/codemirror.js"></script>
    <script type="text/javascript" src="CodeMirror-5.16/mode/python/python.js"></script>
    <script type="text/javascript" src="CodeMirror-5.16/mode/javascript/javascript.js"></script>


    <!-- Portal Core Includes -->    
    <jsp:include page="../../portal-core/jsimports.jsp"/>
    <jsp:include page="../../portal-core/cssimports.jsp"/>
    <jsp:include page="../../cssimports.htm"/>

    <!-- component includes -->
    <script type="text/javascript" src="js/vegl/widgets/CodeEditorField.js"></script>  

    <script type="text/javascript" src="js/vegl/models/FileRecord.js"></script>
    <script type="text/javascript" src="js/vegl/models/Download.js"></script>
    <script type="text/javascript" src="js/vegl/models/Parameter.js"></script>
    <script type="text/javascript" src="js/vegl/models/Job.js"></script>
    <script type="text/javascript" src="js/vegl/models/Series.js"></script>
    <script type="text/javascript" src="js/vegl/models/MachineImage.js"></script>
    <script type="text/javascript" src="js/vegl/models/ComputeType.js"></script>
    <script type="text/javascript" src="js/vegl/models/SimpleFeatureProperty.js"></script>

    <script src="js/ScriptBuilder/templates/BaseTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/UbcGravityTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/UbcMagneticTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/EScriptGravityTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/EScriptMagneticTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/EScriptJointTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/UnderworldGocadTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/AEMInversionTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/EScriptGravityPointTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/Components.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/ComponentTreePanel.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/InsertionPromptWindow.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/ScriptBuilder.js" type="text/javascript"></script>

    <script type="text/javascript" src="js/vegl/jobwizard/forms/BaseJobWizardForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobObjectForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobUploadForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/ScriptBuilderForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/JobSubmitForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/forms/DuplicateJobForm.js"></script>
    <script type="text/javascript" src="js/vegl/jobwizard/JobWizard.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobFilesPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobsTree.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobsPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/MachineImageCombo.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobInputFileCopyWindow.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobInputFileRemoteWindow.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobInputFileUploadWindow.js"></script>
    <script src="js/ScriptBuilder/templates/DynamicTemplate.js" type="text/javascript"></script>

    <script type="text/javascript" src="js/vegl/JobBuilder.js"></script>
    <script type="text/javascript" src="js/vegl/HelpHandler.js"></script>
</head>

<body>
    <%@ include file="page_header.jsp" %>
    <div id="body"></div>
    <%@ include file="page_footer.jsp" %>
</body>

</html>

