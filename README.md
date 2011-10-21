# Jentrata - Message Handler Service

Jentrata MSH is based on Hermes Messaging Gateway developed by CECID, The University of Hong Kong. The goal of Jentrata is build on the orignial work developed by CECID as well as build a community to continue it's development.

See [jentrata.org](http://jentrata.org) for complete details on the project

Build Status - [Current](https://jentrata.ci.cloudbees.com/job/jentrata-msh-master/lastBuild/buildStatus)

## Building/Contributing

### GitHub Setup
1. Fork Repo into your github account
2. Run on your local machine:

		git clone git@github.com:<github-username>/jentrata-msh.git
		cd jentrata-msh.git
		git remote add upstream git@github.com:jentrata/jentrata-msh.git

You can now pull/rebase from the upstream repository

	git pull upstream master
	
And push now push these changes to your forked repository

	git push
	
Also you should run the following

    git config --global user.name "Your Name"
    git config --global user.email <your_email>

### How to Build Jentrata from source
Requires maven 3.0+  and Java 6
	
	git clone git@github.com:<github-username>/jentrata-msh.git
	cd jentrata-msh
	mvn clean install
	
This will create jentrata-msh-tomcat.tar.gz in jentrata-msh/Dist/target/ as well as an uncompressed version

### How to get Jentrata Running on tomcat using PostgreSQL on unix (incuding Mac) systems

## Install and configure Tomcat

1. download and install tomcat 6.x or 7.x
2. untar jentrata-msh-tomcat.tar.gz
3. set JENTRATA_HOME and TOMCAT_HOME environment variables, for example

		export JENTRATA_HOME=/dev/jentrata-msh-tomcat
		export TOMCAT_HOME=/dev/tomcat
		
3. create a symlink to the $JENTRATA_HOME/webapps/corvus in the $TOMCAT_HOME/webapps directory to

		ln -s $JENTRATA_HOME/webapps/corvus $TOMCAT_HOME/webapps/corvus

4. set JAVA_OPTS environment variable as follows

		export JAVA_OPTS="$JAVA_OPTS -Dcorvus.home=$JENTRATA_HOME"

5. add the following roles and user to $TOMCAT_HOME/conf/tomcat-users.xml

		<role rolename="admin"/>
		<role rolename="corvus"/>
		<user username="corvus" password="corvus" roles="tomcat,admin,corvus"/>

## Install and configure database

### PostgreSQL

1. install postgresql 8.3+ - During the install, for the default user 'postgres', make the password 'postgres' (ignore quotes). For Mac OSX you should read the link - [memory configuration info for OSX](http://support.bitrock.com/article/postgresql-cannot-allocate-memory-on-mac-os-x)

2. copy the database creation scripts to your PostgreSQL install

		copy JENTRATA_HOME/sql/ebms.sql to the PostgreSQL bin directory
		copy JENTRATA_HOME/sql/as2.sql to the PostgreSQL bin directory

3. log in as the PostgreSQL user 'postgres' created during the install

4. set the password for the default user 'postgres' as a temporary environment variable

		export PGPASSWORD="postgres"

5. cd to the PostgreSQL bin directory

6. create a postgres user corvus with default password corvus

		./createuser -s -d -P corvus
		
7. create the ebms and as2 databases

		./createdb -O corvus ebms
		./createdb -O corvus as2
		
8. Run the db create tables scripts

		./psql -f ebms.sql ebms
		./psql -f as2.sql as2

9. Logout as user postgres

### MySQL

1. Install MySQL for your OS (http://mysql.com)

2. Create your databases

		mysql -e "CREATE DATABASE ebms;"
		mysql -e "CREATE DAYABASE as2;"

3. Create users/permissions to new databases

		mysql -e "GRANT ALL TO 'ebms'@'localhost' IDENTIFIED BY 'ebms';"
		mysql -e "GRANT ALL TO 'as2'@'localhost' IDENTIFIED BY 'as2';"

4. Import DB Schema

		mysql ebms < JENTRATA_HOME/sql/mysql_ebms.sql
		mysql as2 < JENTRATA_HOME/sql/mysql_as2.sql

5. Configure Jentrata to use MySQL

	You will need to modify the some parameters of the doafactory component in the 'plugins' directory for each plugin of Jentrata that you wish to use with MySQL.
    
		JENTRATA_HOME/plugins/hk.hku.cecid.ebms/conf/hk/hku/cecid/ebms/spa/conf/ebms.module.xml
		JENTRATA_HOME/plugins/hk.hku.cecid.edi.as2/conf/hk/hku/cecid/edi/as2/conf/as2.module.core.xml

	Look for the following Component:
        
		<component id="daofactory" name="System DAO Factory">

	Find the following parameters and change them as follows:

		<parameter name="driver" value="com.mysql.jdbc.Driver"/>
		<parameter name="url" value="jdbc:mysql://127.0.0.1:3306/ebms"/>

## Run Jentrata

1. Start tomcat and browse to [http://localhost:8080/corvus/admin/home](http://localhost:8080/corvus/admin/home). You will need to login using the username and password you set in the tomcat-users.xml corvus/corus by default

2. If Jentrata doesn't start correctly you can check the various log files under $TOMCAT_HOME/logs/ or $JENTRATA_HOME/logs for errors
