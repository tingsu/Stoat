import AST.*;
import java.util.*;
import java.io.*;

class JavaChecker extends Frontend {

  public static void main(String args[]) {  
    //compile(args);
	  //add
	  try {
		  Vector<String> files = readfiles(args[0]);
		  files.add("-classpath");
		  // JFreeChart
		  files.add("..\\JFreeChart-1.0.10\\lib\\servlet.jar;..\\JFreeChart-1.0.10\\lib\\jcommon-1.0.13.jar;..\\JFreeChart-1.0.10\\lib\\jfreechart-1.0.10.jar");
		  
		  // Jigsaw
		  //files.add("..\\Jigsaw\\oro-2.0.6.jar;..\\Jigsaw\\tools.jar;..\\Jigsaw\\classes\\servlet.jar;..\\Jigsaw\\classes\\xerces.jar;..\\Jigsaw\\classes\\Tidy.jar");
		  
		  // JHotDraw
		  //files.add("..\\JHotDraw6.0b1\\junit.jar;..\\JHotDraw6.0b1\\jdo2-api-2.0.jar;..\\JHotDraw6.0b1\\batik-1.1.1.jar");
		  
		  // ecj
		  //files.add("..\\ant.jar");
		  
		  // FreeCol
		  //files.add("..\\FreeCol-0.4.0\\higlayout.jar");
		  
		  // Jmol
		  //files.add("..\\Jmol-10.2.0\\Jmol.jar;..\\Jmol-10.2.0\\jars\\netscape.jar");
		  
		  
		  final String[] paths = new String[files.size()];
		  files.toArray(paths);
		  Thread t = new Thread() {
			  public void run() {
				  long t1 = System.currentTimeMillis();   
				  compile(paths);
				  long t2 = System.currentTimeMillis();
				  System.out.println("Total time: "+(t2-t1));
				  
				  // add
				  System.err.println("DeadCodeNum: "+ deadCodeNum);
			  }
		  };
		  t.start();
	  }
	  catch (FileNotFoundException ex) {}			
	  catch (IOException ex) {}
	 	  
  }

  public static boolean compile(String args[]) {
    JavaChecker checker = new JavaChecker();
    boolean result = checker.process(
        args,
        new BytecodeParser(),
        new JavaParser() {
          public CompilationUnit parse(InputStream is, String fileName) throws IOException, beaver.Parser.Exception {
            return new parser.JavaParser().parse(is, fileName);
          }
        }
    );
    return result;
  }
  protected void initOptions() {
    super.initOptions();
    program.options().addKeyOption("-dot");
  }

  //add
  public static int deadCodeNum = 0;
  
  protected void processNoErrors(CompilationUnit unit) {
	  
    //System.out.println(unit);
    //System.out.println(unit.dumpTree());
	//unit.dumpTree();
    
    //DeadCode 
    for(Iterator it = unit.deadCode().iterator();it.hasNext();) {
    	CFGNode node = (CFGNode)it.next();
    	if(node instanceof Expr)
    		System.err.println(node+" in "+((Expr)node).enclosingStmt());
    	else
    		System.err.println(node);
    }
    // System.err.println("DeadCode Num: "+unit.deadCode().size());
    
	deadCodeNum += unit.deadCode().size();
	  
	/*  //CF
	    for(Iterator it = unit.constUselessCode().iterator();it.hasNext();) {
	    	CFGNode node = (CFGNode)it.next();
	    	System.err.println(node);
	    } 
	*/  
    /*if(program.options().hasOption("-dot"))
      unit.emitDotDescription();*/
  }
  
  
	public static Vector<String> readfiles(String filepath) throws FileNotFoundException, IOException {
		Vector<String> files = new Vector<String>();
		try {			
			File file = new File(filepath);
			if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File readfile = new File(filepath + "\\" + filelist[i]);
					if (!readfile.isDirectory()) 
						files.addAll(readfiles(readfile.getPath()));         
					else 
						files.addAll(readfiles(filepath + "\\" + filelist[i]));
				}
			}
			else { 
				String suffix = "";
				if(filepath.contains("."))
					suffix = filepath.substring(filepath.lastIndexOf("."));					
                if(suffix.equals(".java")) 
					files.add(filepath);	
			}
		}
		catch (FileNotFoundException e) {
	    	System.out.println("readfile() Exception:" + e.getMessage());
		}
		
	    return files;
	}	


}
