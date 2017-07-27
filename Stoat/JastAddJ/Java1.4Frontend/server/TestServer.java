package server;

import AST.*;

import java.io.*;
import java.net.*;
import java.util.*;

class TestServer {

  private static ArrayList readFileNames(BufferedReader in) throws IOException {
    ArrayList list = new ArrayList();
    String msg = in.readLine();
    while(!msg.equals("end")) {
      if(msg.length() > 5) {
        msg = msg.substring(0, msg.length() - 5); // strip .java
        msg = msg.replace(File.separatorChar, '.');
        list.add(msg);
      }
      msg = in.readLine();
    }
    return list;
  }

  private static boolean hasOldCompilationUnit(ArrayList list, Program program, CompilationUnit[] cus) {
    for(int i = 0; i < cus.length; i++) {
      if(cus[i] != null) {
        for(int j = 0; j < cus[i].getNumTypeDecl(); j++)
          if(hasDuplicateTypeDeclaration(program, cus[i].getTypeDecl(j).fullName(), cus[i]))
            return true;
      }
    }
    return false;
  }

  private static boolean hasDuplicateTypeDeclaration(Program program, String typeName, CompilationUnit cu) {
    for(int i = 0; i < program.getNumCompilationUnit(); i++)
      for(int j = 0; j < program.getCompilationUnit(i).getNumTypeDecl(); j++)
        if(program.getCompilationUnit(i).getTypeDecl(j).fullName().equals(typeName) && program.getCompilationUnit(i) != cu)
          return true;
    return false;
  }
  private static CompilationUnit[] loadCompilationUnits(ArrayList list, Program program) {
    CompilationUnit[] cus = new CompilationUnit[list.size()];
    for(int i = 0; i < list.size(); i++) {
       String msg = (String)list.get(i);
       System.out.println("Loading: " + msg);
       CompilationUnit cu = program.getCompilationUnit(msg);
       cus[i] = cu;
       if(cu != null)
         program.addCompilationUnit(cu);
       else {
         throw new Error("Could not load " + msg);
       }
     }
    return cus;
  }

  /*
   * problems:
   * command line classpath specifier (support this)
   * exisiting classes in the same package (check for not only compilation unit but included classes)
   * 
   * 
   */
  
  public static void main(String args[]) {
    Program program = new Program();
    ServerSocket server = null;
    try {
      server = new ServerSocket(12345);
    } catch(IOException e) {
      System.err.println("Could not listen to port 12345");
      System.exit(1);
    }

    System.out.println("Server active");
    while(true) {
      Socket socket = null;
      try {
        socket = server.accept();
      } catch (IOException e) {
        System.err.println("Accept failed");
        System.exit(1);
      }

      try {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String msg = in.readLine();
        if(msg.equals("shutdown")) {
          System.out.println("Exiting");
          System.exit(0);
        }
        else if(msg.equals("reset")) {
          program = new Program();
          System.out.println("Reset");
        }
        else {
          String classPath = msg;
          ArrayList list = readFileNames(in);
          boolean error = false;
          String errorMessage = "";
          try {
            program.simpleReset();
            program.initPaths();
            program.pushClassPath(classPath);

            CompilationUnit[] cus = loadCompilationUnits(list, program);
            Collection collection = new LinkedList();
            for(int i = 0; i < list.size(); i++) {
              CompilationUnit cu = cus[i];
              if(cu != null)
                cu.errorCheck(collection);
            }
            System.out.println("Errors:");
            for(Iterator iter = collection.iterator(); iter.hasNext(); ) {
              String s = (String)iter.next();
              System.out.println(s);
              if(!s.equals(""))
                error = true;
            }
          } catch (Throwable e) {
            errorMessage = msg + ".java:" + e.toString().substring(e.toString().indexOf(':')+2);
            System.err.println(msg + ":" + e.toString().substring(e.toString().indexOf(':')+2));
            e.printStackTrace();
            error = true;
          }
          out.println(error ? "error:" + errorMessage : "ok");
          out.flush();
          System.out.println("Done");
          program.popClassPath();
        }
        out.close();
        in.close();
      } catch(IOException e) {
        System.err.println("IOException");
        System.exit(1);
      }
   }
    
      
    
    
    //long code = System.currentTimeMillis() - start - program.parseTime;
    //program.codeGen();
    //
    //program.printTypes();
    //System.err.println("Parse: " + program.parseTime);
    //System.err.println("Analysis + print: " + code);
  }

  
}
