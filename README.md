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

### How to get Jentrata Running under tomcat using postgresql

1. download and install tomcat 6.x or 7.x
2. untar jentrata-msh-tomcat.tar.gz
3. set JENTRATA_HOME environment variable to root jentrata-msh-tomcat dir for example

		export JENTRATA_HOME=/opt/jentrata-msh-tomcat

3. create a symlink to the <JENTRATA_HOME>/webapps/corvus in the <TOMCAT_HOME>/webapps directory to

		ln -s <JENTRTA_HOME>/webapps/corvus <TOMCAT_HOME>/webapps/corvus

4. set JAVA_OPTS environment variable as follows

		export JAVA_OPTS="$JAVA_OPTS -Dcorvus.home=$JENTRATA_HOME"

5. add the following roles and user to <TOMCAT_HOME>/conf/tomcat-users.xml

		<role rolename="admin"/>
		<role rolename="corvus"/>
		<user username="corvus" password="corvus" roles="tomcat,admin,corvus"/>

6. install postgresql 8.3+
7. create a postgres user corvus with default password corvus

		./createuser -s -d -P corvus
		
8. create the ebms and as2 databases

		./createdb -O corvus ebms
		./createdb -O corvus as2

9. Run the db create tables scripts

		./psql -f <JENTRTA_HOME>/sql/ebms.sql ebms
		./psql -f <JENTRTA_HOME>/as2.sql as2

10. Start tomcat and browse to [http://localhost:8080/corvus/admin/home](http://localhost:8080/corvus/admin/home)
