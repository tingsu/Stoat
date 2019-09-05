import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* the finite state machine for an android app */

public class AndroidAppFSM{
	
	/** the debug flag */
	public final static String DEBUG_STRING = "[AndroidAppFSM] ";
	
	
	/** all FSM states (the app state file name, the app state), stores all visited states, including duplicate ones */
	private Map<String, AppState> FSMStates;
	
	/** all app states in the execution time order, stores all visited states, including duplicate ones */
	private List<String> FSMAllExecutedStates;
	/** the number of executed app states */
	public int allStatesCount;
	
	/** the map: (MD5 hash value, the app state), it stores all unique app states */
	private Map<String, AppState> FSMStatesHashMap;
	
	/** the current app state name */
	public String currentStateName;
	
	/** the number of unique states in the app FSM */
	public int uniqueStatesCount;
	/** the number of unique transition in the app FSM */
	public int uniqueTransitionsCount;
	
	/** get the unique states count of an FSM */
	public int getUniqueStatesCount(){
		return uniqueStatesCount;
	}
	/** get the unique transitions count of an FSM */
	public int getUniqueTransitionsCount(){
		return uniqueTransitionsCount;
	}
	
	
	//add a new app state into the FSM
	public void addState(AppState appState){
		String stateName = appState.getStateName();
		//add the state into FSM
		FSMStates.put(stateName, appState);
        //add the state name into the executed states
        FSMAllExecutedStates.add(stateName);
        //update the state counts
        allStatesCount = FSMAllExecutedStates.size();
        //set the current state
        currentStateName = stateName;
        // update app state alias
        updateAppStateAlias(appState);
	}
	
	//add a new app state into the FSM but do not need to update the app state aliases
	public void addState2(AppState appState){
		String stateName = appState.getStateName();
		//add the state into FSM
		FSMStates.put(stateName, appState);
        //add the state name into the executed states
        FSMAllExecutedStates.add(stateName);
        //update the state counts
        allStatesCount = FSMAllExecutedStates.size();
        //set the current state
        currentStateName = stateName;
	}
	
	/** add a new transition into the FSM */
	public void addTransition(AppState state, Transition trans){
		//add transition to the state itself
		state.addTransition(trans);
	}

	
	/** get all states from the app FSM */
	public Map<String, AppState> getStates(){
		return this.FSMStates;
	}
	
	/**
	 *  get the initial app state in the FSM.
	 *  TODO the current default initial state is the first executed state, but in an android app,
	 *  it could have more than one entries.
	 *  */
	public String getInitialStateName(){
//		String initialStateName = FSMAllExecutedStates.get(0);
//		AppState initialState =  FSMStates.get(initialStateName);
//		return initialState.getSimpleStateName();
		//return ConfigOptions.FSM_FILE_LOCATION + "/ui/S_1.xml";
		return ConfigOptions.FSM_FILE_LOCATION + "/ui/S_1.xml";
	}
	
	/*************************************************************/
	
	public static List<String> listImages ;
    public static List<String> listEditBoxes ;
    public static List<String> listTextViewString ;
    public static List<String> listButtonString ;
    public static List<String> listTexts ;
	
	/* the app FSM builder */
	private static AndroidAppFSM FSMBuilder = null;
	private AndroidAppFSM(){
		
		//init. fields
		FSMStates = new HashMap<String, AppState>();
		FSMAllExecutedStates = new ArrayList<String>();
		FSMStatesHashMap = new HashMap<String, AppState>();  
		currentStateName = null;
		
		//init. view list
		listImages = new ArrayList<String>();
    	listEditBoxes = new ArrayList<String>();
    	listTextViewString = new ArrayList<String>();
    	listButtonString = new ArrayList<String>();
    	listTexts = new ArrayList<String>();
	}
	
	public static AndroidAppFSM getFSMBuilder(){
		if(FSMBuilder == null){
			FSMBuilder = new AndroidAppFSM();
		}
		return FSMBuilder;
	}
	
	
	/*************************************************************/
	
	/** constructor used in the MCMC sampling stage */
	public AndroidAppFSM(String phase){
		System.out.println(DEBUG_STRING + "I: create the app FSM for MCMC sampling.");
		//init. fields
		FSMStates = new HashMap<String, AppState>();
		FSMActionIdsSet = new HashSet<Integer>();
		FSMAllExecutedStates = new ArrayList<String>();
	}
	
	/** the set stores all action ids in the fsm */
	private Set<Integer> FSMActionIdsSet = null;
	
	public Set<Integer> getFSMActionIdsSet(){
		return FSMActionIdsSet;
	}
	
	/*************************************************************/
	
	
	/**
	 *  check and update the alias of the target state.
	 *  @param the target state
	 *  */
	public void updateAppStateAlias(AppState state){
		System.out.println(DEBUG_STRING + "I: checking *alias* for the app state: " + state.getSimpleStateName());
		state.computeMD5_new();
		String md5 = state.getMD5Value();
		System.out.println(DEBUG_STRING + "its md5 value = " + md5);
		if(FSMStatesHashMap.containsKey(md5)){
			//this app state has been visited previously
			AppState aliasState = FSMStatesHashMap.get(md5);
			state.setAliasStateName(aliasState.getStateName());
			System.out.println(DEBUG_STRING + "I: the app state: " + state.getSimpleStateName() + " is alias to this previous state: " 
					+ aliasState.getSimpleStateName());
		}else{
			System.out.println(DEBUG_STRING + "I: this is a new app state");
			//add this new app state into the hashmap
			FSMStatesHashMap.put(md5, state);
		}
	}

	
	/**
	 * get the last executed app state (alias states are skipped)
	 * @return the last app state
	 */
	public AppState getLastState(){
		//get the last state index
		int lastStateIndex = allStatesCount-2;
		String lastStateName = getStateNameByIndex(lastStateIndex);
		AppState lastState = getStateByName(lastStateName);
		//if the last state is a duplicate state, we need to find out its alias state
		if(!lastState.isUniqueState()){
			String aliasStateName = lastState.getAliasStateName();
			lastState = getStateByName(aliasStateName);
		}
		return lastState;
	}
	
