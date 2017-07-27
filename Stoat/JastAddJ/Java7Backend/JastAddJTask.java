/*
 * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
 * by the modified BSD License. You should have received a copy of the
 * modified BSD license with this compiler.
 * 
 * Copyright (c) 2005-2008, Torbjorn Ekman
 *                    2011, Jesper Öqvist <jesper.oqvist@cs.lth.se>
 * All rights reserved.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import AST.*;

public class JastAddJTask extends Task {
	private Path	cp;
	private Path	srcdir;
	private Path	destdir;

	public void execute() {
		ArrayList<String> args = new ArrayList<String>();
		if (cp != null) {
			args.add("-cp");
			args.add(cp.toString());
		}
		if (srcdir != null) {
			//args.add("-sourcepath");
			File fsrcdir = new File(srcdir.toString());
			if (!fsrcdir.exists()) {
				System.err.println("could not open directory "+
						srcdir.toString());
			} else {
				addSrcFiles(args, fsrcdir);
			}
		}
		if (destdir != null) {
			args.add("-d");
			args.add(destdir.toString());
		}
		String[] argv = new String[args.size()];
		for (int i = 0; i < args.size(); ++i)
			argv[i] = args.get(i);
		JavaCompiler.main(argv);
	}
	
	private static Pattern srcPattern = Pattern.compile(".+\\.java");
	private void addSrcFiles(ArrayList<String> args, File srcdir) {
		for (String child : srcdir.list()) {
			File childFile = new File(srcdir, child);
			if (childFile.isDirectory()) {
				addSrcFiles(args, childFile);
			} else {
				Matcher matcher = srcPattern.matcher(child);
				if (matcher.matches()) {
					args.add(childFile.toString());
				}
			}
		}
	}

	public void addClasspath(Path cp) {
		this.cp = cp;
	}

	public void setClasspath(Path cp) {
		this.cp = cp;
	}

	public void setClasspathref(Reference ref) {
		Object deref = ref.getReferencedObject();
		if (deref instanceof Path)
			this.cp = (Path) deref;
	}

	public void setSrcdir(Path srcdir) {
		this.srcdir = srcdir;
	}

	public void setDestdir(Path destdir) {
		this.destdir = destdir;
	}

}
