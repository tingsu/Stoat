import java.util.ArrayList;
import java.util.List;

/* the context menu class */
public class ContextMenu{
	
	public List<MenuItem> menuItems = null;
	/* the view which this context menu belong to */
	private int belongViewId;
	
	/* constructor */
	public ContextMenu(){
		this.menuItems = new ArrayList<MenuItem>();
	}
	
	/* add items into the options menu */
	public void addMenuItems(MenuItem item){
		this.menuItems.add(item);
	}
	/* setter */
	public void setBelongViewID(int viewID){
		this.belongViewId = viewID;
	}
	
	/* output the items of the options menu */
	public String toString(){
		String str = "";
		for(MenuItem item: menuItems){
			str += item.toString() + "  " + belongViewId + "\n";
		}
		return str;
	}
	
}