<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
    <title>ANVGL Portal - Monitor Jobs</title>
    
    <link rel="stylesheet" type="text/css" href="css/vl-styles.css">
    
    <style type="text/css">
        #sitenav-01 a {
            background: url( "img/navigation.gif" ) 0px -38px no-repeat;
        }
      
        .vl-job-details .x-form-value-field {
	       margin: 0 0 0 0;
	       padding: 0 0 0 0;
	       font: bold 28px tahoma,arial,verdana,sans-serif;
	   }
	
	   .vl-job-details .x-form-value-field-label {
	       margin: 0 0 0 0;
	       padding: 0 0 0 0;
	       font: normal 16px tahoma,arial,verdana,sans-serif;
	       text-align: center;
	       color: black;
	   }
	   
	   .vl-job-details-small .x-form-value-field {
           margin: 0 0 0 0;
           padding: 0 0 0 0;
           font: bold 18px tahoma,arial,verdana,sans-serif;
       }
    
       .vl-job-details-small .x-form-value-field-label {
           margin: 0 0 0 0;
           padding: 0 0 0 0;
           font: normal 12px tahoma,arial,verdana,sans-serif;
           text-align: center;
           color: black;
       }
       
       .x-tree-icon.x-tree-icon-leaf {
           background-image: none;
           width: 0px;
       }
    </style>

    <%-- Code Mirror inclusions --%>
	<link href="CodeMirror-5.16/lib/codemirror.css" type="text/css" rel="stylesheet" />    
    <script type="text/javascript" src="CodeMirror-5.16/lib/codemirror.js"></script>
    <script type="text/javascript" src="CodeMirror-5.16/mode/python/python.js"></script>
    <script type="text/javascript" src="CodeMirror-5.16/mode/javascript/javascript.js"></script>
	
    <!-- OpenLayers Includes -->
    <link rel="stylesheet" href="portal-core/js/OpenLayers-2.13.1/theme/default/style.css" type="text/css">
    <script src="portal-core/js/OpenLayers-2.13.1/OpenLayers.js" type="text/javascript"></script>

    <!-- Portal Core Includes -->   
    <jsp:include page="../../portal-core/jsimports.jsp"/>
    <jsp:include page="../../portal-core/cssimports.jsp"/>
    <jsp:include page="../../cssimports.htm"/>


    <script type="text/javascript" src="js/vegl/widgets/CodeEditorField.js"></script>
    <script type="text/javascript" src="js/vegl/models/FileRecord.js"></script>
    <script type="text/javascript" src="js/vegl/models/Job.js"></script>
    <script type="text/javascript" src="js/vegl/models/Series.js"></script>
    <script type="text/javascript" src="js/vegl/models/MachineImage.js"></script>
    <script type="text/javascript" src="js/vegl/models/Parameter.js"></script>
    <script type="text/javascript" src="js/vegl/models/Download.js"></script>
    <script type="text/javascript" src="js/vegl/models/ComputeType.js"></script>
    <script type="text/javascript" src="js/vegl/models/SimpleFeatureProperty.js"></script>

    <script type="text/javascript" src="js/vegl/preview/FilePreviewMixin.js"></script>
    <script type="text/javascript" src="js/vegl/preview/DataServicePreview.js"></script>
    <script type="text/javascript" src="js/vegl/preview/LogPreview.js"></script>
    <script type="text/javascript" src="js/vegl/preview/PlainTextPreview.js"></script>
    <script type="text/javascript" src="js/vegl/preview/ImagePreview.js"></script>
    <script type="text/javascript" src="js/vegl/preview/FilePreviewPanel.js"></script>
    <script type="text/javascript" src="js/vegl/preview/TTLPreview.js"></script>

    <script type="text/javascript" src="js/vegl/widgets/JobDetailsPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobFilesPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobInputFileCopyWindow.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobInputFileRemoteWindow.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobInputFileUploadWindow.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/MachineImageCombo.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobsTree.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobsPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/SeriesPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/FolderPanel.js"></script>
    <script type="text/javascript" src="js/vegl/widgets/JobRegisterPanel.js"></script>

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
    <script src="js/ScriptBuilder/templates/EScriptMagneticTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/EScriptJointTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/UnderworldGocadTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/AEMInversionTemplate.js" type="text/javascript"></script>
    <script src="js/ScriptBuilder/templates/DynamicTemplate.js" type="text/javascript"></script>
    <script type="text/javascript" src="js/ScriptBuilder/ScriptBuilder.js"></script>
    <script type="text/javascript" src="js/ScriptBuilder/InsertionPromptWindow.js"></script>
    <script type="text/javascript" src="js/ScriptBuilder/Components.js"></script>
    <script type="text/javascript" src="js/ScriptBuilder/ComponentTreePanel.js"></script>

    <script type="text/javascript" src="js/vegl/JobList.js"></script>
    <script type="text/javascript" src="js/vegl/HelpHandler.js"></script>
</head>

<body>
    <%@ include file="page_header.jsp" %>
    <div id="body"></div>
    <%@ include file="page_footer.jsp" %>
</body>

</html>