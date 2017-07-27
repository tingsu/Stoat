/*
 * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
 * by the modified BSD License. You should have received a copy of the
 * modified BSD license with this compiler.
 * 
 * Copyright (c) 2005-2008, Torbjorn Ekman
 *               2011, Jesper Öqvist <jesper.oqvist@cs.lth.se>
 * All rights reserved.
 */

package org.jastadd.jastaddj;

import AST.*;
import java.util.*;

public class JavaCompiler extends Frontend {
	public static void main(String args[]) {
		if(!compile(args))
			System.exit(1);
	}

	public static boolean compile(String args[]) {
		boolean result = new JavaCompiler().process(
				args,
				new BytecodeParser(),
				new JavaParser() {
					public CompilationUnit parse(java.io.InputStream is,
						String fileName) throws java.io.IOException,
							beaver.Parser.Exception {

						return new parser.JavaParser().parse(is, fileName);
					}
				});
		return result;
	}
	protected void processNoErrors(CompilationUnit unit) {
		unit.transformation();
		unit.generateClassfile();
	}

	protected ResourceBundle resources = null;
	protected String resourcename = "JastAddJ";
	protected String getString(String key) {
		if (resources == null) {
			try {
				resources = ResourceBundle.getBundle(resourcename);
			} catch (MissingResourceException e) {
				throw new Error("Could not open the resource " +
						resourcename);
			}
		}
		return resources.getString(key);
	}

	protected String name() { return getString("jastaddj.JavaCompiler"); }
	protected String version() { return getString("jastaddj.Version"); }
}
