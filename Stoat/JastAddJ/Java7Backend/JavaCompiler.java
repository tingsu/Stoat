/*
 * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
 * by the modified BSD License. You should have received a copy of the
 * modified BSD license with this compiler.
 * 
 * Copyright (c) 2005-2008, Torbjorn Ekman
 *               2011, Jesper Öqvist <jesper.oqvist@cs.lth.se>
 * All rights reserved.
 */

import AST.*;
import java.util.*;

/**
 * @deprecated As of 2012-01-18. Use org.jastadd.jastaddj.JavaCompiler instead.
 */
@Deprecated
class JavaCompiler extends Frontend {
	public static void main(String args[]) {
		if(!compile(args))
			System.exit(1);
	}

	public static boolean compile(String args[]) {
		return org.jastadd.jastaddj.JavaCompiler.compile(args);
	}
}
