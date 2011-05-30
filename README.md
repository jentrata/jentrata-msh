# Jentrata - Message Handler Service

Jentrata MSH is based on Hermes Messaging Gateway developed by CECID, The University of Hong Kong. The goal of Jentrata is build on the orignial work developed by CECID as well as build a community to continue it's development.

See [jentrata.org](http://jentrata.org) for complete details on the project

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

## Install and configure database - here PostgreSQL

1. install postgresql 8.3+ - During the install, for the default user 'postgres', make the password 'postgres' (ignore quotes). On Mac OSX you should read the following link

		http://support.bitrock.com/article/postgresql-cannot-allocate-memory-on-mac-os-x

2. copy the database creation scripts to your PostgreSQL install

		copy JENTRATA_HOME/sql/ebms.sql to the PostgreSQL bin directory
		copy JENTRATA_HOME/sql/as2.sql to the PostgreSQL bin directory

3. log in as the PostgreSQL user 'postgres' created during the install

4. set the password for the default user 'postgres' as a temporary environment variable

		export PGPASSWORD="postgres"

5. create a postgres user corvus with default password corvus

		./createuser -s -d -P corvus
		
6. create the ebms and as2 databases

		./createdb -O corvus ebms
		./createdb -O corvus as2
		
7. Run the db create tables scripts

		./psql -f ebms.sql ebms
		./psql -f as2.sql as2

8. Logout as user postgres

## Run Jentrata

1. Start tomcat and browse to [http://localhost:8080/corvus/admin/home](http://localhost:8080/corvus/admin/home). You will need to login using the username and password you set in the tomcat-users.xml corvus/corus by default

2. If Jentrata doesn't start correctly you can check the various log files under $TOMCAT_HOME/logs/ or $JENTRATA_HOME/logs for errors
