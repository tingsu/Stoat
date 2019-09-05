import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** the UIPage class
 *  It contains UI layout objects and UI executable objects
 */

public class UIPage{
	
	public List<UILayoutObject> ui_layout_objects = new ArrayList<UILayoutObject>();
	public List<UIExecutableObject> ui_executable_objects = new ArrayList<UIExecutableObject>();
	
	private final String debug = "[UIPage] ";
	
	/** whether the UI page has ListViews */
	private boolean hasListViews = false;
	private final String ListViewClassName = "android.widget.ListView";
	
	/** Is the UI page scrollable */
	private boolean isScrollable = false;
	public boolean isScrollable(){
		return this.isScrollable;
	}
	/** the scroll view's cis */
	private String scrollViewCIS; 
	public String getScrollViewCIS(){
		if(isScrollable())
			return scrollViewCIS;
		else
			return null;
	}
	
	private int textViewCnt;
	private int buttonCnt;
	private int imageViewCnt;
	private int imageButtonCnt;
	private int toggleButtonCnt;
	private int editTextCnt;
	private int checkBoxCnt;
	private int radioButtonCnt;
	private int checkedTextViewCnt;
	private int seekbarCnt;
	
	
	//TODO this list is used in the recursive function, should be initialized outside!!
	private ArrayList<Integer> index_list;
	
	/** constructor */
	public UIPage(){
		ui_layout_objects = new ArrayList<UILayoutObject>();
		ui_executable_objects = new ArrayList<UIExecutableObject>();
		index_list = new ArrayList<Integer>();
		
		textViewCnt = -1;
		buttonCnt = -1;
		imageViewCnt = -1;
		toggleButtonCnt = -1;
		editTextCnt = -1;
		checkBoxCnt = -1;
		radioButtonCnt = -1;
		checkedTextViewCnt = -1;
		seekbarCnt = -1;
	}
	
	/** getter */
	public List<UILayoutObject> getUILayoutObjects(){
		return ui_layout_objects;
	}
	public List<UIExecutableObject> getExecutableObjects(){
		return ui_executable_objects;
	}
	
	private String getCIS(ArrayList<Integer> index_list){
		String output = "";
		for(Integer i: index_list){
			output += i.toString();
		}
		return output;
	}
	
	/** get the instance of a view
	 * the difference between index and instance: http://www.cnblogs.com/lovexiaov/p/uiauto_lovexiaov.html 
	 * */
	private int getViewInstance(String className){
		if(className.equals("android.widget.TextView")){
			textViewCnt ++;
			return textViewCnt;
		}else if(className.equals("android.widget.Button")){
			buttonCnt ++;
			System.out.println("instance = " + buttonCnt);
			return buttonCnt;
		}else if(className.equals("android.widget.ImageView")){
			imageButtonCnt ++;
			return imageButtonCnt;
		}else if(className.equals("android.widget.ToggleButton")){
			toggleButtonCnt ++;
			return toggleButtonCnt;
		}else if(className.equals("android.widget.EditText")){
			editTextCnt ++;
			return editTextCnt;
		}else if(className.equals("android.widget.CheckBox")){
			checkBoxCnt ++;
			return checkBoxCnt;
		}else if(className.equals("android.widget.RadioButton")){
			radioButtonCnt ++;
			return radioButtonCnt;
		}else if(className.equals("android.widget.CheckedTextView")){
			checkedTextViewCnt ++;
			return checkedTextViewCnt;
		}else if(className.equals("android.widget.SeekBar")){
			seekbarCnt ++;
			return seekbarCnt;
		}else{
			System.out.println(debug + "the widget type is not handled??");
			//do nothing
			return 0;
		}
	}
	
