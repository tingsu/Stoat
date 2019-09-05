/* the menu item class */
public class MenuItem extends Action{
	
	public MenuItem(){
		//the default action type for a menu item is "click"
		setActionType(Action.textClick);
	}
	
	/* toString */
	public String toString(){
		return Integer.toHexString(this.getViewID()) + " " + this.getViewText()  + " " + this.getActionType();
	}
}