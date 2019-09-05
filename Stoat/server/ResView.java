//a view component in a layout file
public class ResView{
	
	//represent names instead of the actual values
	private String viewIDName;   //android:id
	private String viewTextName; //android:text
	
	private String viewTypeName;
	
	//constructors
	public ResView(){ }
	public ResView(String viewIDName, String viewTextName, String viewTypeName){
		this.viewIDName = viewIDName;
		this.viewTextName = viewTextName;
		this.viewTypeName = viewTypeName;
	}
	//getter
	public String getResViewIDName(){
		return this.viewIDName;
	}
	public String getResViewTextName(){
		return this.viewTextName;
	}
	public String getResViewTypeName(){
		return this.viewTypeName;
	}
	//setter
	public void setResViewIDName(String viewIDName){
		this.viewIDName = viewIDName;
	}
	public void setResViewTextName(String viewTextName){
		this.viewTextName = viewTextName;
	}
	public void setResViewTypeName(String viewTypeName){
		this.viewTypeName = viewTypeName;
	}
}