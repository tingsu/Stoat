import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

public class CoverageReporter implements Runnable{
	
	private final static String DEBUG_STRING = "[CoverageReporter] ";
	
	public String reporterName = "CoverageReporter";
	private Thread reporterThread;
	private final static int SOCKET_PORT = 2010;
	
	/** the command string */
	private String command;
	public static final String COMPUTE_COVERAGE = "COMPUTE_COVERAGE";
	
	/** the coverage result */
	private static String coverageResult;
	
	private static CoverageReporter reporter;
	private CoverageReporter(){
		command = null; //set the command as null
		if(reporterThread == null){
			reporterThread = new Thread(this, reporterName);
			reporterThread.start();
		}
	}
	public static CoverageReporter v(){
		if(reporter == null){
			reporter = new CoverageReporter();
		}
		return reporter;
	}
	
	
	/** send command to the REMOTE agent controller, and get the coverage result 
	 * @throws InterruptedException */
	public synchronized String request(String requestCommand) throws InterruptedException{
		
		//set the result as empty
		coverageResult = null;
				
		command = requestCommand;
		//this "notify()" correspi
		notify();
		
		
		if(coverageResult == null){
			//wait the rely
			System.out.println(DEBUG_STRING + "I: wait the reply ... ");
			wait();
		}
		
		//clear the reporter
		reporter = null;
		
		System.out.println(DEBUG_STRING + "I: the result before reutrn: " + coverageResult);
		return coverageResult;
	}
	
	public synchronized void reply(String result) {
		 
		 coverageResult = result;
         //this "notify()" corresponds to the "wait()" in the function "request()"
		 System.out.println(DEBUG_STRING + "I: notified: the coverage report is got. ");
         notify();
         
	}
	
	
	public void run() {
		
        Socket clientSocket = null;
        
        InputStream input = null;
        BufferedReader br = null;
        PrintWriter out = null;
        
        String line = null;
        
        try{
        	clientSocket = new Socket("localhost", SOCKET_PORT);
	        System.out.println(DEBUG_STRING + "I: get connected with the remote controller. ");
	        
	        input = clientSocket.getInputStream();
            br = new BufferedReader(new InputStreamReader(input));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            if( command == null){
            	try {
            		System.out.println(DEBUG_STRING + "I: waits for the request command ... ");
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
            System.out.println(DEBUG_STRING + "I: send the request command:  " + command);
            //send the command
            out.println(command);
            //receive the result
            while((line = br.readLine()) != null){
            	System.out.println(DEBUG_STRING + "I: got the coverage result: " + line);
            	break;
            }
            //set the result!!!
            coverageResult = line;
            reply(coverageResult);
            
        }catch(IOException e){
        	e.printStackTrace();
        }finally{
        	try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
}