	/**
	 * get the last executed app state name
	 * @return the last app state name
	 */
	public String getLastStateName(){
		//get the last state
		AppState lastState = getLastState();
		System.out.println(DEBUG_STRING + "I: the last state is: " + lastState.getStateName());
		return lastState.getStateName();
	}
	
	
	/**
	 *  get the current executed app state name
	 *  @return the current app state name
	 *  */
	public String getCurrentStateName(){
		return currentStateName;
	}
	
	public List<String> getAllExecutedStates(){
		return FSMAllExecutedStates;
	}
	
	/**
	 *  get the current executed app state
	 *  @return the current app state
	 *  */
	public AppState getCurrentState(){
		AppState currentState = FSMStates.get(currentStateName);
		assert(currentState != null);
		return currentState;
	}
	
	/**
	 * get the app state by its file name
	 * @param appStateName
	 * @return the app state
	 */
	public AppState getStateByName(String appStateName){
		return FSMStates.get(appStateName);
	}
	
	/**
	 * get the app state name by its index in the execution time order
	 * @param stateIndex the state index in the time order
	 * @return the app state name
	 */
	public String getStateNameByIndex(int stateIndex){
		//check the index is in the valid range 
		assert(stateIndex>=0 && stateIndex< FSMAllExecutedStates.size());
		return FSMAllExecutedStates.get(stateIndex);
	}
	
	/** 
	 * the basic algorithm to build an app FSM
	 * 1. S <- the current app state, check is it a new state (compare with all previous visited states)
	 * 2. if S is a new state (whether S has different view components with other states)
	 * 		create the new corresponding transition T
	 * 		add T into S's transition list
	 * 		add T into FSM's transition list (T must not be duplicate, because its end state is a new state)
	 * 3. if S is an old state
	 * 		find the earliest alias state S', check is T a new transition for S' ? (whether T has different start/end state or action id)
	 * 		4. if T is a new transition
	 * 			  add T into the transition list of S'
	 * 			  add T into FSM's transition list (T) (need check??)
	 * 		5. if T is an old transition
	 * 			  update T' (T's alias) 's executed times in S'
	 * 
	 * Precondition: the app state denoted by $appStateFileName should be already parsed.
	 * 
	 * @param appStateFileName the file name of the current app state
	 * @param actionID the id of the action, which is executed on the last app state and reaches the current state
	 */
	public void buildAppFSM(String appStateFileName, int actionID){
		
		//get the simple file name of the current app state
		String simpleAppStateFileName = AppState.getSimpleAppStateName(appStateFileName);
		
		System.out.println(DEBUG_STRING + "I: building the app FSM, the received app state name: "
				+ simpleAppStateFileName + ", the executed action id: " + actionID);
		
		//get the current state
		AppState currentState = getStateByName(appStateFileName);
		
		//get the last state
		AppState lastState = getLastState();
		if(lastState.isSpecialState() ){
			//Only RESET_APP_STATE and EMPTY_APP_STATE have non-null state tag
			if(lastState.getStateTag().equals(AppState.RESET_APP_STATE) && !currentState.isSpecialState()){
				System.out.println(DEBUG_STRING + "I: the last state is a *RESET* app state, the current state is the entry app state ");
				return;
			}else if(lastState.getStateTag().equals(AppState.EMPTY_APP_STATE) && currentState.isSpecialState()){
				System.out.println(DEBUG_STRING + "I: the last state is an *EMPTY* app state, the current state is a *RESET* app state");
				return;
			}
		}
		
		if( currentState.isUniqueState()){
			//a new app state, add this transition to this state
			System.out.println(DEBUG_STRING + "I: this received app state: " + simpleAppStateFileName +  " is a *new* state, the transition is a *new* transition. ");
			
			//create the new transition
			Transition transition = new Transition(getLastStateName(), getCurrentStateName(), actionID);
			
			addTransition(lastState, transition);
			
			System.out.println(DEBUG_STRING + "I: the new transition is : ");
			System.out.println(transition.toString());
			
		}else{
			//an old app state
			System.out.println(DEBUG_STRING + "I: this received app state: " + simpleAppStateFileName + " is an *old* app state.");
			
			//create the transition
			Transition transition = new Transition(getLastStateName(), currentState.getAliasStateName(), actionID);
			
			Transition aliasTransition = lastState.getAliasTransition(transition);
			
			if(aliasTransition == null){
				//a new transition
				System.out.println(DEBUG_STRING + "I: the transition is a *new* transition. ");
				System.out.println(DEBUG_STRING + "I: this is the new transition: ");
				System.out.println(transition.toString());
				
				addTransition(lastState, transition);
				
			}else{
				//an old transition
				System.out.println(DEBUG_STRING + "I: the transition is an *old* transition: ");
				lastState.updateTransition(aliasTransition);
			}
		}
	}
	
	
	/**
	 * generate a stochastic FSM model from the initial FSM
	 */
	public void initFSMTransitionProb(){
		
		//compute transition probabilities
		System.out.println(DEBUG_STRING + "I: computing initial transition probablities from the FSM model ...");
		System.out.println(DEBUG_STRING + "I: the FSM has total " + FSMAllExecutedStates.size() + " states. ");
		for(String stateName: FSMAllExecutedStates){
			AppState state = FSMStates.get(stateName);
			if(state.isUniqueState()){
				System.out.println(DEBUG_STRING + "I: unique state: " + state.getSimpleStateName());
				state.computeTransitionProb();
			}
		}
	}
	
	
	
