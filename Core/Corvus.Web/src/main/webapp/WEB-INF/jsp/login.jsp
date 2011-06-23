<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<% String ctxPath = request.getContextPath();%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="<%=ctxPath%>/stylesheet/login.css" rel="stylesheet" type="text/css" />
<link rel="icon" href="<%=ctxPath%>/favicon.png" type="image/png">
<link rel="shortcut icon" href="<%=ctxPath%>/favicon.png" type="image/png">
<title>Jentrata MSH::Administration System - Login</title>
</head>
<body>
	<div id="login">
		<h1><a href="#" title="Jentrata MSH Admin"><span class="hide"></span></a></h1> 
		<% if (request.getParameter("failed") != null) { %>
		<div id="login_error">
			<strong>ERROR</strong>: Invalid username and/or password.<br>
		</div>
		<% } %>
		<form name="loginform" id="loginform" action="<%= response.encodeURL("j_security_check") %>" method="post">
			<h2>MSH Admin System</h2>
			<br/>
			<p> 
				<label>Username<br /> 
				<input type="text" name="j_username" id="user_login" class="input" value="" size="20" tabindex="10" /></label> 
			</p> 
			<p> 
				<label>Password<br /> 
				<input type="password" name="j_password" id="user_pass" class="input" value="" size="20" tabindex="20" /></label> 
			</p> 
			<p class="submit"> 
				<input type="submit" name="wp-submit" id="wp-submit" class="button-primary" value="Log In" tabindex="100" /> 
			</p> 
		</form> 
		<script type="text/javascript"> 
		function lb_attempt_focus(){
		setTimeout( function(){ try{
		d = document.getElementById('user_login');
		d.focus();
		d.select();
		} catch(e){}
		}, 200);
		}
		lb_attempt_focus();
		</script>
		<div id="footer" class="footer">
			<br/>
			<p>(<%=hk.hku.cecid.piazza.commons.Sys.main.getVersion()%>)</p> 
		</div>
	</div> 
</body>
</html>