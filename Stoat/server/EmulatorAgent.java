import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * An emulator agent is a snapshot instance of the corresponding REMOTE emulator agent. 
 * They talks through sockets.
 * @author tingsu
 *
 */
public class EmulatorAgent implements Runnable{
	
	private static String DEBUG_STRING;
	
	private static final String emName = "emulator_name";
	private static final String emState = "emulator_state";
	
	/**
	 * the emulator name
	 */
	public String emulatorName;
	
	/** the emulator states */
	public enum State{
		offline,  /* the emulator is offline (it may be booting) */
		idle,  /* the emulator is idle (it is waiting for new test sequences) */
		busy,  /* the emulator is busy (it is busying with executing test sequences) */
		exception /* the emulator crashes or non-responding (it needs to be restarted) */
	}
	/**
	 * the emulator state
	 */
	public String emulatorState;
	
	/** the controller which this emulator agent is associated with */
	private EmulatorController controller;
	/** the communication socket */
	private Socket socket;
	/** the thread which this emulator agent runs in */
	private Thread agentThread;
	private String agentThreadName;
	
	/** the event sequence which will be executed */
	private List<String> eventSequence;
	
	/**
	 * constructor
	 * @param controller the associated emulator controller
	 * @param remoteAgentSocket the communication socket 
	 */
	public EmulatorAgent(EmulatorController controller, Socket remoteAgentSocket, String agentName){
		this.controller = controller;
		this.socket = remoteAgentSocket;
		this.agentThreadName = agentName;
		DEBUG_STRING = "[" + agentName + "] ";
		this.eventSequence = new ArrayList<String>();
	}
	
	public void start(){
		System.out.println(DEBUG_STRING + "I: the agent " + agentThreadName + " is started. ");
		if(agentThread == null){
			agentThread = new Thread(this, agentThreadName);
		}
		//start a new thread
		agentThread.start();
	}
	
	public String getState(){
		return this.emulatorState;
	}
	
	public synchronized void getTestSuite(List<String> testSuite) throws InterruptedException{
		
		if(eventSequence.size()!=0){
			//if the tests have not been finished, wait here
			wait();
		}
		eventSequence.addAll(testSuite);
		System.out.println(DEBUG_STRING + "I: the agent has received the test cases, and notfiy itself to continue ...");
		//notify itself to execute test cases
		notify();
	}
	
	private synchronized void getTestCases(PrintWriter out) throws InterruptedException{
		System.out.println(DEBUG_STRING + "I: the emulator name: " + emulatorName + ", the emulator state: " + emulatorState);
		switch(State.valueOf(emulatorState)){
		case offline:
			break;
		case idle:
			//notify the controller to put test cases
			notify();
			if(eventSequence.size() == 0){
				System.out.println(DEBUG_STRING + "I: the agent notifies the controller to give test cases ... ");
				
				System.out.println(DEBUG_STRING + "I: wait here ... ");
				//when there are no test cases, wait here
				wait();
				System.out.println(DEBUG_STRING + "I: get notified, start to run ... ");
			}
			//remember to add "\n" at the end of the message
			for(String event: eventSequence){
				//send test cases
				out.println(event);
				System.out.println(event);
			}
			
			System.out.println(DEBUG_STRING + "I: test cases have been sent to the remote emulator...");
            out.println("TESTCASES_END");
            System.out.println(DEBUG_STRING + "I: testcases sent!");
            
            //clear the test cases
            eventSequence.clear();
           
            
			break;
			
		case busy:
			
			break;
		case exception:
			break;
		default:
			break;
		}
	}

	/**
	 * the main working loop
	 * 1. receive and update the remote agent state (if busy, wait; if idle, notify)
	 * 2. if idle, send the test cases to the agent 
	 */
	public void run() {
		
		System.out.println(DEBUG_STRING + "I: the agent " + agentThreadName + " is running. ");
		
		InputStream input = null;
        BufferedReader br = null;
        PrintWriter out = null;
        
        try {
            input = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(input));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //the message from the remote actual agent
        String message;
        
        while (true) {
            try {
            	
            	//TODO, we currently use a simple implementation: 
            	// 1. the remote agent send its name to us, 2. we send the test case to the agent 
            	// 3. after the agent finish the execution, it returns a result
            	// incremental
            	
                while( (message = br.readLine()) != null){
                	
                	System.out.println(DEBUG_STRING + "I: the message received from the remote agent: " + message);
                	//handle the message
                	handleAgentMessage(message);
                	
                	if(message.equals("FETCH_TEST")){
                		
                    	System.out.println(DEBUG_STRING + "I: the server is ready to send the test cases. ");
                    	getTestCases(out);
                		
                		break;
                	}else if(message.equals("FINISH_TEST")){
                		//TODO just for test notify the controller the code coverage is ready
                        controller.sendTestCoverage();
                	}
                	
                }
                	
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
	}
	
	/**
	 * handle the message from the remote agent
	 * message type:
	 * 	 emulator_name = xx ; emulator_state = xx;  
	 * @param message
	 */
	public void handleAgentMessage(String message){
		System.out.println(DEBUG_STRING + "I: handle the message from the remote agent. ");
		if(message!=null && message.contains("=")){
			int separatorLoc = message.indexOf('=');
			String messageName = message.substring(0, separatorLoc).trim();
			String messageValue = message.substring(separatorLoc+1).trim();
			System.out.println(DEBUG_STRING + "I: message name: " + messageName);
			System.out.println(DEBUG_STRING + "I: message value: " + messageValue);
			if(messageName.equals(emName)){
				emulatorName = messageValue;
			}else if(messageName.equals(emState)){
				emulatorState = messageValue;
			}else{
				System.out.println(DEBUG_STRING + "E: undefinded message type! ");
				System.exit(0);
			}
		}
	}

	
}