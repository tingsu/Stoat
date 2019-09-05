/* the super class of actions 
 * subclasses: inherited actions, registered actions and menu items
 * */
public class Action{
	
	private final static String DEBUG_STRING = "[Action] ";
	
	/* action types */
	public final static String click = "click";
	public final static String textClick = "click";
	public final static String textViewClickByIndex = "clickTextViewByIndex";
	public final static String longClick = "clickLong";  //text long click
	public final static String scroll = "scroll"; //scroll the screen
	public final static String textViewLongClickByIndex = "clickLongTextViewByIndex";
	public final static String editText = "edit";
	public final static String imageBtnClick = "clickImgBtn";
	public final static String imageViewClick = "clickImgView";
	public final static String menuItemClick = "clickMenuItem";
	public final static String checkboxClick = "clickCheckBox";
	public final static String radiobuttonClick = "clickRadioButton";
	public final static String togglebuttonClick = "clickToggleButton";
	public final static String buttonClick = "";
	public final static String textViewClick = "clickIdx";
	public final static String editTextClick = "key_event";
	public final static String tap = "tap";
		
	/** the unknown view id */
	public final static int nilViewID = -1;
	/** the action id counter */
	private static int actionIDCounter = 1;
	
	/** action id is the action identifier */
	private int actionID;
	/** view id is the associated view's id */
	private int viewID;
	/** the cis of the view from the ui xml file, which uniquely identify the view on one UI page, it can replace "viewID" */
	private String viewCIS;
	/** the associated view component, only remember view ID is not enough!! */
	private ViewComponent viewComponent;
	/** the view text */
	private String viewText;
	/** action type */
	private String actionType;
	/** the index of the view, e.g., "EditText" can be specified through an index */
	private int viewIndex;
	/** action command */
	private String actionCmd;
	/** the activity where the action locates */
	private String activityName;
	
	/** the action source from static analysis or screen analysis*/
	public String actionSource;
	public final static String actionFromStaticAnalysis ="Static";
	public final static String actionFromScreenAnalysis = "Screen";
	public final static String actionFromSystem = "System";
	
	/** 
	 * When an action is added into the global action list, if this action has already exists, this value
	 * will be set as the action id which is alias to this action 
	*/
	public int actionIdExistInActionList;
	
	/* setter */
	public void setActionID(){
		this.actionID = actionIDCounter ++;
	}
	public void setActionSource(String actionSource){
		this.actionSource = actionSource;
	}
	public void setViewID(int viewId){
		this.viewID = viewId;
	}
	public void setViewCIS(String cis){
		this.viewCIS = cis;
	}
	public void setViewComponent(ViewComponent vc){
		this.viewComponent = vc; 
	}
	public void setViewText(String viewText){
		this.viewText = viewText;
	}
	public void setViewIndex(int index){
		this.viewIndex = index;
	}
	public void setActivityName(String activityName){
		this.activityName = activityName;
	}
	public void setActionType(String actionType){
		this.actionType = actionType;
	}
	/**
	 * setActionCmd must be used after setActionType
	 * for action commands should contain actionType now.
	 * Action command format: id@action("text")|actionType
	 * @param actionCmd
	 */
	public void setActionCmd(String actionCmd,String viewType){//, String viewType
		//this.actionCmd = actionCmd;
		
		String viewText = this.getViewText();
		String cmdTag = "";
		
		if(viewText!=null){
			//Note: we concatenate the content of view text behind the action cmd in order to output more readable action commands for debugging.
			//In default, the action is invoked by object index. The view text is only used for understanding without other purposes.
			//To avoid any side effects, we remove all "\n" and ";" in the view texts, and put them in the quotes.
			String sanitized_viewText = viewText.replaceAll("\n", "").replaceAll(";", "");
			cmdTag = viewType + "@" + "\"" + sanitized_viewText + "\"";
		}
		else
			cmdTag = viewType + "@" + "null";
		
		if(viewType.equals(""))
		{
			this.actionCmd = actionCmd;//+":"+this.actionType;			
		}
		else{
			this.actionCmd=actionCmd.substring(0,actionCmd.length()-1) + ":" + cmdTag + "\n";//filter the '\n' in actionCmd and add viewType
		}
	}
	
