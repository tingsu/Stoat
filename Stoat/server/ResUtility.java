import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * we use apktool to reverse engineering resource files in an android app.
 * the basic layout of resource files:
 * 		res/ 
 * 			layout/
 * 			menu/
 * 			values/
 * 				ids.xml: all id values
 * 				public.xml: all resource values
 * 				strings.xml: all string values
 * ResUtility
 * 	 ResPublic
 * 	 ResStrings
 * 	 ResLayout
 * 	 ResMenu (?)
 */

/* the resource utility class */
public class ResUtility{
	/* the resource parser instance */
	private static ResUtility resParser = new ResUtility();
	
	/*  .../res/values/public.xml */
	private String resPublicXMLFile = null;
	private List<ResPublic> resPublicList = null;
	private void addPublicRes(ResPublic resPublic){
		resPublicList.add(resPublic);
	}
	
	/*  .../res/values/strings.xml */
	private String resStringsXMLFile = null;
	private List<ResStrings> resStringsList = null;
	private void addStringsRes(ResStrings resStrings){
		resStringsList.add(resStrings);
	}
	
	//layout resources
	private List<ResLayout> resLayoutList = null;
	private void addLayoutRes(ResLayout resLayout){
		resLayoutList.add(resLayout);
	}
	
	
	private ResUtility(){
		//init. list
		resPublicList = new ArrayList<ResPublic>();
		resStringsList = new ArrayList<ResStrings>();
		resLayoutList = new ArrayList<ResLayout>();
		//init. default file location
		resPublicXMLFile = AndroidAppAnalysis.mAppFolder + "/res/values/public.xml";
		resStringsXMLFile = AndroidAppAnalysis.mAppFolder + "/res/values/strings.xml";
		
		System.out.println("[Android Analysis] init resource files...");
		System.out.println(resPublicXMLFile);
		System.out.println(resStringsXMLFile);
		//parse resources
		parsePublicResources();
		parseStringsResources();
		parseLayoutResources();
	}
	
	public static ResUtility getResParser(){
		return resParser;
	}
	
	/* search for public resource id with $type and $name */
	public int getPublicResourceID(String resType, String resName){
		
		assert(resType != null && resName == null);
		
		int resID = 0;
		for(ResPublic res: resPublicList){
			if(res.getType().equals(resType) && res.getName().equals(resName)){
				resID = res.getID();
				break;
			}
		}
		return resID;
	}
	
	/* search the public resource by ID*/
	public ResPublic getPublicResourceByID(int resID){
		assert(resID != 0);
		ResPublic publicRes = null;
		for(ResPublic res: resPublicList){
			if(res.getID() == resID){
				/* call the copy constructor */
				publicRes = new ResPublic(res);
				break;
			}
		}
		return publicRes;
	}
	
	/* search for strings content with $name*/
	public String getStringValue(String itemName){
		assert(itemName != null);
		
		String title = null;
		for(ResStrings res: resStringsList){
			if(res.getName().equals(itemName)){
				title = res.getContent();
				break;
			}
		}
		return title;
	}
	
	/* get the view text name */
	public String getResViewTextName(String viewIDName){
		assert(viewIDName != null);
		String viewTextName = null;
		for(ResLayout layout: resLayoutList){
			for(ResView view: layout.viewList){
				if(view.getResViewIDName().equals(viewIDName)){
					viewTextName = view.getResViewTextName();
					break;
				}
			}
		}
		return viewTextName;
	}
	
	/* get the view text 
	 * $viewIdName: the view id name
	 * */
	public String getViewText(int viewID){
		ResPublic resPublic = getPublicResourceByID(viewID);
		String itemName = resPublic.getName();
		String viewTextName = getResViewTextName(itemName);
		String text = getStringValue(viewTextName);
		return text;
	}
	
