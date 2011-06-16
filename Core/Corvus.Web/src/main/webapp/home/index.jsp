<%@page import="hk.hku.cecid.piazza.commons.Sys"%>
<%@page import="hk.hku.cecid.piazza.corvus.core.Kernel"%>
<%@page import="java.util.Properties"%>
<%@page contentType="text/html;charset=UTF-8"%>
<html>
<head>
	<title>Jentrata MSH</title>
</head>
<body>
<h2>Welcome to Jentrata MSH</h2>
<hr>
<p>&gt; Jentrata has started up <%=Kernel.getInstance().hasErrors()? "with errors":"successfully"%>.</p>
<p>&gt; System name: <%=Sys.main.getName()%></p>
<p>&gt; System version: <%=Sys.main.getVersion()%></p>
<p><a href="/corvus/admin/home">Jentrata MSH Admin</a></p>
<!-- Java System Properties -->
<!--
<%System.getProperties().list(new java.io.PrintWriter(out));%>
-->
</body>
</html>