#!/bin/sh

BASE=/shared/tools/objectteams
STAGINGBASE=/opt/public/download-staging.priv/tools/objectteams

# Find the master repository to build upon:
MASTER=${HOME}/downloads/objectteams/updates/$1
if [ -r ${MASTER}/features ]
then
    echo "Generating Repository based on ${MASTER}"
else
    echo "No such repository ${MASTER}"
    echo "Usage: $0 updateMasterRelativePath [ -nosign ] [ statsRepoId statsVersionId ]"
    exit 1
fi

# Analyze the version number of the JDT feature as needed for patching content.xml later:
JDTFEATURE=`ls -d ${BASE}/testrun/build-root/eclipse/features/org.eclipse.jdt_*`
JDTVERSION=`echo ${JDTFEATURE} | cut -d '_' -f 2`
JDTVERSIONA=`echo ${JDTVERSION} | cut -d '-' -f 1`
JDTVERSIONB=`echo ${JDTVERSION} | cut -d '-' -f 2`
JDTVERSIONB2=`expr $JDTVERSIONB + 1`
JDTVERSIONB2=`printf "%04d" ${JDTVERSIONB2}`
echo "JDT feature is ${JDTVERSIONA}-${JDTVERSIONB}"
if [ ! -r ${BASE}/testrun/build-root/eclipse/features/org.eclipse.jdt_${JDTVERSIONA}-${JDTVERSIONB}-* ]
then
    echo "JDT feature not correctly found in ${BASE}/testrun/build-root/eclipse/features"
    exit 2
fi


# Configure for calling various p2 applications:
LAUNCHER=`grep equinox.launcher_jar= ${BASE}/build/run.properties | cut -d '=' -f 2`
LAUNCHER_PATH=${BASE}/testrun/build-root/eclipse/plugins/${LAUNCHER}
FABPUB=org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher
CATPUB=org.eclipse.equinox.p2.publisher.CategoryPublisher
NAME="Object Teams"

echo "LAUNCHER_PATH = ${LAUNCHER_PATH}"
echo "NAME          = ${NAME}"

echo "====Step 1: zip and request signing===="
cd ${BASE}/testrun/updateSite
JARS=`find . -name \*.jar -type f`
/bin/rm ${STAGINGBASE}/in/otdt.zip
zip ${STAGINGBASE}/in/otdt.zip ${JARS}
if [ "$2" == "-nosign" ]
then
    echo "SKIPING SIGNING"
    shift
else
    /bin/rm ${STAGINGBASE}/out/otdt.zip
    sign ${STAGINGBASE}/in/otdt.zip nomail ${STAGINGBASE}/out
fi
until [ -r ${STAGINGBASE}/out/otdt.zip ]
do
    sleep 10
    echo -n "."
done
echo "Signing completed"


echo "====Step 2: fill new repository===="
if [ -r ${BASE}/stagingRepo ]
then
    /bin/rm -rf ${BASE}/stagingRepo
fi
mkdir ${BASE}/stagingRepo
cd ${BASE}/stagingRepo
mkdir features
(cd features; ln -s ${MASTER}/features/* .)
mkdir plugins
(cd plugins; ln -s ${MASTER}/plugins/* .)
unzip ${STAGINGBASE}/out/otdt.zip

LOCATION=${BASE}/stagingRepo
echo "LOCATION  = ${LOCATION}"
cd ${LOCATION}


echo "====Step 3: generate metadata===="
java -jar ${LAUNCHER_PATH} -consoleLog -application ${FABPUB} \
    -source ${LOCATION} \
    -metadataRepository file:${LOCATION} \
    -artifactRepository file:${LOCATION} \
    -metadataRepositoryName "${NAME} Updates" \
    -artifactRepositoryName "${NAME} Artifacts"
ls -ltr *\.*


echo "====Step 4: generate category===="
CATEGORYARGS="-categoryDefinition file:${BASE}/testrun/build-root/src/features/org.eclipse.objectteams.otdt/category.xml"
echo "CATEGORYARGS  = ${CATEGORYARGS}"
java -jar ${LAUNCHER_PATH} -consoleLog -application ${CATPUB} \
    -source ${LOCATION} \
    -metadataRepository file:${LOCATION} \
    ${CATEGORYARGS}
ls -ltr *\.*


echo "====Step 5: patch content for feature inclusion version range===="
mv content.xml content.xml-orig
xsltproc  -o content.xml --stringparam version ${JDTVERSIONA}-${JDTVERSIONB} \
    --stringparam versionnext ${JDTVERSIONA}-${JDTVERSIONB2} \
    ../build/patch-content-xml.xsl content.xml-orig
ls -ltr *\.*

echo "====Step 6: add download stats capability===="
XSLT_FILE=${BASE}/bin/addDownloadStats.xsl

if [ $# == 3 ]; then
	mv artifacts.xml artifacts.xml.original
	if grep p2.statsURI artifacts.xml.original ; then echo "p2.statsURI already defined: exiting"; exit 1; fi
	xsltproc -o artifacts.xml --stringparam repo "http://download.eclipse.org/stats/objectteams/${2}" --stringparam version $3 $XSLT_FILE artifacts.xml.original
fi

echo "====Step 7: jar-up metadata===="
jar cf content.jar content.xml
jar cf artifacts.jar artifacts.xml
/bin/rm *.xml
ls -ltr *\.*

echo "====DONE===="
