#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=elasticsearch
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/

	esVersion=1.2.1

	# Create directories that are required for the debian package
    mkdir -p $tempDirectory

	wget https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-$esVersion.deb -P $tempDirectory

	# export all files insdie debian file except DEBIAN folder
	dpkg-deb -x $BASE/$fileName/elasticsearch-$esVersion.deb $BASE/$fileName/

	# unpack tar ball and make changes 
	pushd $tempDirectory
	# remove debian file
	rm *.deb

	# replace configuration file
	rm $BASE/$fileName/etc/elasticsearch/elasticsearch.yml
	mv $BASE/$fileName/etc/elasticsearch/ksks-elasticsearch.yml $BASE/$fileName/etc/elasticsearch/elasticsearch.yml

	# replace service script of elasticsearch
	rm $BASE/$fileName/etc/init.d/elasticsearch
	mv $BASE/$fileName/etc/init.d/ksks-elasticsearch $BASE/$fileName/etc/init.d/elasticsearch
	popd
}

# 1) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 2) Download tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 3) Create the Debian package
generateDebianPackage $productName
