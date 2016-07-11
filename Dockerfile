FROM jentrata/jentrata-msh
MAINTAINER Arran Ubels a.ubels@base2services.com

#install jdk to allow remote debugging
RUN apt-get update --fix-missing && \
    apt-get install -y openjdk-7-jdk && \
    rm -rf /var/lib/apt/lists/*

COPY ./Dist/target/jentrata-msh-2.x-SNAPSHOT-tomcat.tar.gz /opt

#replace jentrata with locally built one
RUN rm -rfv /opt/jentrata && \
    mkdir -p /opt/jentrata && \
    tar -xzvf /opt/jentrata-msh-2.x-SNAPSHOT-tomcat.tar.gz -C /opt/jentrata && \
    rm /opt/jentrata-msh-2.x-SNAPSHOT-tomcat.tar.gz && \
    ln -s $JENTRATA_HOME/webapps/corvus $TOMCAT_HOME/webapps/jentrata


ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64

CMD ["/bin/sh", "/opt/run.sh"]