import java.util.ArrayList;

/* the class for view component */
public class ViewComponent {
   
    public ArrayList<ViewComponent> children;
    
    //view component types
    public static final String EditText = "android.widget.EditText";
    

    //basic properties
    private int id;
    private int locX; //left
    private int locY; //top
    private int width;
    private int height;
    private int measuredWidth;
    private int measuredHeight;
    private int scrollX ; //View.mScrollX;
    private int scrollY ; //View.mScrollY;
    //private int absoluteX = 0; //Absolute position on Screen;
    //private int absoluteY = 0; //Absolute position on Screen;
    //private float cameraDistance = 0; //z-axis value
    
    //the view index, which specifies the view location
    private int viewIndex;

    //additional properties
    private boolean visible = false;
    private long drawingTime = 0; //last time view is drawn
    private boolean isShown = false; //whether view is shown or not
    private boolean hasFocus = false;
    private boolean focusable = false;
    private boolean hasOnClickListener = false;

    private String viewType;
    private String textContent;
    private boolean isEditText = false;
    private boolean isInputMethodTarget = false;
    private boolean isContainer = false;
    private int inputMethod;

    transient private boolean isCollectionMember = false;
    transient private boolean isSpinner = false;

    //constructor
    public ViewComponent(){
    	
    }
    public ViewComponent(int id, int locX, int locY, int width, int height, int mWidth, int mHeight, int scrollX, int scrollY){
    	this.id = id;
    	this.locX = locX;
    	this.locY = locY;
    	this.width = width;
    	this.height = height;
    	this.measuredWidth = mWidth;
    	this.measuredHeight = mHeight;
    	this.scrollX = scrollX;
    	this.scrollY = scrollY;
    }

    /* setter */
    public void setViewID(int id){
    	this.id = id;
    }
    public void setLocX(int locX){
    	this.locX = locX;
    }
    public void setLocY(int locY){
    	this.locY = locY;
    }
    public void setWidth(int width){
    	this.width = width;
    }
    public void setHeight(int height){
    	this.height = height;
    }
    public void setMeasuredWidth(int mWidth){
    	this.measuredWidth = mWidth;
    }
    public void setMeasuredHeight(int mHeight){
    	this.measuredHeight = mHeight;
    }
    public void setViewType(String viewType){
    	this.viewType = viewType;
    }
    public void setTextContent(String textContent){
    	this.textContent = textContent;
    }
    public void setScrollX(int scrollX){
    	this.scrollX = scrollX;
    }
    public void setScrollY(int scrollY){
    	this.scrollY = scrollY;
    }
    public void setViewIndex(int index){
    	this.viewIndex = index;
    }
    
    /* getter */
    public int getViewID(){
    	return this.id;
    }
    public int getLocX(){
    	return this.locX;
    }
    public int getLocY(){
    	return this.locY;
    }
    public int getWidth(){
    	return this.width;
    }
    public int getHeight(){
    	return this.height;
    }
    public int getMeasuredWidth(){
    	return this.measuredWidth;
    }
    public int getMeasuredHeight(){
    	return this.measuredHeight;
    }
    public String getViewType(){
    	return this.viewType;
    }
    public String getTextContent(){
    	return this.textContent;
    }
    public int getScrollX(){
    	return this.scrollX;
    }
    public int getScrollY(){
    	return this.scrollY;
    }
    public int getViewIndex(){
    	return this.viewIndex;
    }
    
    /**
     *  TODO is this comparison is enough? 
     *  App State Abstraction: 
     *  	Text difference are abstracted away, i.e., are not considered in deciding state difference.
     *  Sometimes we note the "text" change of a view component may also its location X-Y, and as a result,
     *  the view component is treated as different. 
     *  */
    public boolean isEqual(ViewComponent vc){
    	//they have two different ids or types
    	if(this.id != vc.id || !this.viewType.equals(vc.viewType)){
    		return false;
    	}
    	if(this.locX != vc.locX || this.locY != vc.locY || 
    			this.width != vc.width || this.height != vc.height){
    		return false;
    	}
    	return true;
    }
    
    /* toString */
    public String toString(){
    	String viewInfo = "";
    	viewInfo += this.viewType + "\n";
    	viewInfo += this.id + " " + this.locX + " " + this.locY + " " + this.width +
    			    " " + this.height + " " + this.measuredWidth + " " + this.measuredHeight + 
    			    " " + this.scrollX + " " + this.scrollY;
    	if(this.viewType.contains(".TextView") || this.viewType.contains(".Button") 
    			|| this.viewType.contains(".EditText")){
    		viewInfo += " " + this.textContent + "\n";
    	}
    	else{
    		viewInfo += "\n";
    	}
    	return viewInfo;
    }
   
}