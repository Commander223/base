#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=derby
downloadFileAndMakeChanges() {
        initializeVariables $1
        tempDirectory=$BASE/$fileName/opt
        tarFile=db-derby-10.4.2.0-bin.tar.gz

        # Create directories that are required for the debian package
        mkdir -p $tempDirectory

        wget -P opt http://archive.apache.org/dist/db/derby/db-derby-10.4.2.0/$tarFile -P $tempDirectory
        if [ -f $BASE/$fileName/opt/README ]; then
                rm $BASE/$fileName/opt/README
        fi
}

# 1) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 2) Download tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 3) Create the Debian package
generateDebianPackage $productName
