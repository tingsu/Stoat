public class RegisteredAction extends Action{
		
		private int actionListenerLine;
		private String callingMethodName;
		
		/* constructor, init. */
		public RegisteredAction(){
			setViewID(Action.nilViewID);
			this.actionListenerLine = -1;
			this.callingMethodName = "";
			setActionType("");
			setActivityName("");
		}
		
		/* set functions */
		public void setCallingMethodName(String methodName){
			this.callingMethodName = methodName;
		}
		public void setRegisteredActionLine(int line){
			this.actionListenerLine = line;
		}
		/* getter functions */
		public String getCallingMethodName(){
			return this.callingMethodName;
		}
		public int getRegisteredActionLine(){
			return this.actionListenerLine;
		}
		
		/* dump a registered action */
		public String toString(){
			return Integer.toHexString(this.getViewID()) +  "	" + this.getViewText() + "     " +   this.getActionType() 
					+ "		" + this.actionListenerLine 
					+ "	  " + this.callingMethodName + "		"  + this.getActivityName();
		}
	}