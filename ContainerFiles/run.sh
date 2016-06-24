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

export jentrata.ebms.db.url="jdbc:postgresql://$DB_HOST_NAME:5432/ebms"
export jentrata.ebms.db.user="$DB_USER_NAME"
export jentrata.ebms.db.pass="$DB_USER_PASS"

export jentrata.as2.db.url="jdbc:postgresql://$DB_HOST_NAME:5432/as2"
export jentrata.as2.db.user="$DB_USER_NAME"
export jentrata.as2.db.pass="$DB_USER_PASS"

export JAVA_OPTS="$JAVA_OPTS -Dcorvus.home=$JENTRATA_HOME"

/opt/wait-for-it.sh db:5432

exec catalina.sh run
