#!/bin/sh

TEST_SET_DIR=`pwd`'/'$1
TEST_SET_FILE=`pwd`'/'$2
RESULT_FILE=`pwd`'/'$3
CLASS_PATH=$4

echo '- Using compiler classpath ['$CLASS_PATH']'
echo '- Using test set file ['$TEST_SET_FILE']'
echo '- Using result file ['$RESULT_FILE']'

COMPILER_PATH=`pwd`
echo '- Compiler path ['$COMPILER_PATH']'

COMPILER_CLASS='JavaCheckerTimeMem'
echo '- Compiler class ['$COMPILER_CLASS']'

JAVAC='java -Xss4M -Xmx2G '$COMPILER_CLASS
echo '- Compiling with ['$JAVAC']'

# Set the class path to the compiler
export CLASSPATH=$COMPILER_PATH:.
echo '- Setting the classpath to ['$CLASSPATH']'

# Move to the directory of the test set
echo '- Moving to ['$TEST_SET_DIR']'
cd $TEST_SET_DIR

$JAVAC $RESULT_FILE -classpath $CLASS_PATH:. @$TEST_SET_FILE

exit
