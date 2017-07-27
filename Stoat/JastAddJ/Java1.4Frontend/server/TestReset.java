package server;

import java.util.*;
import java.net.*;
import java.io.*;

class TestReset {
    public static void main(String[] args) {
      Socket socket = null;
      PrintWriter out = null;
      BufferedReader in = null;

      try {
        socket = new Socket("localhost", 12345);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.println("reset");
  	    out.close();
	      in.close();
	      socket.close();
        
      } catch (UnknownHostException e) {
          System.err.println("Don't know about host: localhost");
      } catch (IOException e) {
          System.err.println("Couldn't get I/O for "
                             + "the connection to: localhost");
      }
      System.exit(0);
    }
}
