package server;

import java.util.*;
import java.net.*;
import java.io.*;

class TestShutdown {
    public static void main(String[] args) {
      boolean silent = args.length == 1 && args[0].equals("silent");
      Socket socket = null;
      PrintWriter out = null;
      BufferedReader in = null;

      try {
        socket = new Socket("localhost", 12345);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        out.println("shutdown");
  	    out.close();
	      in.close();
	      socket.close();
        
      } catch (UnknownHostException e) {
          if(!silent)
              System.err.println("Don't know about host: localhost");
      } catch (IOException e) {
          if(!silent)
              System.err.println("Couldn't get I/O for "
                             + "the connection to: localhost");
      }
      if(!silent)
          System.out.println("Exiting");
      System.exit(0);
    }
}