	/* get the description of action cmd without the cmd tag, especially used for action comparison */
	private String getActionCmdDescription(){
		int cmdTagLoc = this.actionCmd.lastIndexOf(':');
		if(cmdTagLoc == -1)
			return this.actionCmd;
		else
			return this.actionCmd.substring(0, cmdTagLoc);
	}
	
	/* getter */
	public int getActionID(){
		return this.actionID;
	}
	public String getActionSource(){
		return this.actionSource;
	}
	public int getViewID(){
		return this.viewID;
	}
	public String getViewCIS(){
		return this.viewCIS;
	}
	public ViewComponent getViewComponnet(){
		return this.viewComponent;
	}
	public String getViewText(){
		return this.viewText;
	}
	public String getActivityName(){
		return this.activityName;
	}
	public String getActionType(){
		return this.actionType;
	}
	public String getActionCmd(){
		return this.actionCmd;
	}
	
	/* TODO update the action command according to its view !!
	 * An action command depends on its view type.
	 * This function may be updated in the future to support more view types
	 * */
	/*public void updateActionCmd(ViewComponent view){
		
		//does the action already have a command?
		if(this.getActionCmd() != null)
			return;
		
		String viewType = view.getViewType();
		
		if(viewType.contains("ImageView")){
			//TODO
		}else if(viewType.contains("ImageButton")){
			//TODO
		}else if(viewType.contains("EditText")){
			System.out.println(DEBUG_STRING + "I: update \"EditText\" action command, the original action command: " + getActionCmd());
			setActionCmd(getActionType() + "(\"" + view.getViewIndex() + "\")\n");
			System.out.println(DEBUG_STRING + "I: the updated command: " + actionCmd);		
		}else if(viewType.contains("TextView") || viewType.contains("Button")){
			//get the view text content
			String text = view.getTextContent();
			if ((text.contains("\"")) || ((text.contains("(")) && (text.contains(")")))) {
                text = text.replace("\"", "");
                text = text.replace("(", "");
                text = text.replace(")", "");
            }
			this.setActionCmd(this.getActionType() + "(\"" + text + "\")\n");
			
		}else{
			
		}
	}
	*/
	
	public String getActionDescription(){
		String action = "	";
		action += "action_id: " + this.actionID + "; view_id: " + this.viewID;
		if(this.viewText != null)
			action += "; view_text: " + this.viewText;
		else
			action += "; view_text: [ ]";
		if(this.actionType != null)
			action += "; action_type: " + this.actionType;
		else
			action += "; action_type: [ ]"; 
		if(this.actionCmd != null)
			action += "; action_cmd: " + this.actionCmd;
		else
			action += "; action_cmd: [ ]";
		if(this.actionSource != null)
			action += "; action_source: " + this.actionSource;
		else
			action += "; action_source: [ ]";
		return action;
	}
	
	/**
	 * override hashcode for hashset,
	 * filter the redundant value
	 * actionCmd format: 8@click("Just Sit")
	 */
	public int hashCode(){
		//System.out.println("hashcode");
		String cmd=this.getActionCmd();
		return getHashValue(cmd);
	}
	
	/**
	 * Transfer string to hashCode
	 * @param str
	 * @return
	 */
	private int getHashValue(String str){
		int hashCode=0;
		for (int i = 0; i < str.length(); i++) {
			hashCode+=(int)str.charAt(i);
		}
		return hashCode+this.viewID;
	}
	
	/**
	 * This function will only be invoked when comparing the equality between two actions
	 * two actions are the same if and only if 
	 * they have the same actionCmdString, viewCIS and activity name
	 */
	public boolean equals(Object o){
		System.out.println("equal");
		if(o instanceof Action){
			
			String cmd = this.getActionCmdDescription();
			
			Action action=(Action)o;
			String cmd2 = action.getActionCmdDescription();
			
			System.out.println(DEBUG_STRING + "I: cmd: " + cmd + ", view cis: " + this.getViewCIS() + ", activity name: " + this.getActivityName());
			System.out.println("equal:"+cmd+"|"+cmd2+"|");
			System.out.println("cmd2's view cis: " + action.getViewCIS() + " activity name" + action.getActivityName());
			if(cmd.equals(cmd2) 
			   && this.getViewCIS().equals(action.getViewCIS())
			   && this.getActivityName().equals(action.getActivityName()))
			{
				
				actionIdExistInActionList=action.actionID;
				return true;
			}
			else{
				return false;
			}
		}
		return false;
	}
}