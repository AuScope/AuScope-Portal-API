<%@ taglib prefix='security' uri='http://www.springframework.org/security/tags' %>

<div class="header-container">
    <div class="banner"
        onclick="window.open('about.html','AboutWin','toolbar=no, menubar=no,location=no,resizable=no,scrollbars=yes,statusbar=no,top=100,left=200,height=650,width=450');return false">
	    <span class="sr-only">Government of Western Australia - Department of Mines and Petroleum</span>
        <span id="app-title" class="sr-only">Australian National <br/> Virtual Geophysics Laboratory</span>
	</div>
	<div class="menu">
		
			<div class="menu-item"><a id="help-button">Help</a></div>
			<div class="menu-item<%if (request.getRequestURL().toString().contains("/gmap.jsp")) {%> current<%} %>"><a href="gmap.html">Portal</a></div>
			<div class="menu-item<%if (request.getRequestURL().toString().contains("/jobbuilder.jsp")) {%> current<%} %>"><a href="jobbuilder.html">Submit</a></div>
			<div class="menu-item<%if (request.getRequestURL().toString().contains("/joblist.jsp")) {%> current<%} %>"><a href="joblist.html">Monitor</a></div>

		

		<div class="login-widget menu-item">
			<security:authorize ifAllGranted="ROLE_ANONYMOUS">
				<div class="login-text"><a href="login.html">Login</a></div>
			</security:authorize>

			<security:authorize ifNotGranted="ROLE_ANONYMOUS">
				<div>Hello <security:authentication property="principal.email" /><div class="dropdownicon"></div> 
				</div>
				
				<div class="sub-menu">
				    <security:authorize ifAllGranted="ROLE_ADMINISTRATOR">
                        <a href="admin.html">Administration</a>
                    </security:authorize>
                    <a href="user.html">Profile</a>
                    <a href="j_spring_security_logout">Logout</a>
				</div>
			</security:authorize>
		</div>
	</div>
	
	<div id="latlng"></div>
	<%if (request.getRequestURL().toString().contains("/gmap.jsp")) {%>
    <div id="permalinkicon"><a href="javascript:void(0)"><img src="portal-core/img/link.png" width="16" height="16"/></a></div>
    <div id="permalink"><a href="javascript:void(0)">Permanent Link</a></div>
    <%} %>
</div>
