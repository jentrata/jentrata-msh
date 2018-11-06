FROM jentrata/jenrata-msh:tomcat9
MAINTAINER Arran Ubels a.ubels@base2services.com

#install jdk to allow remote debugging
RUN apt-get update --fix-missing && \
    apt-get install -y openjdk-${JDK_VERSION}-jdk && \
    rm -rf /var/lib/apt/lists/*

ENV JENTRATA_VERSION 3.x-SNAPSHOT

COPY ./Dist/target/jentrata-msh-$JENTRATA_VERSION-tomcat.tar.gz /opt

#replace jentrata with locally built one
RUN rm -rfv /opt/jentrata && \
    mkdir -p /opt/jentrata && \
    tar -xzvf /opt/jentrata-msh-$JENTRATA_VERSION-tomcat.tar.gz -C /opt/jentrata && \
    rm /opt/jentrata-msh-$JENTRATA_VERSION-tomcat.tar.gz && \
    ln -s $JENTRATA_HOME/webapps/corvus $TOMCAT_HOME/webapps/jentrata


ENV JAVA_HOME /usr/lib/jvm/java-${JDK_VERSION}-openjdk-amd64

CMD ["/bin/sh", "/opt/run.sh"]