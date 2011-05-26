# Jentrata - Message Handler Service

Jentrata MSH is based on Hermes Messaging Gateway developed by 
CECID, The University of Hong Kong. The goal of Jentrata is build
on the orignial work developed by CECID as well as build a community to
continue it's development.

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
    


