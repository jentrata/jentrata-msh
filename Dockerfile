FROM jentrata/jentrata-msh
MAINTAINER Arran Ubels a.ubels@base2services.com

COPY ./Dist/target/jentrata-msh-2.x-SNAPSHOT-tomcat.tar.gz /opt

RUN rm -rfv /opt/jentrata && \
    mkdir -p /opt/jentrata && \
    tar -xzvf /opt/jentrata-msh-2.x-SNAPSHOT-tomcat.tar.gz -C /opt/jentrata && \
    rm /opt/jentrata-msh-2.x-SNAPSHOT-tomcat.tar.gz && \
    ln -s $JENTRATA_HOME/webapps/corvus $TOMCAT_HOME/webapps/jentrata && \
    chmod a+x /opt/wait-for-it.sh

CMD ["/bin/sh", "/opt/run.sh"]