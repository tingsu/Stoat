#!/bin/bash

# Author: Emma (emma@cs.lth.se)
# Date: Jan/8/2010
# Comment: Script to simplify running of test apps

# Setup Xalan
function run-xalan {
	echo ''
	echo ' == Xalan == '
	echo ''
	TEST_SET_DIR='testsets/dacapo/xalan'
	TEST_SET_FILE='testsets/dacapo/xalan/xalan_filelist'
	SRC_ROOT='xalan-j_2_7_1'
	CLASS_PATH=$SRC_ROOT'/lib/BCEL.jar:'$SRC_ROOT'/lib/regexp.jar:'$SRC_ROOT'/lib/runtime.jar:'$SRC_ROOT'/lib/xercesImpl.jar:'$SRC_ROOT'/lib/xml-apis.jar:'$SRC_ROOT'/tools/java_cup.jar'
	RESULT_FILE='result.xalan'
	./compile.sh $TEST_SET_DIR $TEST_SET_FILE $RESULT_FILE $CLASS_PATH
}

# Setup PMD
function run-pmd {
	echo ''
	echo ' == PMD == '
	echo ''
	TEST_SET_DIR='testsets/dacapo/pmd'
	TEST_SET_FILE='testsets/dacapo/pmd/pmd_filelist'
	CLASS_PATH='svn/pmd/trunk/pmd/lib/saxon9.jar:svn/pmd/trunk/pmd/lib/asm-3.2.jar:svn/pmd/trunk/pmd/lib/javacc.jar:svn/pmd/trunk/pmd/lib/junit-4.4.jar:svn/pmd/trunk/pmd/lib/jaxen-1.1.1.jar:svn/pmd/trunk/pmd/lib/js-cvs-11282008.jar:../common/ant.jar'
	RESULT_FILE='result.pmd'
	./compile.sh $TEST_SET_DIR $TEST_SET_FILE $RESULT_FILE $CLASS_PATH
}

# Setup Lucene
function run-lucene {
	echo ''
	echo ' == Lucene == '
	echo ''
	TEST_SET_DIR='testsets/dacapo/lucene'
	TEST_SET_FILE='testsets/dacapo/lucene/lucene_filelist'
	CLASS_PATH='svn/lucene/java/trunk/lib/servlet-api-2.4.jar:svn/lucene/java/trunk/lib/junit-3.8.2.jar'
	RESULT_FILE='result.lucene'
	./compile.sh $TEST_SET_DIR $TEST_SET_FILE $RESULT_FILE $CLASS_PATH
}

# Setup Jython
function run-jython {
	echo ''
	echo ' == Jython == '
	echo ''
	TEST_SET_DIR='testsets/dacapo/jython'
	TEST_SET_FILE='testsets/dacapo/jython/jython_filelist'
	CLASS_PATH='../common/servlet.jar:../common/ant.jar'
	RESULT_FILE='result.jython'
	./compile.sh $TEST_SET_DIR $TEST_SET_FILE $RESULT_FILE $CLASS_PATH
}

# Setup hsqldb
function run-hsqldb {
	echo ''
	echo ' == Hsqldb == '
	echo ''
	TEST_SET_DIR='testsets/dacapo/hsqldb'
	TEST_SET_FILE='testsets/dacapo/hsqldb/hsqldb_filelist'
	CLASS_PATH='../common/ant.jar:../common/servlet.jar:../common/junit-4.7.jar'
	RESULT_FILE='result.hsqldb'
	./compile.sh $TEST_SET_DIR $TEST_SET_FILE $RESULT_FILE $CLASS_PATH
}

# Setup FOP
function run-fop {
	echo ''
	echo ' == FOP == '
	echo ''
	TEST_SET_DIR='testsets/dacapo/fop'
	TEST_SET_FILE='testsets/dacapo/fop/fop_filelist'
	CLASS_PATH='svn/trunk/lib/avalon-framework-4.2.0.jar:svn/trunk/lib/batik-all-1.7.jar:svn/trunk/lib/commons-io-1.3.1.jar:svn/trunk/lib/xalan-2.7.0.jar:svn/trunk/lib/commons-logging-1.0.4.jar:svn/trunk/lib/xml-apis-ext-1.3.04.jar:svn/trunk/lib/serializer-2.7.0.jar:svn/trunk/lib/xml-apis-1.3.04.jar:svn/trunk/lib/xmlgraphics-commons-1.4svn.jar:svn/trunk/lib/xercesImpl-2.7.1.jar:svn/trunk/lib/servlet-2.2.jar:../common/ant.jar:svn/trunk/build/fop.jar:svn/trunk/build/fop-hyph.jar:svn/trunk/src/java'
	RESULT_FILE='result.fop'
	./compile.sh $TEST_SET_DIR $TEST_SET_FILE $RESULT_FILE $CLASS_PATH
}

# Setup Chart
function run-chart {
	echo ''
	echo ' == Chart == '
	echo ''
	TEST_SET_DIR='testsets/dacapo/chart'
	TEST_SET_FILE='testsets/dacapo/chart/chart_filelist'
	SRC_ROOT='krysalis-jCharts-1.0.0-alpha-1'
	CLASS_PATH=$SRC_ROOT'/lib/servlet_2_2.jar:'$SRC_ROOT'/lib/batik-awt-util.jar:'$SRC_ROOT'/lib/batik-dom.jar:'$SRC_ROOT'/lib/batik-svggen.jar:'$SRC_ROOT'/lib/batik-util.jar:'$SRC_ROOT'/lib/batik-xml.jar:'$SRC_ROOT'/lib/junit.jar'
	RESULT_FILE='result.chart'
	./compile.sh $TEST_SET_DIR $TEST_SET_FILE $RESULT_FILE $CLASS_PATH
}

# Setup Bloat
function run-bloat {
	echo ''
	echo ' == Bloat == '
	echo ''
	TEST_SET_DIR='testsets/dacapo/bloat'
	TEST_SET_FILE='testsets/dacapo/bloat/bloat_filelist'
	CLASS_PATH=''
	RESULT_FILE='result.bloat'
	./compile.sh $TEST_SET_DIR $TEST_SET_FILE $RESULT_FILE $CLASS_PATH
}

# Setup Antlr
function run-antlr {
	echo ''
	echo ' == Antlr == '
	echo ''
	TEST_SET_DIR='testsets/dacapo/antlr'
	TEST_SET_FILE='testsets/dacapo/antlr/antlr_filelist'
	CLASS_PATH=$TEST_SET_DIR'/antlr-2.7.2/antlr'
	RESULT_FILE='result.antlr'
	./compile.sh $TEST_SET_DIR $TEST_SET_FILE $RESULT_FILE $CLASS_PATH
}

# Setup small
function run-small {
	echo ''
	echo ' == small == '
	echo ''
	TEST_SET_DIR='test'
	TEST_SET_FILE='test/small_filelist'
	CLASS_PATH=$TEST_SET_DIR
	RESULT_FILE='result.small'
	./compile.sh $TEST_SET_DIR $TEST_SET_FILE $RESULT_FILE $CLASS_PATH
}

run-small
#run-antlr
#run-bloat
#run-chart
#run-fop
#run-hsqldb
#run-jython
#run-lucene
#run-pmd
#run-xalan

exit
