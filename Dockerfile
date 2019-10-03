FROM jentrata/jentrata-msh:3
MAINTAINER Arran Ubels a.ubels@base2services.com

ENV JENTRATA_VERSION 3.x-SNAPSHOT

COPY ./Dist/target/jentrata-msh-$JENTRATA_VERSION-tomcat.tar.gz /opt

#replace jentrata with locally built one
RUN rm -rfv /opt/jentrata && \
    mkdir -p /opt/jentrata && \
    tar -xzvf /opt/jentrata-msh-$JENTRATA_VERSION-tomcat.tar.gz -C /opt/jentrata && \
    rm /opt/jentrata-msh-$JENTRATA_VERSION-tomcat.tar.gz && \
    ln -s $JENTRATA_HOME/webapps/corvus $TOMCAT_HOME/webapps/jentrata


CMD ["/bin/sh", "/opt/run.sh"]