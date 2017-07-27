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

class JavaPrettyPrinter extends Frontend {
  public static void main(String args[]) {
    if(!compile(args))
      System.exit(1);
  }

  public static boolean compile(String args[]) {
    return new JavaPrettyPrinter().process(
        args,
        new BytecodeParser(),
        new JavaParser() {
          public CompilationUnit parse(InputStream is, String fileName) throws IOException, beaver.Parser.Exception {
            return new parser.JavaParser().parse(is, fileName);
          }
        }
    );
  }

  protected void processErrors(Collection errors, CompilationUnit unit) {
    super.processErrors(errors, unit);
    System.out.println(unit);
  }
  protected void processNoErrors(CompilationUnit unit) {
    System.out.println(unit);
  }

  protected String name() { return "JavaPrettyPrinter"; }
  protected String version() { return "R20071015"; }
}
