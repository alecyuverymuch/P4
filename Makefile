###
# This Makefile can be used to make a parser for the cimple language
# (parser.class) and to make a program (P3.class) that tests the parser and
# the unparse methods in ast.java.
#
# make clean removes all generated files.
#
###

JC = javac
CP = ../deps_src/java-cup-11b.jar:../deps_src/java-cup-11b-runtime.jar:../deps_src:.
FLAGS = -g -cp $(CP)

P4.class: P4.java parser.class Yylex.class ASTnode.class
	$(JC) $(FLAGS)  P4.java

parser.class: parser.java ASTnode.class Yylex.class ErrMsg.class
	$(JC) $(FLAGS) parser.java

parser.java: cimple.cup
	java -cp $(CP)  java_cup.Main < cimple.cup

Yylex.class: cimple.jlex.java sym.class ErrMsg.class
	$(JC) $(FLAGS)  cimple.jlex.java

ASTnode.class: ast.java
	$(JC)  $(FLAGS)  ast.java

cimple.jlex.java: cimple.jlex sym.class
	java  -cp $(CP)  JLex.Main cimple.jlex

sym.class: sym.java
	$(JC) $(FLAGS)  sym.java

sym.java: cimple.cup
	java -cp $(CP)  java_cup.Main < cimple.cup

ErrMsg.class: ErrMsg.java
	$(JC) $(FLAGS) ErrMsg.java

##test
test:
	java -cp $(CP) P4 test.cimple test.out

###
# clean
###
clean:
	rm -f *~ *.class parser.java cimple.jlex.java sym.java
