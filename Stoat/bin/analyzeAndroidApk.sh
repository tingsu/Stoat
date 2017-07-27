#!/bin/bash

ANDROID_JARS_PATH="./libs/android-platforms"
JAVA_CLASSPATH="\
./soot-github/lib/soot-develop.jar:\
./soot-github/libs/AXMLPrinter2.jar:\
./server/Server.jar:
"

OPTION=$1
APP_SOURCE_DIR=$2
PROJECT_TYPE=$3
APK_PATH=$4

SOOT_OUT_DIR="./out"
SERVER_DIR="./server"
BIN="./bin"


###############
# Configurations

function init(){

	echo "-----"

	AppLocation=${APP_SOURCE_DIR%/*}
	echo "app location: ${AppLocation}"
	app_src_dir_simple_name=${APP_SOURCE_DIR##*/}
	echo "app src dir simple name: ${app_src_dir_simple_name}"


	# At the location "./AppRepo", prepare three dirs:
	# "app-name/", "app-name-instr/", "app-name-apks/"
	OriginalAppSource="${AppLocation}/${app_src_dir_simple_name}"
	echo "OriginalAppSource: ${OriginalAppSource}"

	echo "-----"

}
##############


function compile_app (){

echo "the current directory: `pwd`"
sleep 1
echo "the original app source code directory: ${OriginalAppSource}"
sleep 1
cd ${OriginalAppSource}
echo "the current directory: `pwd`"
echo "start building the app in debug mode ... "
sleep 1

# update android project (SDK version 16)
android update project --target android-18 --path .


ant clean
ant debug # compile and build the original app source code

echo "---------------"
ant instrument # compile and build the instrumented app source code

}

function build_fsm(){

echo `pwd`

# compile the MCMC-droid project in case that its source codes are changed
#rm -rf ${SERVER_DIR}/*.class
#javac ${SERVER_DIR}/*.java

DebugApk=""
if [ "$PROJECT_TYPE" = "ant" ] 
then
	DebugApk=`ls ${APP_SOURCE_DIR}/bin/*-debug.apk`
else
	DebugApk=$APK_PATH
fi

echo "the apk file: $DebugApk"

# invoke apktool to decode the apk file
echo "------------------"
echo "[invoke apktool to decode the apk file ...]"
${BIN}/apktool.sh d -f -o ${DebugApk%.apk} ${DebugApk}
echo "------------------"

# invoke soot to do static analysis (detect actions statically)

PROCESS_THIS=" -process-dir $DebugApk"

SOOT_CMD="SocketServer \
$APP_SOURCE_DIR \
--apk-name ${DebugApk%.apk} \
-w \
-keep-line-number \
-allow-phantom-refs \
-src-prec apk \
-android-jars $ANDROID_JARS_PATH \
-f J \
-d ${SOOT_OUT_DIR} \
$PROCESS_THIS
"

java \
-Xss50m \
-Xmx1500m \
-classpath  ${JAVA_CLASSPATH} \
${SOOT_CMD}\

}


function build_fsm_apk(){

# compile the MCMC-droid project in case that its source codes are changed
#rm -rf ${SERVER_DIR}/*.class
#javac ${SERVER_DIR}/*.java


DebugApk=$APP_SOURCE_DIR # *.apk
OutputDir=${DebugApk%.apk}-output

# invoke apktool to decode the apk file
echo "------------------"
echo "[invoke apktool to decode the apk file ...]"
${BIN}/apktool.sh d -f -o ${DebugApk%.apk} ${DebugApk}
echo "------------------"

# invoke soot to do static analysis (detect actions statically)

PROCESS_THIS=" -process-dir $DebugApk"

SOOT_CMD="SocketServer \
$OutputDir \
--apk-name ${DebugApk%.apk} \
-w \
-keep-line-number \
-allow-phantom-refs \
-src-prec apk \
-android-jars $ANDROID_JARS_PATH \
-f J \
-d ${SOOT_OUT_DIR} \
$PROCESS_THIS
"

java \
-Xss50m \
-Xmx1500m \
-classpath  ${JAVA_CLASSPATH} \
${SOOT_CMD}\

}

function MCMC_sample(){

# compile the MCMC-droid project in case that its source codes are changed
#rm -rf ${SERVER_DIR}/*.class
#javac ${SERVER_DIR}/*.java

MCMC_CMD="MCMCSampler --test \
$APP_SOURCE_DIR
"

java \
-Xss50m \
-Xmx1500m \
-classpath  ${JAVA_CLASSPATH} \
${MCMC_CMD}\

}



function MCMC_sample_apk(){

# compile the MCMC-droid project in case that its source codes are changed
#rm -rf ${SERVER_DIR}/*.class
#javac ${SERVER_DIR}/*.java

DebugApk=$APP_SOURCE_DIR # *.apk
OutputDir=${DebugApk%.apk}-output

MCMC_CMD="MCMCSampler --test \
$OutputDir
"

java \
-Xss50m \
-Xmx1500m \
-classpath  ${JAVA_CLASSPATH} \
${MCMC_CMD}\

}



function MCMC_compare(){

# compile the MCMC-droid project in case that its source codes are changed
#rm -rf ${SERVER_DIR}/*.class
#javac ${SERVER_DIR}/*.java

MCMC_CMD="MCMCSampler --compare\
"

java \
-Xss50m \
-Xmx1500m \
-classpath  ${JAVA_CLASSPATH} \
${MCMC_CMD}\

}



echo "options: ${OPTION}"
echo "app source dir: ${APP_SOURCE_DIR}"

init

if [ "${OPTION}" = "compile" ]; then
# uncomment this to compile the app from its source codes
compile_app
elif [ "${OPTION}" = "fsm" ]; then
build_fsm
elif [ "${OPTION}" = "fsm_apk" ]; then
build_fsm_apk
elif [ "${OPTION}" = "mcmc" ]; then
MCMC_sample
elif [ "${OPTION}" = "mcmc_apk" ]; then
MCMC_sample_apk
elif [ "${OPTION}" = "mcmc-compare" ]; then
MCMC_compare
else
# At the location "./AppRepo", prepare three dirs:
# "app-name/", "app-name-instr/", "app-name-apks/"
echo "MCMC-droid"
echo "Current Configuration: compile apps as \"android-16\" "
echo "Usage: ./bin/analyzeAndroidApk.sh Options App_src_dir"
echo "Options:  \"compile\", \"fsm\", \"mcmc\" "
echo "Example: ./bin/analyzeAndroidApk.sh compile ./AppRepo/HelloAndroidWithMenus"
fi

