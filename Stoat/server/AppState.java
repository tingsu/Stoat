import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* the class for view info, i.e., the gui screen info */
public class AppState{
	
	/** the debug flag */
	public final static String DEBUG_STRING = "[AppState] ";
	
	/** the empty app state */
	public final static String EMPTY_APP_STATE = "EMPTY_APP_STATE";
	/** the reset app state: restart the app */
	public final static String RESET_APP_STATE = "RESET_APP_STATE";
	
	/** the view components the app state has */
	public List<ViewComponent> viewComponents;
	/** the invokable actions on this app state (it stores action ids) */
	public List<Integer> invokableActions;
	/** the invoked action transitions (transition id, transition), these transitions are unique! */
	private Map<Integer, Transition> transitions;
	/** the executed transitions in the time order, they are also unique! */
	public List<Integer> executedTransitions;
	private int transitionsCount;
	
	/** the UI page of this app state */
	private UIPage uiPage;
	
	/** the app state name (it stores the app state file name) */
	private String stateName;
	/** the simple state name */
	private String simpleStateName;
	/** the activity where the app state locates */
	private String activityName;
	/** the earliest alias state name */
	private String aliasStateName;
	/** the state tag, e.g., "Empty", "Reset" */
	private String stateTag;
	
	/** the md5 value */
	private String md5Value;
	public void computeMD5(){
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			//set its original content as empty in case of EMPTY_APP_STATE or RESET_APP_STATE
			String content = "";
			for(ViewComponent vc: this.viewComponents){//we abstract the app state as the set of visible widgets on the app screen
				content += "|" + vc.getViewID() + ";" + vc.getLocX() + ";" + vc.getLocY() + ";" +
						vc.getWidth() + ";" + vc.getHeight() + ";" + vc.getMeasuredWidth() +
						";" + vc.getMeasuredHeight();	
			}
			md5.update(StandardCharsets.UTF_8.encode(content));
			md5Value = String.format("%032x", new BigInteger(1, md5.digest()));
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			System.out.println(DEBUG_STRING + "E: fail to generate md5 value!");
			e.printStackTrace();
			System.exit(0);
		}		
	}
	
	/** Compute MD5 value based on the UI Page 
	 *  To mitigate state explosion, we follows:
	 *  1. For ui_layout_object, consider its "cis" and "class_name"
	 *  2. For ui_executable_object, consider its "cis" and "class_name", omitting the changes of text and checks
	 *  3. TODO we need to consider ListView here
	 * */
	public void computeMD5_new(){
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			//set its original content as empty in case of EMPTY_APP_STATE or RESET_APP_STATE
			String content = "";
			for(UILayoutObject lo: uiPage.ui_layout_objects){
				content += lo.getCIS() + lo.getClassName();
			}
			for(UIExecutableObject eo: uiPage.ui_executable_objects){
				content += eo.getCIS() + eo.getClassName();
			}
			
			md5.update(StandardCharsets.UTF_8.encode(content));
			md5Value = String.format("%032x", new BigInteger(1, md5.digest()));
			
		} catch (NoSuchAlgorithmException e) {
			System.out.println(DEBUG_STRING + "E: fail to generate md5 value!");
			e.printStackTrace();
			System.exit(0);
		}		
	}
	
	
	
	public String getMD5Value(){
		return md5Value;
	}
	
	//create an empty app state
	public AppState(){
		viewComponents = new ArrayList<ViewComponent>();
		transitions = new HashMap<Integer, Transition>();
		invokableActions = new ArrayList<Integer>();
		executedTransitions = new ArrayList<Integer>();
	}
	
	//setter
	public void setAliasStateName(String stateName){
		this.aliasStateName = stateName;
	}
	public void setStateName(String stateName){
		this.stateName = stateName;
		setSimpleStateName(this.stateName);
	}
	private void setSimpleStateName(String stateName){
		int lastSlashLoc = stateName.lastIndexOf('/');
		String temp = this.stateName.substring(lastSlashLoc+1);
		int dotLoc = temp.indexOf('.');
		String simpleStateName = temp.substring(0, dotLoc);
		System.out.println(simpleStateName);
		this.simpleStateName = simpleStateName;
	}
	public void setActivityName(String activityName){
		this.activityName = activityName;
	}
	public void setStateTag(String tag){
		this.stateTag = tag;
	}
	public void setUIPage(UIPage page){
		this.uiPage = page;
	}
	
	//getter
	public String getAliasStateName(){
		return this.aliasStateName;
	}
	public String getStateName(){
		return this.stateName;
	}
	public String getSimpleStateName(){
		return this.simpleStateName;
	}
	public String getActivityName(){
		return this.activityName;
	}
	public List<ViewComponent> getViewComponenets(){
		return this.viewComponents;
	}
	public String getStateTag() {
		return stateTag;
	}
	public UIPage getUIPage(){
		return uiPage;
	}
	

	/** get the number of unique transitions starting from this state */
	public int getTransitionCount(){
		return this.transitionsCount;
	}
	public List<Integer> getExecutedTransitions(){
		return this.executedTransitions;
	}
	public Transition getStateTransition(int id){
		return this.transitions.get(id);
	}
	/** get all unique transitions of the state */
	public Map<Integer, Transition> getStateTransitions(){
		return this.transitions;
	}
	
	public boolean isSpecialState(){
		if(this.stateTag != null)
			return true;
		else
			return false;
	}
	
	//add the transition
	public void addTransition(Transition transition){
		transitions.put(transition.getTransitionID(), transition);
		executedTransitions.add(transition.getTransitionID());
		//update the transition count
		transitionsCount = executedTransitions.size();
		System.out.println(DEBUG_STRING + "I: now the app state " + this.simpleStateName + 
				" has " + transitionsCount + " unique transitions.");
	}
	
	/**
	 *  TODO is this comparison enough? 
	 *  When two app states have the same activity name and the same components, they are treated as equal.
	 *  */
	public boolean isEqual(AppState targetState){
		//two states are not in the same activity
		if(! this.activityName.equals(targetState.activityName)){
			System.out.println(DEBUG_STRING + "I: activity name is different!");
			return false;
		}
		//two states have different numbers of view components
		if(this.viewComponents.size() != targetState.viewComponents.size()){
			System.out.println(DEBUG_STRING + "I: the number of view components is different!");
			return false;
		}
		//does two states have the same view components
		for(int i=0; i<this.viewComponents.size(); i++){
			ViewComponent c1 = this.viewComponents.get(i);
			ViewComponent c2 = targetState.viewComponents.get(i);
			//System.out.println("c1:" + c1.toString());
			//System.out.println("c2:" + c2.toString());
			if(!c1.isEqual(c2)){
				System.out.println(DEBUG_STRING + "I: some view component is different!");
				return false;
			}
		}
		return true;
	}
	
	/* is it a duplicate state */
	public boolean isUniqueState(){
		if(this.aliasStateName == null)
			//if it has no alias state name, it is a unique state
			return true;
		else
			return false;
	}
	
	/* add view components */
	public void addViewComponent(ViewComponent vc){
		this.viewComponents.add(vc);
	}
	
	/* add invokable actions */
	public void addInvokableAction(Integer actionID){
		System.out.println(DEBUG_STRING + "I: add an action <id: " + actionID + "> into the app state's invokable actions list");
		this.invokableActions.add(actionID);
	}
	
	
	public void printInvokableActions(){
		//collect invokable actions
        String invokableActions = "";
        for(Integer actionID: this.invokableActions ){
        	Action action = ActionHandler.getHandler().getAction(actionID);
        	if(action==null){
        		System.out.println(DEBUG_STRING + "I: action id: " + actionID);
        		System.out.println(DEBUG_STRING + "E: action is null !! ");
        		System.exit(0);
        	}
        	if(action.getActionCmd() == null){
        		System.out.println(DEBUG_STRING + "E: action command is null ??");
        	}
        	invokableActions += "<" + action.actionSource + ">" + "    " + actionID + "@" + action.getActionCmd() ;
        }
        System.out.println(invokableActions);
	}
	
	/* get invokable actions */
	public String getInvokableActions(){
		//collect invokable actions
        String invokableActions = "";
        for(Integer actionID: this.invokableActions ){
        	Action action = ActionHandler.getHandler().getAction(actionID);
        	assert(action!=null); //it must be found!!
        	if(action.getActionCmd() == null){
        		System.out.println(DEBUG_STRING + "E: action command is null ??");
        	}
        	invokableActions += "    " + actionID + "@" + action.getActionCmd();
        }
        return invokableActions;
	}
	
	/* print app state */
	public String toString(){
		String state = this.activityName + "\n";
		for(ViewComponent vc: viewComponents){
			state += vc.toString();
		}
		return state;
	}

	/** get the alias transition */
	public Transition getAliasTransition(Transition transition) {
		for(int id: executedTransitions){
			//get the transition
			Transition trans = transitions.get(id);
			if(trans.isEqual(transition)){
				System.out.println(DEBUG_STRING + "I: this is a duplicate transition, alias to the transition: id@ " + trans.getTransitionID());
				return trans;
			}
		}
		return null;
	}

	/** 
	 * update the transition 
	 * */
	public void updateTransition(Transition transition) {
		System.out.println(DEBUG_STRING + "I: update the transition: id@" + transition.getTransitionID());
		System.out.println(DEBUG_STRING + "I: the original transition: " + transition.toString());
		transition.incrExecutedTimes();
		System.out.println(DEBUG_STRING + "I: the updated transition: " + transition.toString());
	}
	
	/**
	 * compute the transition probabilities 
	 */
	public void computeTransitionProb(){
		
		System.out.println(DEBUG_STRING + "I:  this state has " + transitionsCount + " transitions.");
		
		if(ConfigOptions.COMPUTE_PROB_FROM_FSM_BUILDING){
			System.out.println(DEBUG_STRING + "I: init the probab from fsm building");
			int totalExecutedTimes = 0;
			for(int i=0; i<transitionsCount; i++){
				int id = executedTransitions.get(i);
				Transition tran = transitions.get(id);
				totalExecutedTimes += tran.getExecutedTimes();
			}
			System.out.println(DEBUG_STRING + "I: total executed times: " + totalExecutedTimes);
			for(int i=0; i<transitionsCount; i++){
				int id = executedTransitions.get(i);
				Transition tran = transitions.get(id);
				int executedTimes = tran.getExecutedTimes();
				double prob = executedTimes* 1.0 / totalExecutedTimes;
				tran.setExecutionProb(prob);
				System.out.println(DEBUG_STRING + "I: the execution prob: transition id: " + id + ", execution time: " + executedTimes +", prob: " + prob);
			}
		}else{ //average the probability values
			System.out.println(DEBUG_STRING + "I: init the probab by using the average probability");
			double prob = 1.0 / transitionsCount;
			for(int i=0; i<transitionsCount; i++){
				int id = executedTransitions.get(i);
				Transition tran = transitions.get(id);
				tran.setExecutionProb(prob);
				System.out.println(DEBUG_STRING + "I: the execution prob: transition id: " + id + ", prob: " + prob);
			}
		}
	}
	
	/**
	 * a helper function to get the simple app state name
	 * @param appStateName the app state name in the whole path
	 * @return the simple app state name
	 */
	public static String getSimpleAppStateName(String appStateName){
		int lastSlashLoc = appStateName.lastIndexOf('/');
		if(lastSlashLoc == -1){
			System.out.println(DEBUG_STRING + "E: Fail to locate the '/' in this appStateName: " + appStateName);
			System.exit(0);
		}
		return appStateName.substring(lastSlashLoc+1);
	}
}