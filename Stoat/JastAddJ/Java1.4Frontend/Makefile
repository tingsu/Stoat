JASTADD=java -jar tools/jastadd2.jar --novisitcheck
#JAVACOMPILER = javac
JAVACOMPILER = jikes -nowarn
#JAVACOMPILER = ajc
JFLEXCOMPILER = java -jar tools/JFlex.jar --nobak
BEAVERCOMPILER = java -jar tools/beaver.jar

# -- Names of the main programs --
MAIN=JavaPrettyPrinter.java JavaDumpTree.java

# -- Names of additional hand-written java files
LIB=

# -- All handwritten Java-files
HANDWRITTENJAVA=$(MAIN) $(LIB)

# -- Jrag files
JRAGFILES=PrettyPrint.jadd TypeAnalysis.jrag LookupVariable.jrag LookupType.jrag LookupMethod.jrag DeclareBeforeUse.jrag ResolveAmbiguousNames.jrag Helper.jrag PrimitiveTypes.jrag  VariableDeclaration.jrag ErrorCheck.jrag TypeCheck.jrag AccessControl.jrag ExceptionHandling.jrag LookupLabel.jrag UnreachableStatements.jrag DefiniteAssignment.jrag ConstantExpression.jrag NameCheck.jrag Modifiers.jrag ClassPath.jrag UpdateRemoteAttributeWrites.jadd Options.jadd

ASTFILES = java.ast

PARSERFILES = parser/java14.parser
PARSERFILES += $(EXTERNALPARSERFILES)

ifndef SCANNER
  SCANNER = parser/java14.flex
endif

# -- Name of the start nonterminal for the abstract syntax tree --
STARTSYMBOL=Program

# -- Package name --
PACKAGE=AST

# -- Node prefix --
NODEPREFIX=

JRAGFILES += $(EXTERNALJRAGFILES)
HANDWRITTENJAVA += $(EXTERNALJAVAFILES)
ifndef BUILDDIR
  BUILDDIR = .
endif

ifeq ($(JAVACOMPILER),jikes -nowarn)
 JAVACLASSPATH += $(JIKESPATH):$(CLASSPATH):.
else
  JAVACLASSPATH += $(CLASSPATH):.
endif

$(HANDWRITTENJAVA:java=class):	$(BUILDDIR)/$(PACKAGE)/$(NODEPREFIX)$(STARTSYMBOL).java $(HANDWRITTENJAVA) $(BUILDDIR)/parser/JavaParser.java $(BUILDDIR)/parser/JavaScanner.java $(BUILDDIR)/parser/UnicodeEscapes.java
	$(JAVACOMPILER) -d $(BUILDDIR) -classpath "$(JAVACLASSPATH)" $(HANDWRITTENJAVA) $(BUILDDIR)/$(PACKAGE)/*.java bytecode/*.java $(BUILDDIR)/parser/JavaParser.java $(BUILDDIR)/parser/JavaScanner.java $(BUILDDIR)/parser/UnicodeEscapes.java

# -- Run AST class generation --
$(BUILDDIR)/$(PACKAGE)/$(NODEPREFIX)$(STARTSYMBOL).java:	$(ASTFILES) $(JRAGFILES) Makefile
	$(JASTADD) --rewrite --beaver --o=$(BUILDDIR) --package=$(PACKAGE) --prefix="$(NODEPREFIX)" $(JRAGFILES) $(ASTFILES)

# -- Run test cases --
test:	$(HANDWRITTENJAVA:java=class)
	$(TESTSCRIPTS)

$(BUILDDIR)/parser/JavaScanner.java: $(SCANNER)
	$(JFLEXCOMPILER) -d $(BUILDDIR)/parser $^

$(BUILDDIR)/parser/UnicodeEscapes.java: parser/unicode.flex
	$(JFLEXCOMPILER) -d $(BUILDDIR)/parser $^

$(BUILDDIR)/parser/JavaParser.beaver: $(PARSERFILES) Makefile
	cat $(PARSERFILES) >$(BUILDDIR)/parser/JavaParser.all && java -cp tools/proj.jar:tools/beaver-rt.jar Main $(BUILDDIR)/parser/JavaParser.all $(BUILDDIR)/parser/JavaParser.beaver
$(BUILDDIR)/parser/JavaParser.java: $(BUILDDIR)/parser/JavaParser.beaver Makefile
	$(BEAVERCOMPILER) -w  $(BUILDDIR)/parser/JavaParser.beaver

# -- Clean up by removing generated files and backup files from text editors --
clean:
	/bin/rm -f $(BUILDDIR)/$(PACKAGE)/*.class
	/bin/rm -f $(BUILDDIR)/$(PACKAGE)/*.java
	/bin/rm -f $(BUILDDIR)/bytecode/*.class
	/bin/rm -f $(BUILDDIR)/parser/*.class
	/bin/rm -f $(BUILDDIR)/parser/JavaScanner.java
	/bin/rm -f $(BUILDDIR)/parser/JavaParser.java
	/bin/rm -f $(BUILDDIR)/parser/UnicodeEscapes.java
	/bin/rm -f $(BUILDDIR)/*.class
	/bin/rm -f $(BUILDDIR)/*~
