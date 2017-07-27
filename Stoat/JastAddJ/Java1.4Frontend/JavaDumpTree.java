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

class JavaDumpTree extends Frontend {
  public static void main(String args[]) {
    compile(args);
  }
  
  public static boolean compile(String args[]) {
    return new JavaDumpTree().process(
        args,
        new BytecodeParser(),
        new JavaParser() {
          parser.JavaParser parser = new parser.JavaParser();
          public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
            return parser.parse(is, fileName);
          }
        }
    );
  }

  protected void processErrors(Collection errors, CompilationUnit unit) {
    super.processErrors(errors, unit);
    System.out.println(unit.dumpTreeNoRewrite());
  }

  protected void processNoErrors(CompilationUnit unit) {
    System.out.println(unit.dumpTreeNoRewrite());
  }
}