	/* parse the public resources */
	private void parsePublicResources(){
		File file = new File(resPublicXMLFile);
		if(! file.exists()){
			System.out.println("the .../res/values/public.xml file does not exist ?");
			System.exit(0);
		}
		
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
	        Document resources = docBuilder.parse(file);

	        NodeList publicNodes = resources.getElementsByTagName("public");
	        System.out.println("[Android Analysis] resources count: " + publicNodes.getLength());
	        
	        for (int i = 0; i < publicNodes.getLength(); i++) {
	        	Node resNode = publicNodes.item(i);
	        	
	        	/* create a public resource and add it into the list */
	        	String type = resNode.getAttributes().getNamedItem("type").getNodeValue();
	        	String name = resNode.getAttributes().getNamedItem("name").getNodeValue();
	        	String idString = resNode.getAttributes().getNamedItem("id").getNodeValue();
	        	int id = Integer.parseInt(idString.substring(2), 16);
	        	ResPublic publicRes = new ResPublic(type, name, id);
	        	assert(resParser!=null);
	        	addPublicRes(publicRes);
	        }
		}catch (Exception err) {
			System.out.println("[Android Analysis] Error in scanning public.xml file !");
			err.printStackTrace();
		}
	}
	
	//parse strings resources
	private void parseStringsResources(){
		File file = new File(resStringsXMLFile);
		if(! file.exists()){
			System.out.println("the .../res/values/strings.xml file does not exist ?");
			System.exit(0);
		}
		
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
	        Document resources = docBuilder.parse(file);
	        
	        NodeList stringsNodes = resources.getElementsByTagName("string");
	        System.out.println("[Android Analysis] strings count: " + stringsNodes.getLength());
	        
	        for (int i = 0; i < stringsNodes.getLength(); i++) {
	        	Node stringsNode = stringsNodes.item(i);
	        	
	        	/* create a string resource and add it into the list */
	        	String name = stringsNode.getAttributes().getNamedItem("name").getNodeValue();
	        	String content = stringsNode.getTextContent();
	        	ResStrings stringsRes = new ResStrings(name, content);
	        	addStringsRes(stringsRes);
	        }
		}catch (Exception err) {
			System.out.println("[Android Analysis] Error in scanning strings.xml !");
			err.printStackTrace();
		}
	}
	
	//parse layout resources
	private void parseLayoutResources(){
		//check the public resources have been already computed
		if(resPublicList.size() == 0){
			System.out.println("[Android Analysis] Error: the public resources should be computed before layout resources"
					     + " <" + this.getClass().getName()+ ">.");
			System.exit(0);
		}
		
		for(ResPublic resPublic: resPublicList){
			//is it a layout?
			String resType = resPublic.getType();
			if(resType.contains("layout")){  
				//get the layout file name
				String resName = resPublic.getName();
				String layoutFileName = AndroidAppAnalysis.mAppFolder + "/res/" + resType + "/" + resName + ".xml";
				System.out.println("[Android Analysis] public resource, type: " + resType + " file location: " + layoutFileName);
				parseLayoutFile(layoutFileName);
			}
		}
		
		System.out.println("[Android Analysis] the total detected layouts: " + resLayoutList.size());
	}
	
	//parse layout file 
	private void parseLayoutFile(String layoutFileName){
		File layoutFile = new File(layoutFileName);
		if(! layoutFile.exists()){
			System.out.println("[Android Analysis] " + layoutFileName + " does not exist!" + " <" + this.getClass().getName()+ ">.");
			System.exit(0);
		}
		
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
	        Document resDoc = docBuilder.parse(layoutFile);
	        
	        NodeList nodes = resDoc.getElementsByTagName("*");
	        System.out.println("[Android Analysis] element count: " + nodes.getLength());
	        
	        ResLayout layout = new ResLayout(layoutFileName);
	        
	        for (int i = 0; i < nodes.getLength(); i++) {
	        	Node node = nodes.item(i);
	        	//only identify a view with an id
	        	if(node.getAttributes().getNamedItem("android:id") != null ){
	        		//create a resource view
	        		ResView view = new ResView();
	        		String idValue = node.getAttributes().getNamedItem("android:id").getNodeValue();
	        		int slashLoc = idValue.indexOf("/");
	        		//get the name of the view id
	        		String idName = idValue.substring(slashLoc+1);
	        		view.setResViewIDName(idName);
	        		//does it has "android:text"?
	        		if(node.getAttributes().getNamedItem("android:text")!=null){
	        			String textValue = node.getAttributes().getNamedItem("android:text").getNodeValue();
		        		int slashLoc2 = textValue.indexOf("/");
		        		//get the name of the view text
		        		String textName = textValue.substring(slashLoc2+1);
		        		view.setResViewTextName(textName);
	        		}else{
	        			view.setResViewTextName(null);
	        		}
	        		//get the name of the view type
	        		String typeName = node.getNodeName();
	        		view.setResViewTypeName(typeName);
	        		//add the view into the layout
	        		layout.addResView(view);
	        	}
	        }
	        //add the layout into the list
	        addLayoutRes(layout);
		}catch (Exception err) {
			System.out.println("[Android Analysis] Error in scanning strings.xml !");
			err.printStackTrace();
		}
	}
	
	/* output the parsing result */
	public void outputParsingResult(){
		System.out.println("public.xml: ");
		for(ResPublic publicRes: resPublicList){
			System.out.println(publicRes.toString());
		}
		System.out.println("strings.xml: ");
		for(ResStrings stringsRes: resStringsList){
			System.out.println(stringsRes.toString());
		}
		System.out.println("layout: ");
		for(ResLayout layoutRes: resLayoutList){
			for(ResView view: layoutRes.viewList){
				System.out.println("view type: " + view.getResViewTypeName() 
						+ "view id: " + view.getResViewIDName()
						+ "view text: " + view.getResViewTextName()
						);
			}
		}
	}
	

	/* parse the menu item ID */
	public int parseMenuItemID(String menuItemID){
		
		int slashLoc = menuItemID.indexOf('/');
		String idString = menuItemID.substring(slashLoc+1);
		
		return ResUtility.getResParser().getPublicResourceID("id", idString);
	}
	
	/* parse the menu item title */
	public String parseMenuItemTitle(String menuItemTitle){
		
		int slashLoc = menuItemTitle.indexOf('/');
		/* if the title is already given */
		if(slashLoc == -1) return menuItemTitle;
		
		String name = menuItemTitle.substring(slashLoc+1);
		
		return ResUtility.getResParser().getStringValue(name);
	}
}