	/** analyze the UI xml nodes */
	public void analyzeNode(NodeList node_list, boolean parent_clickable, boolean parent_long_clickable, 
			boolean parent_checkable, boolean parent_scrollable){
		
		int node_cnt = node_list.getLength();
		
		for(int i=0; i<node_cnt; i++){
			
			Node n = node_list.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){		
				
				System.out.println(n.getNodeName() + "   " + "[open]");
				
				//only consider the element named "node", do not consider the root element "hierarchy"
				if(n.hasAttributes()){ 
					NamedNodeMap nMap = n.getAttributes();
					
					// find the value of "index"
					Node attr_node = nMap.getNamedItem("index");
					if(attr_node != null){
						
						// add the value of "index"
						index_list.add(Integer.parseInt(attr_node.getNodeValue()));
					}else{
						System.out.println("E: has not find \"index\" attribute ??");
						System.exit(0);
					}
					
					if(n.hasChildNodes()){ //non-leaf nodes -> ui layout object
						
						String clickable = null;
						String long_clickable = null;
						String checkable = null;
						String scrollable = null;
						
						try{
							
							String cis = getCIS(index_list);
							attr_node = nMap.getNamedItem("package");
							String package_name = attr_node.getNodeValue();
							attr_node = nMap.getNamedItem("class");
							String class_name = attr_node.getNodeValue();
							
							// get the actionable properties
							attr_node = nMap.getNamedItem("clickable");
							clickable = attr_node.getNodeValue();
							attr_node = nMap.getNamedItem("long-clickable");
							long_clickable = attr_node.getNodeValue();
							attr_node = nMap.getNamedItem("checkable");
							checkable = attr_node.getNodeValue();
							
							attr_node = nMap.getNamedItem("scrollable");
							scrollable = attr_node.getNodeValue();
							
							
							System.out.println("non-leaf node ok!!");
							
							UILayoutObject lo = new UILayoutObject(cis, package_name, class_name);
							
							// is it a ListView?
							if(class_name.equals(ListViewClassName)){ 
								lo.isListView(true);
							}else{
								lo.isListView(false);
							}
							
							if(Boolean.parseBoolean(scrollable)==true){
								System.out.println("[UIPage]: this page is scrollable.");
								this.isScrollable = true;
								this.scrollViewCIS = cis;
							}
							
							ui_layout_objects.add(lo);
							
						}catch(NullPointerException e){ 
							//if we fail to find the specified attribute, attr_node will be null
							System.out.println("E: has not find the specified attribute ??");
							System.exit(0);
						}
						
						// depth-first search on the UI tree
						analyzeNode(n.getChildNodes(), //if its parent has a true property, pass it on to its child 
								(Boolean.parseBoolean(clickable) || parent_clickable), 
								(Boolean.parseBoolean(long_clickable) || parent_long_clickable), 
								(Boolean.parseBoolean(checkable) || parent_checkable), 
								(Boolean.parseBoolean(scrollable) || parent_scrollable) );
						
					}else{ //leaf nodes --> ui executable object
						
						try{
							
							String cis = getCIS(index_list);
							attr_node = nMap.getNamedItem("package");
							String package_name = attr_node.getNodeValue();
							attr_node = nMap.getNamedItem("class");
							String class_name = attr_node.getNodeValue();
							
							attr_node = nMap.getNamedItem("clickable");
							String clickable = attr_node.getNodeValue();
							attr_node = nMap.getNamedItem("long-clickable");
							String long_clickable = attr_node.getNodeValue();
							attr_node = nMap.getNamedItem("checkable");
							String checkable = attr_node.getNodeValue();
							attr_node = nMap.getNamedItem("scrollable");
							String scrollable = attr_node.getNodeValue();
							
							attr_node = nMap.getNamedItem("text");
							String text = attr_node.getNodeValue();
//							System.out.println("---- leaf node text = " + text);
//							if(text.contains("\n"))
//								System.out.println("it contains a return ");
							attr_node = nMap.getNamedItem("resource-id");
							String resource_id = attr_node.getNodeValue();
							attr_node = nMap.getNamedItem("content-desc");
							String content_desc = attr_node.getNodeValue();
							attr_node = nMap.getNamedItem("index");
							String index = attr_node.getNodeValue();
							
							System.out.println("leaf node ok!!");
							
							UIExecutableObject eo = new UIExecutableObject(cis, package_name, class_name);
							eo.setEventProperty(Boolean.parseBoolean(clickable), Boolean.parseBoolean(long_clickable), Boolean.parseBoolean(scrollable), Boolean.parseBoolean(checkable));
							eo.setText(text);
							eo.setResourceId(resource_id);
							eo.setContentDesc(content_desc);
							eo.setIndex(index);
							eo.setInstance(getViewInstance(class_name));
							
							
							//Note we need to check whether the parent node has some executable properties, if they are executable,
							//we will inherit this property. E.g., items in the ListView
							//TODO Here, we have only consider "clickable", "long_clickable", and not sure whether this is enough
							
							System.out.println("cis = " + cis);
							if(parent_long_clickable){
								System.out.println("parent node's long-clickable is true, set its own's long-clickable as true");
								eo.setLongClickable(true);
							}
									
							if(parent_clickable){
								System.out.println("parent node's clickable is true, set its own's clickable as true");
								eo.setClickable(true);
							}
							
							if(parent_checkable){
								System.out.println("parent node's checkable is true, set its own's checkable as true");
								eo.setCheckable(true);
							}
							
							
							ui_executable_objects.add(eo);
							
						}catch(NullPointerException e){ 
							//if we fail to find the specified attribute, attr_node will be null
							System.out.println("E: has not find the specified attribute ??");
							System.exit(0);
						}
						
						// remove the last element of index_list
						index_list.remove(index_list.size()-1);
					
						System.out.println(n.getNodeName() + "   " + "[close]");
					}
				}
				
			}
			
		}
		
	}
	
//	// test whether the UI xml file is parsed correctly, obsoleted
//	public static void testUIXML(){
//		
//		System.out.println("----ui layout objects -----");
//		for(UILayoutObject lo: ui_layout_objects){
//			System.out.println(lo.toString());
//		}
//		System.out.println("----ui executable objects -----");
//		for(UIExecutableObject eo: ui_executable_objects){
//			System.out.println(eo.toString());
//		}
//	}
//	
//  	public void main(String[] args){
//  		
//  		parseAppState_new(" "," "," ");
//  		testUIXML();
//  	}
}


