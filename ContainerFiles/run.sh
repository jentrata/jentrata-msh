#!/bin/bash -ex

export TOMCAT_HOME=$CATALINA_HOME

cat > ${TOMCAT_HOME}/conf/tomcat-users.xml <<-ENDL
<?xml version='1.0' encoding='utf-8'?>
<tomcat-users>
    <role rolename="admin" />
    <role rolename="admin-gui"/>
    <role rolename="manager-gui"/>
    <role rolename="${TOMCAT_USER_NAME}" />
    <user username="${TOMCAT_USER_NAME}" password="${TOMCAT_USER_PASS}" roles="tomcat,admin,${TOMCAT_USER_NAME},admin-gui,manager-gui" />
</tomcat-users>
ENDL

sed -i "/name=\"url\"/s/localhost/${DB_HOST_NAME}/" "${JENTRATA_HOME}/plugins/hk.hku.cecid.edi.as2/conf/hk/hku/cecid/edi/as2/conf/as2.module.core.xml"
sed -i "/name=\"username\"/s/corvus/${DB_USER_NAME}/" "${JENTRATA_HOME}/plugins/hk.hku.cecid.edi.as2/conf/hk/hku/cecid/edi/as2/conf/as2.module.core.xml"
sed -i "/name=\"password\"/s/corvus/${DB_USER_PASS}/" "${JENTRATA_HOME}/plugins/hk.hku.cecid.edi.as2/conf/hk/hku/cecid/edi/as2/conf/as2.module.core.xml"

export JENTRATA_EBMS_DB_URL="jdbc:postgresql://$DB_HOST_NAME:5432/ebms"
export JENTRATA_EBMS_DB_USER="$DB_USER_NAME"
export JENTRATA_EBMS_DB_PASS="$DB_USER_PASS"
export JENTRATA_AS2_DB_URL="jdbc:postgresql://$DB_HOST_NAME:5432/as2"
export JENTRATA_AS2_DB_USER="$DB_USER_NAME"
export JENTRATA_AS2_DB_PASS="$DB_USER_PASS"

export JENTRATA_ACTIVEMQ_MODULE="$ACTIVEMQ_MODE"
export ACTIVEMQ_HOST_NAME="localhost"
if test "$ACTIVEMQ_MODE" = "external"; then
    export ACTIVEMQ_HOST_NAME=`echo "$ACTIVEMQ_URL" | sed -n 's:^tcp\://\([^\:]*\)\:61616:\1:p'`
    export JENTRATA_ACTIVEMQ_USERNAME="$ACTIVEMQ_USER"
    export JENTRATA_ACTIVEMQ_PASSWORD="$ACTIVEMQ_PASS"
    export JENTRATA_ACTIVEMQ_CONNECTIONFACTORYURL="$ACTIVEMQ_URL"
fi

export JAVA_OPTS="$JAVA_OPTS -Dcorvus.home=$JENTRATA_HOME"

echo waiting for postgres
/opt/wait-for-it.sh "$DB_HOST_NAME:5432"
echo waiting for activemq
/opt/wait-for-it.sh "$ACTIVEMQ_HOST_NAME:61616"

exec catalina.sh run
