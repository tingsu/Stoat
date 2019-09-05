public class InheritedAction extends Action{
	
	private String actionName;
	private int actionOverrideMethodLine;
	private String overrideMethodName;
	
	/* constructor */
	public InheritedAction(){
		setViewID(nilViewID);
		this.actionName = "";
		this.actionOverrideMethodLine = -1;
		this.overrideMethodName = "";
		setActivityName("");
	}
	
	/* set functions */
	public void setActionName(String actionName){
		this.actionName = actionName;
	}
	public void setActionOverrideMethodLine(int actionOverrideMethodLine){
		this.actionOverrideMethodLine = actionOverrideMethodLine;
	}
	public void setOverrideMethodName(String overrideMethodName){
		this.overrideMethodName = overrideMethodName;
	}
	
	public String getOverrideMethodName(){
		return this.overrideMethodName;
	}
	
	/* dump an inherited action */
	public String toString(){
		return Integer.toHexString(this.getViewID()) + "	" + this.actionName + "		" + this.actionOverrideMethodLine +
				"	" + this.overrideMethodName + "		"  + this.getActivityName();
	}
}