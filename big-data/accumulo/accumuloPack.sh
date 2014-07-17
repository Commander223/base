#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=accumulo
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/accumulo

	accumuloVersion=1.6.0

	# Create directories that are required for the debian package
    mkdir -p $tempDirectory
    mkdir -p $confDirectory

	# download accumulo
	wget http://archive.apache.org/dist/accumulo/$accumuloVersion/accumulo-$accumuloVersion-bin.tar.gz -P $tempDirectory
	
	pushd $tempDirectory
	tar -xzpf accumulo-$accumuloVersion-bin*.tar.gz

	# remove tar file
	rm accumulo-$accumuloVersion-bin*.tar.gz
	
	# move configuration files 
	mv accumulo-$accumuloVersion/conf/* $confDirectory

	# copy sample configuration files
	cp -a $confDirectory/examples/1GB/native-standalone/* $confDirectory/
	popd
}
# 1) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 2) Download tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 3) Create the Debian package
generateDebianPackage $productName