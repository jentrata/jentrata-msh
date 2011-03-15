# Hermes Messaging Gateway 2.0 (H2O)

This repo is a mirror of the Hermes Messaging Gateway developed by 
CECID, The University of Hong Kong but with a mavenized build.

See http://www.cecid.hku.hk/hermes.php for details on 

The code is based on the source downloaded from 
http://community.cecid.hku.hk/download/beta/hermes2_src_20100331.zip

We will keep the code on master as close as possible to the original
source but will maintain a branch for our changes

## Building

Requires maven 3.0+  and Java 6
	
	git clone git@github.com:<github-username>/Hermes-Gateway.git
	cd Hermes-Gateway
	mvn clean install

## Developer Notes

### GitHub Setup
1. Fork Repo into your base2 github account
2. Run on your local machine:
		git clone git@github.com:<github-username>/Hermes-Gateway.git
		cd Hermes-Gateway
		git remote add upstream git@github.com:base2Services/Hermes-Gateway.git

You can now pull from the upstream repository
	git pull upstream master
	
And push now push these changes to your forked repository
	git push
	
Also you should run the following
    git config --global user.name "Your Name"
    git config --global user.email <your_email>@base2services.com