	/**
	 * output the property of the FSM (including the number of unique states and unique transitions)
	 */
	public void outputFSMProperty(String fsm_building_progress_file){	
		System.out.println("I: compute the properties of the current FSM model... ");
		//init. as zero
		uniqueStatesCount = 0;
		uniqueTransitionsCount = 0;
		//compute the number of unique states and transitions
		Iterator<Entry<String, AppState>> iter = FSMStates.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,AppState> pair = (Entry<String, AppState>)iter.next();
			AppState state = pair.getValue();
			if(state.isUniqueState()){
				uniqueStatesCount ++;
				uniqueTransitionsCount += state.getTransitionCount();
			}
		}
		System.out.println(DEBUG_STRING + "I: the FSM model has total *unique states*: " + uniqueStatesCount + 
				"; total *unique transitions*: " + uniqueTransitionsCount);
		
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(new BufferedWriter(new FileWriter(fsm_building_progress_file, true)));
			pw.write(uniqueStatesCount + "  " + uniqueTransitionsCount + "\n");
		}catch(IOException e){
			System.out.println(DEBUG_STRING + "E: failed to write fsm_building_progress_file!");
			e.printStackTrace();
		}
		pw.close();
	}
	
	
	/**
	 * clear the app FSM 
	 * When an avd restarts, the previous app FSM should be cleared.
	 */
	public void clearAppFSM(){
		if(FSMBuilder == null)
			return;
		else{
			//clear all FSM data
			FSMStates.clear();
			FSMAllExecutedStates.clear();
			currentStateName = null;
			
			listImages.clear();
	    	listEditBoxes.clear();
	    	listTextViewString.clear();
	    	listButtonString.clear();
	    	listTexts.clear();
		}
	}
	
	
	/** 
	 * export the app FSM to a dot file. 
	 * DOT file format: http://en.wikipedia.org/wiki/DOT_(graph_description_language)
	 * @param dotFileName the dot file name
	 * */
	public void exportAppFSMDotFile(String dotFileName){
		
		System.out.println(DEBUG_STRING + "I: export the FSM into the dot file: " + dotFileName);
		String indent = "  ";
		String nextLine = "\n";
		String direction = " -> ";
		String endFlag = ";";
		String graph = "digraph graphtest {" + nextLine;
		
		for(String stateName: FSMAllExecutedStates){
			AppState state = FSMStates.get(stateName);
			if(state.isUniqueState()){
				System.out.println("	unique state name: " + state.getSimpleStateName());
				//output unique states
				for(int i: state.getExecutedTransitions()){
					Transition tran = state.getStateTransition(i);
					String startStateName = tran.getStartAppStateName();
					String endStateName = tran.getEndAppStateName();
					System.out.println("		transition: " 
							+ FSMStates.get(startStateName).getSimpleStateName() + " -> "
							+ FSMStates.get(endStateName).getSimpleStateName()
							+ ", action:" + tran.getAssociatedActionCmd() );
					AppState endState = FSMStates.get(endStateName);
					if(endState.isSpecialState()){
						graph += indent + FSMStates.get(startStateName).getSimpleStateName()
								+ direction + FSMStates.get(endStateName).getSimpleStateName()
								+ indent +  "[ style=dotted,label=\"@" + tran.getAssociatedActionID() + "\"] "
								+ endFlag + nextLine;
					}else{
						graph += indent + FSMStates.get(startStateName).getSimpleStateName()
								+ direction + FSMStates.get(endStateName).getSimpleStateName()
								+ indent +  "[ label=\"@" + tran.getAssociatedActionID() + "\"] "
								+ endFlag + nextLine;
					}
				}
			}
		}
		graph += "}";
		
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(dotFileName, "UTF-8");
			pw.write(graph);
		}catch(IOException e){
			System.out.println(DEBUG_STRING + "E: failed to export dot file!");
			e.printStackTrace();
		}
		pw.close();
	}
	
	/**
	 * Dump the app FSM into a file. Only unique app states and their transitions are dumped.
	 * It aims to bring convenience to debugging.
	 * 
	 * The file content format:
	 * # file description line
	 * the app state: 
	 * @param fileName the file to dump the app FSM
	 */
	public void dumpAppFSM(String fileName){
		System.out.println(DEBUG_STRING + "I: dump the FSM into the file: " + fileName);
		
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(fileName, "UTF-8");
			//write a file description line
			pw.println("# the app FSM, including all unique app states and transitions. Please keep this file unchanged. ");
			//write the number of states and transitions
			pw.println(uniqueStatesCount + ";" + uniqueTransitionsCount);
			//iterate all unique app states
			Iterator<Entry<String, AppState>> iter = FSMStates.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String,AppState> pair = (Entry<String, AppState>)iter.next();
				AppState state = pair.getValue();
				if(state.isUniqueState()){
					//write the app state
					//the state name "\n" the state simple name " " the activity name " " the transition counts
					pw.println(state.getStateName());
					pw.println(state.getSimpleStateName() + ";" + state.getActivityName() + ";" + state.getTransitionCount());
					//iterate all unique transitions of an app state
					Map<Integer, Transition> stateTransitions = state.getStateTransitions();
					Iterator<Entry<Integer, Transition>> transitionIter = stateTransitions.entrySet().iterator();
					while(transitionIter.hasNext()){
						Map.Entry<Integer,Transition> transitionPair = (Entry<Integer,Transition>)transitionIter.next();
						Transition tran = transitionPair.getValue();
						//write the transitions of the app state: in the order of its property declarations separated by a space
						pw.println(tran.getStartAppStateName() + ";" + tran.getEndAppStateName() + ";" + tran.getAssociatedActionID()
								+ ";" + tran.getAssociatedActionCmd().replace("\n", "") + ";" + tran.getTransitionID() + ";" + tran.getExecutedTimes()
								+ ";" + tran.getExecutionProb());
					}
					
				}
			}
		}catch(IOException e){
			System.out.println(DEBUG_STRING + "E: failed to dump FSM!");
			e.printStackTrace();
		}
		pw.close();
	}
	
	/**
	 * restore the app FSM from the FSM file
	 * @param fileName
	 */
	public void restoreAppFSM(String fileName){
		System.out.println(DEBUG_STRING + "I: restore the FSM from the file: " + fileName);
		try {
			InputStream input = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(input, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);
			String line;
			try {
				//get the file description
				line = br.readLine();
				System.out.println("I: " + line);
				//get the number of total app states and transitions
				line = br.readLine();
				String[] counts = line.split(";");
				int appStatesCount = Integer.parseInt(counts[0]);
				int appTransitionsCount = Integer.parseInt(counts[1]);
				System.out.println(DEBUG_STRING + "I: app states count: " + appStatesCount + ", transitions count: " + appTransitionsCount);
				//get the unique states and transitions count
				uniqueStatesCount = appStatesCount;
				uniqueTransitionsCount = appTransitionsCount;
				//get the app state
				for(int i=0; i<appStatesCount; i++){
					AppState state = new AppState();
					//get the app state name
					line = br.readLine();
					state.setStateName(line);
					//get the app simple name, activity name, the number of transitions
					line = br.readLine();
					String[] stateInfo = line.split(";");
					int stateTransitionsCount = Integer.parseInt(stateInfo[2]);
					state.setActivityName(stateInfo[1]);
					//System.out.println(line);
					//read the transitions of this app state
					for(int j=0; j<stateTransitionsCount; j++){
						line = br.readLine();
						//System.out.println(line);
						//get the transition info
						String[] transitionInfo = line.split(";");
						//start state name, end state name, and action id
						Transition tran = new Transition();
						tran.setStartAppState(transitionInfo[0]);
						tran.setEndAppState(transitionInfo[1]);
						tran.setAssociatedActionId(Integer.parseInt(transitionInfo[2]));
						//action cmd
						tran.setAssociatedActionCmd(transitionInfo[3]);
						//transition id
						tran.setTransitionID(Integer.parseInt(transitionInfo[4]));
						//executed times
						tran.setExecutionTimes(Integer.parseInt(transitionInfo[5]));
						//executed probability, initialized as 0.0
						tran.setExecutionProb(0.0);
						
						// add the action id into the set
						FSMActionIdsSet.add(Integer.parseInt(transitionInfo[2]));
						
						//add transition
						addTransition(state, tran);
					}
					//add state
					addState2(state);
				}
				
			} catch (IOException e) {
				System.out.println(DEBUG_STRING + "E: failed to restore FSM, *readline* !");
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			System.out.println(DEBUG_STRING + "E: failed to restore FSM!");
			e.printStackTrace();
		}
	}
	
	/** is it an unexpected line ? */
	private boolean isUnexpectedLine(String line){
    	if(line.startsWith("I/")){
    		//skip "I/umd.troyd ..." lines
    		System.out.println(DEBUG_STRING + "I: skip the line :" + line + " , this is not an expected app state line");
    		return true;
    	}
    	return false;
	}
	
	private boolean isEmptyAppStateLine(String line){
		if(line.equals(AppState.EMPTY_APP_STATE)){
			//EMPTY_APP_STATE
			System.out.println(DEBUG_STRING + "I: this is an *EMPTY* app state, we may lose the app focus");
			return true;
		}
		return false;
	}
	
	private boolean isResetAppStateLine(String line){
		if(line.equals(AppState.RESET_APP_STATE)){
			//EMPTY_APP_STATE
			System.out.println(DEBUG_STRING + "I: this is an *RESET* app state, we need to restart the app");
			return true;
		}
		return false;
	}
	
	/** If a line starts with a view id and has 9 properties, then it is a property line */
	private boolean isViewPropertyLine(String line){
		if(line == null || line.length() == 0 || line.contains(" ") == false)
			return false;
		String content[] = line.split(" ");
		try{
			Integer.parseInt(content[0]);
			//a view property line should has 9 properties
			if(content.length == 9)
				return true;
			else
				return false;
		}catch(NumberFormatException nfe){
			return false;
		}
		
		
	}
	
	
	/** parse the UI xml file */
	public void parseAppState(String uiFileName, String packageName, String activityName){
		
		//create a new app state
		AppState state = new AppState();
		//create the ui page
		UIPage page = new UIPage();
		
		//TODO test whether the code is correct?
		if(uiFileName.contains(AppState.EMPTY_APP_STATE)){
			//when it is an empty state, uiFileName is "/EMPTY_APP_STATE.xml"
			//empty state, do nothing
			state.setStateName(uiFileName);
			state.setActivityName(activityName);
			state.setStateTag(AppState.EMPTY_APP_STATE);
			System.out.println("D: this is an empty state! ");
			
		}else if(uiFileName.contains(AppState.RESET_APP_STATE)){
			//when it is a reset state, uiFileName is "/RESET_APP_STATE.xml"
			//reset state, do nothing
			state.setStateName(uiFileName);
			state.setActivityName(activityName);
			state.setStateTag(AppState.RESET_APP_STATE);
			System.out.println("D: this is a reset state! ");
			
		}else{
			
			state.setStateName(uiFileName);
			state.setActivityName(activityName);
		
			try{
				
				File fXmlFile = new File(uiFileName);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				
				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				doc.getDocumentElement().normalize();
				
				
				// check whether it has child nodes
				if(doc.hasChildNodes()){
					
					NodeList nl = doc.getChildNodes();
					int nl_size = nl.getLength();
					if(nl_size == 1){ //the root element node "hierarchy"
						Node root_node = nl.item(0);
						if(root_node.hasChildNodes()){
							// assign the root node's default properties (clickable, long-clickable, checkable, and 
							// scrollable) as false  
							page.analyzeNode(root_node.getChildNodes(), false, false, false, false);
						}
						else{
							System.out.println("E: the root element \"hierarchy\" has no child nodes?? ");
							System.exit(0);
						}
					}else{
						System.out.println("E: the root element node is not the unique \"hierarchy\"?? ");
						System.exit(0);
					}
					
				}
			}catch(Exception e){
				System.out.println(e.getMessage());
				System.out.println("D: error when parsing the xml file");
				System.exit(0);
			}
		}

		// associate the state with its page
		state.setUIPage(page);
		// add this state
		addState(state);
	
	}
	
	
	// Just use for debug
	public static void main(String[] args){
		String uiFileName = "/home/suting/AppTest/fr.kwiatkowski.ApkTrack_11_src.tar2.gz_fsm_building_3/ui/S_871.xml";
		AndroidAppFSM.getFSMBuilder().parseAppState(uiFileName, "", "");
	}
	
	
	//dump all app states in the FSM
	public void dumpAllAppStates(String fileName) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName,"UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Iterator<Entry<String, AppState>> iter = FSMStates.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,AppState> pair = (Entry<String, AppState>)iter.next();
			String appStateFileName = pair.getKey();
			AppState state = pair.getValue();
			pw.println("the state file name: " + appStateFileName);
			pw.println(state.toString());
			pw.println("-----------\n");
		}
		pw.close();
		System.out.println("I: dump all app states in the FSM into the file: " + fileName);
	}
	
	
    /*
     * detect invokable actions from the UI page (xml file)
     * The basic work flow :
     * For each view component on the current app state,
     * First, create a basic "click" action according to the component type (called as a dynamic/screen action
     *  since it is generated according to the current dynamic screen state)
     * Second, check whether the component itself has other invokable actions (i.e., those actions detected 
     * 	by static analysis, called as static actions, which are not detected in the first phase.)
     * Third, check whether the current app state has system actions, e.g., menu, back, down, up.
     */    
    public String detectInvokableActions(String appStateFileName) {
    	
        //get the app state
        AppState state = FSMStates.get(appStateFileName);
        UIPage page = state.getUIPage();
        
        System.out.println(DEBUG_STRING + "I: start to detect invokable actions in the current app state. ");
        
        if(page.getExecutableObjects().size() == 0){
        	
        	if(state.getStateTag().equals(AppState.EMPTY_APP_STATE)){
        		System.out.println(DEBUG_STRING + "I: the app state does not has view components, this is an empty state.");
        		//add keyevent_back action
        		ActionHandler.getHandler().addKeyEventBackAction(state);
        	}else if(state.getStateTag().equals(AppState.RESET_APP_STATE)){
        		System.out.println(DEBUG_STRING + "I: the app state does not has view components, this is a reset state.");
        		//add reset action
        		ActionHandler.getHandler().addResetAction(state);
        	}
	    	
	        System.out.println("==========");
	        System.out.println(DEBUG_STRING + "I: the final detected invokable actions on this app state: ");
	        state.printInvokableActions();
	        //get invokable actions from this app state
	        return state.getInvokableActions();

        	
        }else if(appStateFileName.contains("overflow_menu")){  //special handle on overflow menu since UIAutomator fail to dump xml file on overflow menus
        	
        	
        		
        		System.out.println(DEBUG_STRING + "I: read overflow menu action list " + "aagtl_overflow_menu_events.txt");
        		try {
        			
        			InputStream input = null;
        			if(appStateFileName.contains("aagtl")){
        				input = new FileInputStream("/home/suting/proj/fsmdroid/special/aagtl_overflow_menu_events.txt");
        			}else if(appStateFileName.contains("sanity")){
        				input = new FileInputStream("/home/suting/proj/fsmdroid/special/sanity_overflow_menu_events.txt");
        			}else if(appStateFileName.contains("wiki")){
        				input = new FileInputStream("/home/suting/proj/fsmdroid/special/wiki_overflow_menu_events.txt");
        			}else if(appStateFileName.contains("bubble")){
        				input = new FileInputStream("/home/suting/proj/fsmdroid/special/frozenbubble_overflow_menu_events.txt");
        			}
        			InputStreamReader isr = new InputStreamReader(input, Charset.forName("UTF-8"));
        			BufferedReader br = new BufferedReader(isr);
        			String line;
        			try{
        				//get the file description
        				while((line = br.readLine()) != null){
	        				System.out.println("I: " + line + "#");
	        		
			        		DynamicAction da = new DynamicAction();
			        		da.setActionID();
			        		da.setViewCIS("");
			        		da.setActivityName(state.getActivityName());
			        		da.setActionSource(Action.actionFromScreenAnalysis);
			        		da.setActionType(Action.tap);
			        		
			        		String actionCmd = line + "\n";
			        		da.setActionCmd(actionCmd,"");
			        		
			        		// add this new blind action into the action list
			        		Integer actionID = ActionHandler.getHandler().addAction(da);
			        		// add the action into the app state's action list
			        		state.addInvokableAction(actionID);
			        	
			        		System.out.println(DEBUG_STRING + "I: create a *Tap* Action!");
        				}
	        		} catch (IOException e) {
	    				System.out.println(DEBUG_STRING + "E: failed to read overflow menu file, *readline* !");
	    				e.printStackTrace();
	    				System.exit(0);
	    			}
	    		} catch (FileNotFoundException e) {
	    			System.out.println(DEBUG_STRING + "E: failed to read overflow menu fil!");
	    			e.printStackTrace();
	    			System.exit(0);
	    		}
        	
        	//add system actions
	    	ActionHandler.getHandler().addSystemActions(state);
	    	
	        System.out.println("==========");
	        System.out.println(DEBUG_STRING + "I: the final detected invokable actions on this app state: ");
	        state.printInvokableActions();
	        //get invokable actions from this app state
	        return state.getInvokableActions();
        	
        }else{
        
	        //iterate on all ui executable objects on this app page
	        for(UIExecutableObject eo: page.getExecutableObjects()){
	        	
	        	//get the view type
	        	String viewType = eo.getClassName();
	        	
	            System.out.println(DEBUG_STRING + "I: view type: " + viewType);
	            System.out.println(DEBUG_STRING + "I: cis - " + eo.getCIS());
	            
	            // TODO Now we consider only "click" and "long click"
	            if(eo.getClassName().contains(".TextView") 
	            		|| eo.getClassName().contains(".Button")
	            		|| eo.getClassName().contains(".ToggleButton")){       
	            	
	            	// Debug
	            	System.out.println("clickable = " + eo.isClickable() + ", longclickable = " + eo.isLongClickable());
	            	
		            if(eo.isClickable()){ 
		            	generateClickAction(state, eo);
		            }
		            if(eo.isLongClickable()){
		            	generateLongClickAction(state, eo);
		            }
		            
	    			System.out.println(DEBUG_STRING + "I: up to now, detected invokable actions: ");
	            	state.printInvokableActions();
	            	System.out.println("==========");
	            	
	            }else if(eo.getClassName().contains(".ImageView")  // usually text =""
	            		|| eo.getClassName().contains(".ImageButton") ){
	            	
	            	if(eo.isClickable()){ 
	            		generateClickImageAction(state, eo);
	            	}
	            	if(eo.isLongClickable()){
	            		generateLongClickImageAction(state, eo);
	            	}
			            
	    			System.out.println(DEBUG_STRING + "I: up to now, detected invokable actions: ");
	            	state.printInvokableActions();
	            	System.out.println("==========");
	            	
	            }else if( eo.getClassName().contains(".EditText") 		//EditText
			|| eo.getClassName().contains(".MultiAutoCompleteTextView")
				){
	            	//generate EditText actions
	            	generateEditTextAction(state, eo);
	            	
	            }else if(eo.getClassName().contains(".CheckBox") 		//CheckBox
	            		|| eo.getClassName().contains(".RadioButton")){  //RadioButton
	            	
	            	// Debug
	            	System.out.println("checkable = " + eo.isCheckable());
	            	
	            	if(eo.isCheckable()){
	            		generateCheckAction(state, eo);
	            	}
	            }else if(eo.getClassName().contains(".CheckedTextView")){ //like RadioButton
	            	// Debug
	            	System.out.println("checkable = " + eo.isCheckable());
	            	
	            	if(eo.isCheckable()){
	            		generateCheckTextViewAction(state, eo);
	            	}
	            }else if(eo.getClassName().contains(".SeekBar")){
	            	// Debug
	            	System.out.println("clickable = " + eo.isClickable());
	            	
	            	if(eo.isClickable()){
	            		generateSeekbarAction(state, eo);
	            	}
	            }
	        }
	        
        	//System.out.println("I: all actions in this app: ");
        	//ActionHandler.getHandler().outputAllActions();
        	
	        //add system actions
	    	ActionHandler.getHandler().addSystemActions(state);
	    	
	        System.out.println("==========");
	        System.out.println(DEBUG_STRING + "I: the final detected invokable actions on this app state: ");
	        state.printInvokableActions();
	        
	        //TODO uncomment it, breakpoint
//	        char[] inputs = new char[4];
//	        InputStreamReader isr = new InputStreamReader(System.in);
//	        try {
//				isr.read(inputs);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	        
	        //get invokable actions from this app state
	        return state.getInvokableActions();
	        
	        
        }
    }
    
    public void generateClickAction(AppState state, UIExecutableObject eo){
    	DynamicAction da = new DynamicAction();
		da.setActionID();
		da.setViewCIS(eo.getCIS());
		da.setViewText(eo.getText());
		da.setActivityName(state.getActivityName());
		da.setActionSource(Action.actionFromScreenAnalysis);
		da.setActionType(Action.click);
		
		String actionCmd = "";
		
		//For TextView, text is better than resource id
		if(!eo.getText().equals("")){ // if the "text" field is not empty
			if(!ConfigOptions.USE_UI_INSTANCE){
				//use text to create action cmd, not very accurate, but intuitive
				if(!eo.hasMultiLinesText()){
					actionCmd = Action.click + "(text=\'" + eo.getText() + "\')\n";
				}else{
					actionCmd = Action.click + "(textContains=\'" + eo.getFirstLineText() + "\')\n";
				}
			}else{
				//use view instance to create action cmd, accurate, but not intuitive
				actionCmd = Action.click + "(className=\'" + eo.getClassName() + "\',instance=\'" + eo.getInstance() + "\')\n";
			}
		}else if(!eo.getResourceId().equals("")){ // if the "resource-id" field is not empty
			actionCmd = Action.click + "(resource-id=\'" + eo.getResourceId() + "\')\n"; 
		}else if(!eo.getContentDesc().equals("")){ // if the "content-desc" field is not empty
			actionCmd = Action.click + "(content-desc=\'" + eo.getContentDesc() + "\')\n"; 
		}else{

			//if actionCmd = "", return
			return;
			
		}
		
		da.setActionCmd(actionCmd,eo.getClassName());
		
		// add this new blind action into the action list
		Integer actionID = ActionHandler.getHandler().addAction(da);
		// add the action into the app state's action list
		state.addInvokableAction(actionID);
	
		System.out.println(DEBUG_STRING + "I: create a *Click* Action!");
    }
    
    
    public void generateLongClickAction(AppState state, UIExecutableObject eo){
    	DynamicAction da = new DynamicAction();
		da.setActionID();
		da.setViewCIS(eo.getCIS());
		da.setViewText(eo.getText());
		da.setActivityName(state.getActivityName());
		da.setActionSource(Action.actionFromScreenAnalysis);
		da.setActionType(Action.longClick);
		
		String actionCmd = "";
		
		//We put getResourceId at the first place before since resource id is more accurate than text
		if(!eo.getText().equals("")){ // if the "text" field is not empty
			if(!ConfigOptions.USE_UI_INSTANCE){
				if(!eo.hasMultiLinesText()){
					actionCmd = Action.longClick + "(text=\'" + eo.getText() + "\')\n";
				}else{
					actionCmd = Action.longClick + "(textContains=\'" + eo.getFirstLineText() + "\')\n";
				}
			}else{
				actionCmd = Action.longClick + "(className=\'" + eo.getClassName() + "\',instance=\'" + eo.getInstance() + "\')\n";
			}
		}else if(!eo.getResourceId().equals("")){ // if the "resource-id" field is not empty
			actionCmd = Action.longClick + "(resource-id=\'" + eo.getResourceId() + "\')\n"; 
		}else if(!eo.getContentDesc().equals("")){ // if the "content-desc" field is not empty
			actionCmd = Action.longClick + "(content-desc=\'" + eo.getContentDesc() + "\')\n"; 
		}else{
			//if actionCmd = "", return
			return;
		}
		
		da.setActionCmd(actionCmd,eo.getClassName());
		
		// add this new blind action into the action list
		Integer actionID = ActionHandler.getHandler().addAction(da);
		// add the action into the app state's action list
		state.addInvokableAction(actionID);
	
		System.out.println(DEBUG_STRING + "I: create a *Long Click* Action!");
    }
    
    
    public void generateClickImageAction(AppState state, UIExecutableObject eo){
    	DynamicAction da = new DynamicAction();
		da.setActionID();
		da.setViewCIS(eo.getCIS());
		da.setViewText(eo.getText());
		da.setActivityName(state.getActivityName());
		da.setActionSource(Action.actionFromScreenAnalysis);
		da.setActionType(Action.click);
		
		String actionCmd = "";
		
		if(!eo.getResourceId().equals("")){ // if the "resource-id" field is not empty
			actionCmd = Action.click + "(resource-id=\'" + eo.getResourceId() + "\')\n"; 
		}else if(!eo.getContentDesc().equals("")){ // if the "content-desc" field is not empty
			actionCmd = Action.click + "(content-desc=\'" + eo.getContentDesc() + "\')\n"; 
		}else{
			//use view instance to create action cmd, accurate, but not intuitive
			actionCmd = Action.click + "(className=\'" + eo.getClassName() + "\',instance=\'" + eo.getInstance() + "\')\n";
		}
		
		da.setActionCmd(actionCmd,eo.getClassName());
		
		// add this new blind action into the action list
		Integer actionID = ActionHandler.getHandler().addAction(da);
		// add the action into the app state's action list
		state.addInvokableAction(actionID);
	
		System.out.println(DEBUG_STRING + "I: create a *Click Image* Action!");
    }
    
    public void generateLongClickImageAction(AppState state, UIExecutableObject eo){
    	DynamicAction da = new DynamicAction();
		da.setActionID();
		da.setViewCIS(eo.getCIS());
		da.setViewText(eo.getText());
		da.setActivityName(state.getActivityName());
		da.setActionSource(Action.actionFromScreenAnalysis);
		da.setActionType(Action.longClick);
		
		String actionCmd = "";
		
		if(!eo.getResourceId().equals("")){ // if the "resource-id" field is not empty
			actionCmd = Action.longClick + "(resource-id=\'" + eo.getResourceId() + "\')\n"; 
		}else if(!eo.getContentDesc().equals("")){ // if the "content-desc" field is not empty
			actionCmd = Action.longClick + "(content-desc=\'" + eo.getContentDesc() + "\')\n"; 
		}else{
			//use view instance to create action cmd, accurate, but not intuitive
			actionCmd = Action.longClick + "(className=\'" + eo.getClassName() + "\',instance=\'" + eo.getInstance() + "\')\n";
		}
		
		da.setActionCmd(actionCmd,eo.getClassName());
		
		// add this new blind action into the action list
		Integer actionID = ActionHandler.getHandler().addAction(da);
		// add the action into the app state's action list
		state.addInvokableAction(actionID);
	
		System.out.println(DEBUG_STRING + "I: create a *Long Click Image* Action!");
    }
    
    
    public void generateCheckAction(AppState state, UIExecutableObject eo){
    	DynamicAction da = new DynamicAction();
		da.setActionID();
		da.setViewCIS(eo.getCIS());
		da.setViewText(eo.getText());
		da.setActivityName(state.getActivityName());
		da.setActionSource(Action.actionFromScreenAnalysis);
		da.setActionType(Action.click);
		
		String actionCmd = "";
		
		//For CheckBox, the resource id is more unique than text
		if(!eo.getResourceId().equals("")){ // if the "resource-id" field is not empty
			actionCmd = Action.click + "(resource-id=\'" + eo.getResourceId() + "\')\n"; 
		}else if(!eo.getText().equals("")){ // if the "text" field is not empty
			if(!eo.hasMultiLinesText()){
				actionCmd = Action.click + "(text=\'" + eo.getText() + "\')\n";
			}else{
				actionCmd = Action.click + "(textContains=\'" + eo.getFirstLineText() + "\')\n";
			}
		}else if(!eo.getContentDesc().equals("")){ // if the "content-desc" field is not empty
			actionCmd = Action.click + "(content-desc=\'" + eo.getContentDesc() + "\')\n"; 
		}else{
			// the last resort
			actionCmd = Action.click + "(className=\'" + eo.getClassName() + "\',instance=\'" + eo.getInstance() + "\')\n";
		}
		
		da.setActionCmd(actionCmd,eo.getClassName());
		
		// add this new blind action into the action list
		Integer actionID = ActionHandler.getHandler().addAction(da);
		// add the action into the app state's action list
		state.addInvokableAction(actionID);
	
		System.out.println(DEBUG_STRING + "I: create a *CheckBox/RadioButton* Action!");
    }
    
    public void  generateCheckTextViewAction(AppState state, UIExecutableObject eo){
    	DynamicAction da = new DynamicAction();
		da.setActionID();
		da.setViewCIS(eo.getCIS());
		da.setViewText(eo.getText());
		da.setActivityName(state.getActivityName());
		da.setActionSource(Action.actionFromScreenAnalysis);
		da.setActionType(Action.click);
		
		String actionCmd = "";
		
		//For CheckTextView, the text is better than resource id
		if(!eo.getText().equals("")){ // if the "text" field is not empty
			if(!ConfigOptions.USE_UI_INSTANCE){
				if(!eo.hasMultiLinesText()){
					actionCmd = Action.click + "(text=\'" + eo.getText() + "\')\n";
				}else{
					actionCmd = Action.click + "(textContains=\'" + eo.getFirstLineText() + "\')\n";
				}
			}else{
				actionCmd = Action.click + "(className=\'" + eo.getClassName() + "\',instance=\'" + eo.getInstance() + "\')\n";
			}
		}else if(!eo.getResourceId().equals("")){ // if the "resource-id" field is not empty
			actionCmd = Action.click + "(resource-id=\'" + eo.getResourceId() + "\')\n"; 
		}else if(!eo.getContentDesc().equals("")){ // if the "content-desc" field is not empty
			actionCmd = Action.click + "(content-desc=\'" + eo.getContentDesc() + "\')\n"; 
		}else{
			//if actionCmd = "", return
			return;
		}
		
		da.setActionCmd(actionCmd,eo.getClassName());
		
		// add this new blind action into the action list
		Integer actionID = ActionHandler.getHandler().addAction(da);
		// add the action into the app state's action list
		state.addInvokableAction(actionID);
	
		System.out.println(DEBUG_STRING + "I: create a *Check TextView* Action!");
    }
    
    public void generateSeekbarAction(AppState state, UIExecutableObject eo){
    	DynamicAction da = new DynamicAction();
		da.setActionID();
		da.setViewCIS(eo.getCIS());
		da.setViewText(eo.getText());
		da.setActivityName(state.getActivityName());
		da.setActionSource(Action.actionFromScreenAnalysis);
		da.setActionType(Action.click);
		
		String actionCmd = "";
		
		//For Seekbar, text usually is empty
		if(!eo.getResourceId().equals("")){ // if the "resource-id" field is not empty
			actionCmd = Action.click + "(resource-id=\'" + eo.getResourceId() + "\')\n"; 
		}else if(!eo.getContentDesc().equals("")){ // if the "content-desc" field is not empty
			actionCmd = Action.click + "(content-desc=\'" + eo.getContentDesc() + "\')\n"; 
		}else{
			//the last resort
			actionCmd = Action.click + "(className=\'" + eo.getClassName() + "\',instance=\'" + eo.getInstance() + "\')\n";
		}
		
		da.setActionCmd(actionCmd,eo.getClassName());
		
		// add this new blind action into the action list
		Integer actionID = ActionHandler.getHandler().addAction(da);
		// add the action into the app state's action list
		state.addInvokableAction(actionID);
	
		System.out.println(DEBUG_STRING + "I: create a *SeekBar Click* Action!");
    }
    
    public void generateEditTextAction(AppState state, UIExecutableObject eo){

    	DynamicAction da = new DynamicAction();
		da.setActionID();
		da.setViewCIS(eo.getCIS());
		da.setViewText(eo.getText());
		da.setActivityName(state.getActivityName());
		da.setActionSource(Action.actionFromScreenAnalysis);
		da.setActionType(Action.editText);
		
		String actionCmd = "";
		
		
		
		//For EditText, we put getResourceId at the first place before since resource id is more accurate than text
		if(!eo.getResourceId().equals("")){ // if the "resource-id" field is not empty
			actionCmd = Action.editText + "(resource-id=\'" + eo.getResourceId() + "\')\n"; 
		}
		//For EditText, it is not a good way to use its text to identify them.
//		else if(!eo.getText().equals("")){ // if the "text" field is not empty
//			if(!ConfigOptions.USE_UI_INSTANCE){
//				if(!eo.hasMultiLinesText()){
//					actionCmd = Action.editText + "(text=\'" + eo.getText() + "\')\n";
//				}else{
//					actionCmd = Action.editText + "(textContains=\'" + eo.getFirstLineText() + "\')\n";
//				}
//			}else{
//				
//			}
//		}
		else if(!eo.getContentDesc().equals("")){ // if the "content-desc" field is not empty
			actionCmd = Action.editText + "(content-desc=\'" + eo.getContentDesc() + "\')\n"; 
		}else{
			//resource id, text and content desc are all empty, we have to use class name + instance
			actionCmd = Action.editText + "(className=\'" + eo.getClassName() + "\',instance=\'" + eo.getInstance() + "\')\n";
		}
		
		da.setActionCmd(actionCmd,eo.getClassName());
		
		// add this new blind action into the action list
		Integer actionID = ActionHandler.getHandler().addAction(da);
		// add the action into the app state's action list
		state.addInvokableAction(actionID);
	
		System.out.println(DEBUG_STRING + "I: create an *EditText* Action!");
		
    }
    
	
}
