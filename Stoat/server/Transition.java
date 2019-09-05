/* the transition of the android app FSM
 * */
public class Transition{
	
	private String startAppStateName;
	private String endAppStateName;
	/** the associated action's id */
	private int actionID;
	/** the associated action's command, NOTE it ends with a "\n" */
	private String actionCmd;
	/** the transition id, a key of a transition, note it is independent from action id 
	 * (its action id may be duplicate)
	 * */
	private int transitionID;
	/** the executed times of this transition */
	private int executedTimes;
	/** the execution probability of this transition */
	private double executionProb;
	
	
	/** the counter to generate transition ids */
	private static int transitionIdCounter = 1;
	
	public Transition(){
		this.executionProb = 0.0; 
		this.executedTimes = 1;
		this.transitionID = transitionIdCounter ++;
	}
	
	/**
	 * create a transition
	 * @param startAppStateName the start app state name
	 * @param endAppStateName the end app state name
	 * @param actionID the id of the executed action
	 */
	public Transition(String startAppStateName, String endAppStateName, int actionID){
		this.executionProb = 0.0; 
		this.executedTimes = 1;
		this.transitionID = transitionIdCounter ++;
		
		setStartAppState(startAppStateName);
		setEndAppState(endAppStateName);
		setAssociatedAction(actionID);
	}
	
	//getter
	public String getStartAppStateName(){
		return this.startAppStateName;
	}
	public String getEndAppStateName(){
		return this.endAppStateName;
	}
	public int getAssociatedActionID(){
		return this.actionID;
	}
	public String getAssociatedActionCmd(){
		return this.actionCmd;
	}
	public int getTransitionID(){
		return this.transitionID;
	}
	public double getExecutionProb(){
		return this.executionProb;
	}
	//setter
	public void setStartAppState(String startAppStateName){
		this.startAppStateName = startAppStateName;
	}
	public void setEndAppState(String endAppStateName){
		this.endAppStateName = endAppStateName;
	}
	public void setAssociatedAction(int actionID){
		this.actionID = actionID;
		this.actionCmd = ActionHandler.getHandler().getAction(actionID).getActionCmd();
	}
	public void setAssociatedActionId(int actionID){
		this.actionID = actionID;
	}
	public void setAssociatedActionCmd(String actionCmd){
		this.actionCmd = actionCmd;
	}
	public void setTransitionID(int id){
		this.transitionID = id;
	}
	public void setExecutionTimes(int times){
		this.executedTimes = times;
	}
	public void setExecutionProb(double prob){
		this.executionProb = prob;
	}
	
	/** is two transitions equal ? 
	 *  It only depends on its action, start state and end state
	 * */
	public boolean isEqual(Transition trans){
		if(this.actionID != trans.actionID){
			System.out.println("I: these two transitions have different action ids.");
			return false;
		}
		if(!this.startAppStateName.equals(trans.startAppStateName)){
			System.out.println("I: these two transitions have different start states.");
			return false;
		}
		if(!this.endAppStateName.equals(endAppStateName)){
			System.out.println("I: these two transitions have different end states.");
			return false;
		}
		return true;
	}
	
	/** increase the executed times of a transition */
	public void incrExecutedTimes() {
		this.executedTimes += 1;
	}
	
	/** get the executed times of the transition */
	public int getExecutedTimes(){
		return this.executedTimes;
	}
	
	public String toString(){
		return this.startAppStateName + " -> " + this.endAppStateName + " <" 
				+ this.actionCmd + ">" + "executed count: " + this.executedTimes;
	}
}