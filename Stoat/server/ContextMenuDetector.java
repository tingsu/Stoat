import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import soot.SootMethod;

/* the context menu detector class*/

public class ContextMenuDetector{
	
	private static ContextMenuDetector detector = new ContextMenuDetector();
	public List<ContextMenu> contextMenus = null;
	
	/* constructor */
	private ContextMenuDetector(){
		contextMenus = new ArrayList<ContextMenu>();
	}
	public static ContextMenuDetector getDetector(){
		return detector;
	}
	/* add a context menu into the list */
	public void addContextMenu(ContextMenu contextMenu){
		contextMenus.add(contextMenu);
	}
	
	/* detect context menu by inflate
	 *  android.view.MenuInflater: MenuInflater.inflate(int menuRes, Menu menu) 
	 * */
	public boolean detectContextMenuByMenuInflate(SootMethod onCreateContextMenuMethod){
		
		/* is MenuInflater.inflate(...) is called in this method ? */
		int menuResID = AndroidUtility.hasMenuInfate(onCreateContextMenuMethod);
		if(menuResID == 0){
			System.out.println("No MenuInflate found!");
			return false;
		}
		
		/* find the menu xml file according to $menuResID */
		ResPublic publicRes = ResUtility.getResParser().getPublicResourceByID(menuResID);
		menuResClassName = publicRes.getType();
		menuResFileName = publicRes.getName();
		if(menuResClassName == null || menuResFileName == null){
			System.out.println("Context Menu Resource File Not Found ?? ");
			return false;
		}
		
		/* generate the target resource file path */
		String targetFilePath = AndroidAppAnalysis.mAppFolder + "/res/" + menuResClassName + "/" + menuResFileName + ".xml";
		System.out.println("[Android Analysis] the context menu resource file locates at " + targetFilePath);
		parseContextMenuItems(targetFilePath);
		
		return true;
	}
	
	private String menuResClassName = null;
	private String menuResFileName = null;
	private int menuBelongViewId;
	
	/* parse the menu resource file and collects all menu items 
	 * Although a menu resource file has <menu>, <group> and <item> tags,
	 * Since only menu items are invokable options, we only need to parse the <item> tags.
	 * parse ContextMenu, see this link: http://developer.android.com/guide/topics/resources/menu-resource.html
	 * */
	private void parseContextMenuItems(String fileName){
		File file = new File(fileName);
		if(! file.exists() ){
			System.out.println("menu resource file does not exist ??");
			System.exit(0);
		}
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            Document menu = docBuilder.parse(file);
            
            NodeList menuItemNodes = menu.getElementsByTagName("item");
            System.out.println("[Android Analysis] menu items count: " + menuItemNodes.getLength());
            
            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setBelongViewID(menuBelongViewId);
            
            for (int i = 0; i < menuItemNodes.getLength(); i++) {
            	Node menuItem = menuItemNodes.item(i);
            	/* get item id "@+id/item1" and get item title "@string/item1" */
            	String itemIDString = menuItem.getAttributes().getNamedItem("android:id").getNodeValue();
            	String itemTitle = menuItem.getAttributes().getNamedItem("android:title").getNodeValue();
            	
            	System.out.println("context menu, itemID: " + itemIDString + "itemTitle: " + itemTitle);
            	int parsedItemID = ResUtility.getResParser().parseMenuItemID(itemIDString);
            	String parsedItemTitle = ResUtility.getResParser().parseMenuItemTitle(itemTitle);
            	
            	/* create an item and add it into the corresponding options menu */
            	MenuItem mi = new MenuItem();
            	mi.setViewID(parsedItemID);
            	mi.setViewText(parsedItemTitle);
            	/* add the menu item into the options menu */
            	contextMenu.addMenuItems(mi);
            }
            
            /* add the options menu into the menu list */
            addContextMenu(contextMenu);
            
		}catch (Exception err) {
			System.out.println("[Android Analysis] Error in scanning menu resource files !");
			err.printStackTrace();
		}
	}
	
	
	
	/* output options menus */
	public void outputContextMenus(){
		System.out.println("options menus:");
		for(ContextMenu contextMenu: contextMenus){
			System.out.println(contextMenu.toString());
		}
	}
	
	public void writeContextMenuItemActions(String testDir){
		if(contextMenus.size() == 0)
			return;
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(testDir + "/contextmenuitems.txt", "UTF-8");
		} catch (IOException e) {
			System.out.println("can not open the file");
			e.printStackTrace();
		}
		writer.println("---------------------");
		writer.println("Detected OptionsMenu Actions: ");
		writer.println("---------------------");
		Iterator<ContextMenu> i = contextMenus.iterator();
		while(i.hasNext()){
			ContextMenu cm = (ContextMenu)i.next();
			writer.println(cm.toString());
		}
		writer.close();
	}
}
