import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/* a java socket server */
public class SocketServer{
	
	private final static String DEBUG_STRING = "[SocketServer] ";
	
	/**
     * Runs the server.
     * the communication protocol
     *    server (java)       ----        client (a3e)
     *    |		    <-	M1					|
     *    |			<-	M2					|
     *    |			server: T1				|
     *    |			server: T2				|
     *    |			server: T3				|
     *    |			M3	->					|
     *    |			client:	T3				|
     *    |			client:	T4				|
     *    M1: send "READY" message when the avd is started
     *    M2: send the current app state
     *    M3: send the app state activity name
     *    M4: send the executed action (option)
     *    T1: store the app state to a file
     * 	  T2: parse the app state into view components (after received the state activity name)
     * 	  T3: build the app FSM (add the received app state and executed action into FSM)
     *    T4: detect invokable actions from the app state
     *    M5: send the invokable actions back to client
     *    T5: write the invokable actions to the command file
     *    T6: pick the action to execute (depend on search strategies)
     *    
     *    the message protocol:
     *    "AVD_STATE_EOM", "APP_STATE_EOM", "ACTIVITY_EOM", "ACTION_EOM", "FINISH_EOM"
     */
    public void acceptAppStates(int port) throws IOException {
    	
    	System.out.println(DEBUG_STRING + "I: the server is started ...waiting at port " + port);
    	
    	//open a server socket
        ServerSocket listener = new ServerSocket(port);
        try {
            while (true) {
            	//listen for and accept connections from clients
                Socket serverSocket = listener.accept();
                System.out.println(DEBUG_STRING + "I: a client connected to this server. ");
                try {
                	BufferedReader input =
        		            new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                	PrintWriter out =
                            new PrintWriter(serverSocket.getOutputStream(), true);
                	
                	System.out.println(DEBUG_STRING + "I: received an app state from [a3e]: ");
                	String line;
                	StringBuffer clientMessage = new StringBuffer("");
                	
                	//the loop to accept client messages
                	while(true){
                		
                		//clear the client message
                		clientMessage.setLength(0);
                		
	                	while((line = input.readLine()) != null){
	                		if( line.contains("EOM"))	/* the end of message */
		                		break;
	                		
	                		//output the line
	                		//System.out.println(line);
	                		
	                		//append a newline character "\n"
	                		clientMessage.append(line+"\n");  
	                	}
	                	
	                	if(line.equals("AVD_STATE_EOM")){
	                		/* avd message */
	                		if(clientMessage.toString().contains("READY")){
	                			System.out.println(DEBUG_STRING + "I: the android emulator is ready now. ");
	                			eventCounter = 0;
	                		}else if(clientMessage.toString().contains("STOP")){
	                			System.out.println(DEBUG_STRING + "I: close the server.");
	                			//clean up
	                        	serverSocket.close();
	                        	listener.close();
	                        	System.exit(0);
	                		}
                		}else if(line.equals("ENTRY_ACTIVITY_EOM")){
                			
                			ConfigOptions.ENTRY_ACTIVITY_NAME = clientMessage.toString().replace("\n", ""); //remove the tailing newline
                			System.out.println(DEBUG_STRING + "I: get the entry activity: " + ConfigOptions.ENTRY_ACTIVITY_NAME);
                			break;
                			
                		}else if(line.equals("UI_FILE_NAME_EOM")){
                			
                			/* step1: write the app state to a file */
        	                System.out.println(DEBUG_STRING + "I: get the current UI file name");
        	                currentUIFileName = clientMessage.toString().replace("\n", "");
        	                // count the events
        	                eventCounter ++;
        	                
                		}else if(line.equals("PACKAGE_NAME_EOM")){
                			
                			System.out.println(DEBUG_STRING + "I: get the current package name ...");
                			currentPackageName = clientMessage.toString().replace("\n", "");
        	                
                		}else if(line.equals("ACTIVITY_NAME_EOM")){
                			
                			currentActivityName = clientMessage.toString().replace("\n", "");
                			
                			/* step2: parse the ui file into view components */
        	                System.out.println(DEBUG_STRING + "I: parse the received UI file to view components ...");
        	                AndroidAppFSM.getFSMBuilder().parseAppState(currentUIFileName, currentPackageName, currentActivityName);
                			
                		}else if(line.equals("ACTION_EOM")){
                			
                			String actionID = clientMessage.toString().replace("\n", "");
                			
                			if(! actionID.equals("")){
                				System.out.println(DEBUG_STRING + "I: an action is executed in [a3e], action id = " + actionID );
                				/* step 3: build the FSM according to the action and the received app state */
                				currentExecutedActionId = Integer.parseInt(actionID.trim());
                				AndroidAppFSM.getFSMBuilder().buildAppFSM(currentUIFileName, currentExecutedActionId);
                			}else{
                				System.out.println(DEBUG_STRING + "I: no action is executed in [a3e].");
                			}
                		
                		}else if(line.equals("FINISH_EOM")){
                			
                			/* step4: detect invokable actions from the received app state */
        	                System.out.println(DEBUG_STRING + "I: detect invokable actions on the received app state: " + currentUIFileName);
        	        		String invokableActions = AndroidAppFSM.getFSMBuilder().detectInvokableActions(currentUIFileName);
                            
                            /* step5: send out the invokable actions */
                            out.print(invokableActions);
                            
                            break;
                		}
	                	
                	}
                	
                	//dump all the computation results
                	System.out.println("-----------");
                    AndroidAppFSM.getFSMBuilder().dumpAllAppStates(ConfigOptions.APP_FSM_BUILDING_OUTPUT_DIR + "/allstates.txt");
                    AndroidAppFSM.getFSMBuilder().outputFSMProperty(ConfigOptions.APP_FSM_BUILDING_OUTPUT_DIR + "/fsm_states_edges.txt");
                    AndroidAppFSM.getFSMBuilder().exportAppFSMDotFile(ConfigOptions.APP_FSM_BUILDING_OUTPUT_DIR + "/app.gv");
                    AndroidAppFSM.getFSMBuilder().dumpAppFSM(ConfigOptions.APP_FSM_BUILDING_OUTPUT_DIR + "/FSM.txt");
                    
                	//Send "Goodbye" to close the connection with the [a3e] client
                    out.println("server: Goodbye!");
                    System.out.println(DEBUG_STRING + "I: the server sends *Goodbye* to the [a3e] client, " +
                    		"and close the connection with the [a3e] client. ");
                    System.out.println(DEBUG_STRING + "I: the current executed events count: " + eventCounter + " maximum allowed events: " + ConfigOptions.MAX_FSM_BUILDING_EVENTS);
                    System.out.println("-----------");
                    System.out.println("\n\n");
                    out.close();
                    
                } finally {
                	serverSocket.close();
                }
            }
        }
        finally {
            listener.close();
        }
    }
    
