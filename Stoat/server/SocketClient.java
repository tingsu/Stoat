import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.JOptionPane;

/* the socket client class */
public class SocketClient{
	
	 public static void main(String[] args) throws IOException {
	        String serverAddress = JOptionPane.showInputDialog(
	            "Enter IP Address of a machine that is\n" +
	            "running the date service on port 9090:");
	        try{
		        Socket s = new Socket(serverAddress, 9090);
		        System.out.println("the client start to read the date from the server...");
		        BufferedReader input =
		            new BufferedReader(new InputStreamReader(s.getInputStream()));
		        String answer = input.readLine();
		        JOptionPane.showMessageDialog(null, answer);
		        System.exit(0);
		        s.close();
	        }catch(IOException e){
	        	System.out.println(e);
	        }
	    }
	 
}