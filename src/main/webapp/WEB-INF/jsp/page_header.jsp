<%@ taglib prefix='security' uri='http://www.springframework.org/security/tags' %>

<div class="header-container">
    <div class="banner"
        onclick="window.open('about.html','AboutWin','toolbar=no, menubar=no,location=no,resizable=no,scrollbars=yes,statusbar=no,top=100,left=200,height=650,width=450');return false">
	    <span class="sr-only">Government of Western Australia - Department of Mines and Petroleum</span>
        <span id="app-title" class="sr-only">Australian National <br/> Virtual Geophysics Laboratory</span>
	</div>
	<div class="menu">
		
			<div class="menu-item"><a id="help-button">Help<span></span></a></div>
			<div class="menu-item<%if (request.getRequestURL().toString().contains("/gmap.jsp")) {%> current<%} %>"><a href="gmap.html">Portal<span></span></a></div>
			<div class="menu-item<%if (request.getRequestURL().toString().contains("/jobbuilder.jsp")) {%> current<%} %>"><a href="jobbuilder.html">Submit<span></span></a></div>
			<div class="menu-item<%if (request.getRequestURL().toString().contains("/joblist.jsp")) {%> current<%} %>"><a href="joblist.html">Monitor<span></span></a></div>

		

		<div class="login-widget">
			<security:authorize ifAllGranted="ROLE_ANONYMOUS">
				<div class="button">Login</div>
			</security:authorize>

			<security:authorize ifNotGranted="ROLE_ANONYMOUS">
				<div>Hello <security:authentication property="principal.email" />.<div class="dropdownicon"></div> 
				</div>
				
				<div class="sub-menu">
				    <security:authorize ifAllGranted="ROLE_ADMINISTRATOR">
                        <div class="sub-menu-item"><a href="admin.html">Administration<span></span></a></div>
                    </security:authorize>
                    <div class="sub-menu-item"><a href="user.html">Profile<span></span></a></div>
                    <div class="sub-menu-item"><a href="j_spring_security_logout">Logout<span></span></a></div>
				</div>
			</security:authorize>
		</div>
	</div>
</div>
