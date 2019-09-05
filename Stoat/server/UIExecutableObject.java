/** the UIExecutableObject class
  * the executable object
*/
public class UIExecutableObject{
	
	/** cumulative indext string */
	private String cis;
	private String package_name;
	private String class_name;
	
	private boolean clickable;
	private boolean long_clickable;
	private boolean scrollable;
	private boolean checkable;
	
	private String text;
	private String resource_id;
	private String content_desc;
	private String index; //not very accurate, the last resort
	
	private int instance; //UI instance
	
	
	public UIExecutableObject(String cis, String pkg, String cls){
		this.cis = cis;
		this.package_name = pkg;
		this.class_name = cls;
	}
	
	public void setEventProperty(boolean clickable, boolean long_clickable, boolean scrollable, boolean checkable){
		this.clickable =clickable;
		this.long_clickable =long_clickable;
		this.scrollable =scrollable;
		this.checkable =checkable;
	}
	
	public void setInstance(int instance){
		this.instance = instance;
	}
	public int getInstance(){
		return this.instance;
	}
	
	/**
	 * check whether the text has multiple lines
	 * @return
	 */
	public boolean hasMultiLinesText(){
		if(this.text.contains("\n"))
			return true;
		else 
			return false;
	}
	
	/**
	 * get the first line when the text has multiple lines
	 * @return
	 */
	public String getFirstLineText(){
		int return_symbol_loc = this.text.indexOf("\n");
		return this.text.substring(0, return_symbol_loc);
	}
	
	public void setClickable(boolean clickable){
		this.clickable = clickable;
	}
	
	public void setLongClickable(boolean long_clickable){
		this.long_clickable = long_clickable;
	}
	
	public void setScrollable(boolean scrollable){
		this.scrollable = scrollable;
	}
	
	public void setCheckable(boolean checkable){
		this.checkable = checkable;
	}
	
	public void setIndex(String index){
		this.index = index;
	}
	
	public void setText(String text){
		this.text = text;
	}
	public String getText(){
		return text;
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
	
	public void setResourceId(String resource_id){
		this.resource_id = resource_id;
	}
	public String getResourceId(){
		return resource_id;
	}
	
	public void setContentDesc(String desc){
		this.content_desc = desc;
	}
	public String getContentDesc(){
		return content_desc;
	}
	
	
	/** check the executable properties */
	public boolean isClickable(){
		return clickable;
	}
	public boolean isLongClickable(){
		return long_clickable;
	}
	public boolean isCheckable(){
		return checkable;
	}
	public boolean isScrollable(){
		return scrollable;
	}
	/********/
	
	
	public String toString(){
		return this.cis + " " + this.package_name + " " + this.class_name + " "
				+ this.clickable + " " + this.long_clickable + " " + this.scrollable + " "
				+ this.checkable + " " + this.text + " " + this.index; 
	}
	
}