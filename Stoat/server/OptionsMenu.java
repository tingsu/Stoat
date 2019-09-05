import java.util.ArrayList;
import java.util.List;

/* the class for options menu */
public class OptionsMenu{
	
	public List<MenuItem> menuItems = null;
	/* the activity which this options menu belong to */
	private String belongActivityName = null;
	private int optionsMenuID;
	
	//reserved 
	//public String belongFragment = null;
	
	/* constructor */
	public OptionsMenu(){
		this.menuItems = new ArrayList<MenuItem>();
	}
	
	/* add items into the options menu */
	public void addMenuItems(MenuItem item){
		this.menuItems.add(item);
	}
	public void setBelongActivity(String activityName){
		this.belongActivityName = activityName;
	}
	public void setOptionsMenuID(int menuID){
		this.optionsMenuID = menuID;
	}
	
	/**
	 * get the belong activity
	 */
	public String getBelongActivity(){
		return this.belongActivityName;
	}
	
	/* output the items of the options menu */
	public String toString(){
		String str = "";
		for(MenuItem item: menuItems){
			str += item.toString() + "  " + belongActivityName + "\n";
		}
		return str;
	}
}