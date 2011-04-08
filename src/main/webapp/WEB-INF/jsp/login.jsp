<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="include.jsp" %>

<html>

<head>
	<title>VEGL Portal - OpenID Login</title>
	<link rel="stylesheet" type="text/css" href="css/styles.css"> 
    <link rel="stylesheet" type="text/css" href="css/menu.css">
    <link rel="stylesheet" type="text/css" href="js/external/extjs/resources/css/ext-all.css">
	
</head>
<body>
    <div id="header-container">
      <div id="logo">
         <h1>
            <a href="#" onclick="window.open('about.html','AboutWin','toolbar=no, menubar=no,location=no,resizable=no,scrollbars=yes,statusbar=no,top=100,left=200,height=650,width=450');return false"><img alt="VEGL Header" src="img/img-auscope-banner.gif"></a>
         </h1>
      </div>
    </div>
	<div>
		<form action="j_spring_openid_security_check" method="post" class="openid_form">
			<table align="center" style="margin-top: 100px"><tr><td>
				<div>
					<p>Sign in with OpenID</p>
					<input type="text" name="openid_identifier" class="openid_identifier" />
					<input type="submit" name="login" value="Login" />
				</div>
			</td></tr></table>
		</form>
	</div>
</body>
</html>
