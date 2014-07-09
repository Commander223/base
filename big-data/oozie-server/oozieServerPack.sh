#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=oozie-client
downloadFileAndMakeChanges() {
        initializeVariables $1
        tempDirectory=$BASE/$fileName/opt/temp
        optDirectory=$BASE/$fileName/opt
        hadoopTarFile=hadoop-1.2.1-bin.tar.gz
	oozieTarFile=oozie-3.3.2.tar.gz
        distroTarFile=oozie-3.3.2-distro.tar.gz
	libextTarFile=libext.tar.gz
        extZipFile=ext-2.2.zip
	libextDirectory=libext
	extractedHadoopDirectory=hadoop-1.2.1
	extractedExtDirectory=ext-2.2
	extractedOozieDirectory=oozie-3.3.2
        mkdir -p $tempDirectory
        mkdir -p $optDirectory


	# Get necessary files
        wget http://www.apache.org/dist/hadoop/core/hadoop-1.2.1/$hadoopTarFile -P $tempDirectory
        wget http://archive.apache.org/dist/oozie/3.3.2/$oozieTarFile -P $tempDirectory
        wget extjs.com/deploy/$extZipFile -P $tempDirectory

	if  ls $optDirectory/README* ; then
                rm $optDirectory/README*
        fi

        # Unpack tar ball and make changes 
        pushd $tempDirectory
        tar -xpzf $oozieTarFile -C .
        rm $oozieTarFile
	tar -xvpzf $hadoopTarFile -C .
	tar -xvpzf $oozieTarFile -C .

	#Creating libext directory
	mkdir -p $libextDirectory
	cp $extractedHadoopDirectory/*.jar $libextDirectory/
	cp $extractedHadoopDirectory/lib/*.jar $libextDirectory

	#Creating oozie distro
	$extractedOozieDirectory/bin/mkdistro.sh -DskipTests
	cp $extractedOozieDirectory/distro/target/$distroTarFile $optDirectory
	if [ -d "$tempDirectory/../local.repository" ]; then
                rm -rf "$tempDirectory/../local.repository"
        fi
	popd

	# Remove temp directory
	pushd $BASE
	rm -r $tempDirectory
	popd

	#Extract tar files under opt directory
	pushd $optDirectory
	tar -xpzf $distroTarFile -C .
	rm -rf $distroTarFile
        mv $tempDirectory/$libextDirectory $extractedOozieDirectory/
	unzip  $extZipFile
	mv $extractedExtDirectory $extractedOozieDirectory/
	rm -rf $extZipFile
	popd
	rm -r $tempDirectory
}

# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download hadoop tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
