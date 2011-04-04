# Jentrata - Message Handler Service

Jentrata MSH is based on Hermes Messaging Gateway developed by 
CECID, The University of Hong Kong. The goal of Jentrata is build
on the orignial work developed by CECID as well as build a community to
continue it's development.

See http://www.cecid.hku.hk/hermes.php for details on Hermes

The code was based on the source downloaded from 
http://community.cecid.hku.hk/download/beta/hermes2_src_20100331.zip

## Building/Contributing

### GitHub Setup
1. Fork Repo into your github account
2. Run on your local machine:
		git clone git@github.com:<github-username>/jentrata-msh.git
		cd jentrata-msh.git
		git remote add upstream git@github.com:base2Services/jentrata-msh.git

You can now pull from the upstream repository
	git pull upstream master
	
And push now push these changes to your forked repository
	git push
	
Also you should run the following
    git config --global user.name "Your Name"
    git config --global user.email <your_email>

### Maven
Requires maven 3.0+  and Java 6
	
	git clone git@github.com:<github-username>/jentrata-msh.git
	cd jentrata-msh
	mvn clean install
    
### Jenkins CI

Jenkins is setup to merge changes from a branch into the master branch only if
that build passed.

