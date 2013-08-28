@echo off
rem ---------------------------------------------------------------------------
rem Set JENTRATA_HOME and/or TOMCAT_HOME if not already set, ensure any provided settings
rem are valid and consistent with the selected start-up options and set up the
rem endorsed directory.
rem
rem ---------------------------------------------------------------------------

rem Make sure prerequisite environment variables are set

rem uncomment this and point it to a valid tomcat distribution
rem set "TOMCAT_HOME=C:\tomcat"

rem override Tomcat JVM settings
rem JAVA_OPTS="-Xms1024m -Xmx1024m"

rem enable Remote JMX
rem set JAVA_OPTS="%JAVA_OPTS% -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=8001 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
