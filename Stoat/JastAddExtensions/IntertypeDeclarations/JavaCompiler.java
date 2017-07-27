/*
 * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
 * by the modified BSD License. You should have received a copy of the
 * modified BSD license with this compiler.
 * 
 * Copyright (c) 2005-2008, Torbjorn Ekman
 * All rights reserved.
 */

import AST.*;

import java.util.*;
import java.io.*;

import AST.List;

public class JavaCompiler extends Frontend {

  public static void main(String args[]) {
    if(!compile(args))
      System.exit(1);
  }

  public static boolean compile(String args[]) {
    JavaCompiler c = new JavaCompiler();
    boolean result = c.process(
       args,
       new BytecodeParser(),
       new JavaParser() {
          public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
            return new parser.JavaParser().parse(is, fileName);
          }
       }
    );
    if(!result) return false;
    c.generate();
    return true;
  }

  public void generate() {
    program.generateIntertypeDecls();
    program.transformation();
    if(program.options().verbose())
      System.out.println(program.toString());

    for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
      CompilationUnit cu = (CompilationUnit)iter.next();
      if(cu.fromSource()) {
        for(int i = 0; i < cu.getNumTypeDecl(); i++) {
          cu.getTypeDecl(i).generateClassfile();
        }
      }
    }
  }

  protected String name() {
    return "Java5 + ITDs";
  }

  protected void initOptions() {
    super.initOptions();
    program.options().addKeyOption("-weave_inline");
  }
}
