<%@page import="hk.hku.cecid.piazza.commons.Sys"%>
<%@page import="hk.hku.cecid.piazza.corvus.core.Kernel"%>
<%@page import="java.util.Properties"%>
<%@page contentType="text/html;charset=UTF-8"%>
<%
    String path = application.getRealPath("/home/git.properties");
    Properties git = new Properties();
    try {
        git.load(new java.io.FileInputStream(path));
    } catch (Exception e) {
        //ignore any exceptions
    }
%>
<% String ctxPath = request.getContextPath();%>
<html>
<head>
	<title>Jentrata MSH</title>
</head>
<body>
<h2>Welcome to Jentrata MSH</h2>
<hr/>
<p>&gt; Jentrata has started up <%=Kernel.getInstance().hasErrors()? "with errors":"successfully"%>.</p>
<p>&gt; System name: <%=Sys.main.getName()%></p>
<p>&gt; System version: <%=Sys.main.getVersion()%> - (#<%=Sys.main.getBuildID().substring(0,7)%>)</p>
<p><a href="<%=ctxPath%>/admin/home">Jentrata MSH Admin</a></p>
<hr/>
<h3>Git Details:</h3>
<pre>
<%git.list(new java.io.PrintWriter(out));%>
</pre>
<hr/>
<h3>Java System Properties:</h3>
<pre>
<%System.getProperties().list(new java.io.PrintWriter(out));%>
</pre>
</body>
</html>