    /**
     *  the event counter 
     *  */
    private static int eventCounter = 1;
    
    private int currentExecutedActionId;
    
    private String currentPackageName;
    private String currentActivityName;
    private String currentUIFileName;
    
    
    public static void buildFSM(String[] args){
    	//run android app static analysis to detect actions
  		System.out.println("I: start android app static analysis to detect actions.... ");
		//TODO we disabled the static analysis now
		//AndroidAppAnalysis analysis = new AndroidAppAnalysis();
		//analysis.run(args);
		
		//store all actions
		ActionHandler.getHandler().storeActionsFromStaticAnalysis();
		System.out.println("I: finish static analysis, there are total [" + ActionHandler.siaListLength + "] statically inferred actions.");
		
		//dump all actions
		ActionHandler.getHandler().outputAllSIA();
		
		//start the server to communicate with the client
  		SocketServer server = new SocketServer();
  		
  		int port = ConfigOptions.PORT;
  		System.out.println("port = " + port);
  		
  		try{
  			//server.acceptAppStates(port);
  			server.acceptAppStates(port);
  		}catch(IOException e){
  			
  			System.out.println("Get app states exception!");
  			e.printStackTrace();
  		}

    }
    
    //the main entry of MCMC-droid
  	public static void main(String[] args){
  		
  		// app dir -- args[0]
  		ConfigOptions.setupFSMBuildingOutputDir(args[0]);
  		
  		//read the configuration file
  		ConfigOptions.readConfigOptions(ConfigOptions.FSM_BUILDING_CONFIG_FILE);
  		
  		//remove the first element
  		String[] params = Arrays.copyOfRange(args, 1, args.length);
  		
  		// build the app FSM
  		buildFSM(params);
  	}
    
}
