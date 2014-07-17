#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=pig
downloadHadoopAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt

	confDirectory=$BASE/$fileName/etc/$productName
	pigVersion=0.13.0
	pigTarFile=pig-$pigVersion.tar.gz

	# Create directories that are required for the debian package
    mkdir -p $confDirectory
	mkdir -p $tempDirectory

	wget http://archive.apache.org/dist/pig/pig-$pigVersion/$pigTarFile -P $tempDirectory
	if [ -f $BASE/$fileName/opt/README ]; then
	        rm $BASE/$fileName/opt/README
	fi
	# unpack tar ball and make changes 
	pushd $tempDirectory
	tar -xpf $pigTarFile -C .
	rm $pigTarFile

	# move conf directory
	mv pig*/conf/* $confDirectory
	popd
}

# 1) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 2) Download pig tar file and make necessary changes
downloadHadoopAndMakeChanges $productName
# 3) Create the Debian package
generateDebianPackage $productName