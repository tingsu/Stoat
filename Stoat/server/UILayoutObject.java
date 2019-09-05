/** the UILayoutObject class
  * the layout object, non-executable
*/
public class UILayoutObject{
	
	/** cumulative indext string */
	private String cis;
	private String package_name;
	private String class_name;
	
	/** Is the layout object is a ListView? */
	private boolean isListView;
	
	public UILayoutObject(String cis, String pkg, String cls){
		this.cis = cis;
		this.package_name = pkg;
		this.class_name = cls;
	}
	
	public void isListView(boolean isListView){
		this.isListView = isListView;
	}
	
	public String getCIS(){
		return cis;
	}
	public String getPackageName(){
		return package_name;
	}
	public String getClassName(){
		return class_name;
	}
	
	
	public String toString(){
		return this.cis + " " + this.package_name + " " + this.class_name + " " + this.isListView;
	}
	
}
