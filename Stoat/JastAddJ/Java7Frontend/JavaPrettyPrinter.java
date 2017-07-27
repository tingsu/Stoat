/*
 * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
 * by the modified BSD License. You should have received a copy of the
 * modified BSD license with this compiler.
 * 
 * Copyright (c) 2005-2008, Torbjorn Ekman
 *		      2011, Jesper Öqvist <jesper.oqvist@cs.lth.se>
 * All rights reserved.
 */

import AST.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java pretty printer.
 * Parses and then pretty prints a Java program.
 */
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
					public CompilationUnit parse(java.io.InputStream is, String fileName)
						throws java.io.IOException, beaver.Parser.Exception {
							return new parser.JavaParser().parse(is, fileName);
					}
				}
				);
	}
	protected void processErrors(java.util.Collection errors,
			CompilationUnit unit) {
		super.processErrors(errors, unit);
	}

	protected void processNoErrors(CompilationUnit unit) {
		String separator = System.getProperty("file.separator");
		if (separator.equals("\\"))
			// regex escape
			separator = "\\\\";
		String fnIn = unit.pathName();
		Pattern srcPattern = Pattern.compile(
				"^(.*"+separator+
				")?([^"+separator+
				"]+)\\.java$");
		Matcher matcher = srcPattern.matcher(fnIn);
		if (!matcher.find())
			throw new Error("Coult not determine output filename "+
					"for source file "+fnIn);
		String fnOut = "";
		if (program.options().hasOption("-d"))
			fnOut = program.options().getValueForOption("-d") +
				System.getProperty("file.separator");
		fnOut += matcher.group(2)+".txt";

		try {
			FileOutputStream fout = new FileOutputStream(
					new File(fnOut));
			PrintStream out = new PrintStream(fout);
			out.println(unit.toString());
			fout.close();
		} catch (IOException e) {
			System.err.println("Could not write output to "+fnOut);
		}
	}

	protected ResourceBundle resources = null;
	protected String resourcename = "JastAddJ";
	protected String getString(String key) {
		if (resources == null) {
			try {
				resources = ResourceBundle.getBundle(
						resourcename);
			} catch (MissingResourceException e) {
				throw new Error("Could not open the resource " +
						resourcename);
			}
		}
		return resources.getString(key);
	}

	protected String name() { return getString("jastaddj.PrettyPrinter"); }
	protected String version() { return getString("jastaddj.Version"); }
}
