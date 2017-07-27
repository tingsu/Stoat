/*
 * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
 * by the modified BSD License. You should have received a copy of the
 * modified BSD license with this compiler.
 * 
 * Copyright (c) 2011, Jesper Öqvist <jesper.oqvist@cs.lth.se>
 * All rights reserved.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import AST.*;

/**
 * Apache Ant Task to diff a set of .java and .txt files.
 */
public class DiffTask extends Task {
	private Path	srcdir;
	private Path	outdir;
	private static Pattern srcPattern = Pattern.compile(".+\\.java");

	public void execute() {
		if (srcdir == null || outdir == null) {
			System.err.println("srcdir and outdir attributes "+
					"are required for diff task!");
			return;
		}

		File fsrcdir = new File(srcdir.toString());
		File foutdir = new File(outdir.toString());

		if (!fsrcdir.exists()) {
			System.err.println("could not open directory "+srcdir);
			return;
		}
		if (!foutdir.exists()) {
			System.err.println("could not open directory "+outdir);
			return;
		}

		for (String name : fsrcdir.list()) {
			Matcher matcher = srcPattern.matcher(name);
			if (!matcher.matches()) continue;

			File file = new File(fsrcdir, name);
			if (!file.isDirectory()) {
				String name2 = name.substring(0,
						name.length()-4) + "txt";

				File file2 = new File(foutdir, name2);
				if (file2.exists() && !file2.isDirectory())
					diffFiles(file, file2);
				else
					System.err.println("could not find "+
							name2);
			}
		}
	}

	/**
 	 * Compare two files to see if the contents match. Leading and trailing
 	 * whitespace is ignored, as well as empty lines.
 	 */
	private void diffFiles(File a, File b) {
		try {
			BufferedReader ra =
				new BufferedReader(new FileReader(a));
			BufferedReader rb =
				new BufferedReader(new FileReader(b));

			int nline = 0;
			String linea = null;
			String lineb = null;
			boolean matching = true;
			boolean enda = false;
			boolean endb = false;
			do {
				// get next non-empty line from file a
				do {
					linea = ra.readLine();
					if (linea == null) {
						linea = "";
						enda = true;
						break;
					}
					linea = linea.trim();
				} while (linea.isEmpty());

				// get next non-empty line from file b
				do {
					lineb = rb.readLine();
					if (lineb == null) {
						lineb = "";
						endb = true;
						break;
					}
					lineb = lineb.trim();
				} while (lineb.isEmpty());

				if (!linea.equals(lineb)) {
					if (matching) {
						matching = false;
						System.err.println(
							"files "+a.getName()+
							" and "+b.getName()+
							" do not match!");
					}

					System.err.println(""+nline+": "+linea);
					System.err.println(""+nline+": "+lineb);
				}

				++nline;
			} while (!enda || !endb);
			if (matching)
				System.err.println("files "+a.getName()+" and "+
						b.getName()+" match!");
		} catch (IOException e) {
			System.err.println("read error while comparing files " +
					a.getName() + " and " + b.getName());
		}
	}

	public void setSrcdir(Path srcdir) {
		this.srcdir = srcdir;
	}

	public void setOutdir(Path outdir) {
		this.outdir = outdir;
	}
}
