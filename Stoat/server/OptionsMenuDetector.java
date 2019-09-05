import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.util.Chain;

/* OptionsMenu Detector */
public class OptionsMenuDetector{
	
	/* the detector instance */
	private static OptionsMenuDetector detector = null;
	
	/* the options menu list */
	public List<OptionsMenu> optionsMenus = null;
	
	private void addOptionsMenus(OptionsMenu optionsMenu){
		optionsMenus.add(optionsMenu);
	}
	
	/* private constructor */
	private OptionsMenuDetector(){
		optionsMenus = new ArrayList<OptionsMenu>();
	}
	
	/**
	 * get the options menu detector
	 * @return the instance of the detector
	 */
	public static OptionsMenuDetector getDetector(){
		if(detector == null)
			detector = new OptionsMenuDetector();
		return detector;
	}
	
	/**
	 * check whether the activity has an options menu (an options menu is declared as an inherited action)
	 * @param activityName the activity name
	 * @return true or false
	 */
	public boolean hasOptionsMenu(String activityName){
		for(OptionsMenu om: optionsMenus){
			if(om.getBelongActivity().equals(activityName)){
				return true;
			}
		}
		return false;
	}
	
	/* detect options menu by inflate
	 *  android.view.MenuInflater: MenuInflater.inflate(int menuRes, Menu menu) 
	 * */
	public boolean detectOptionsMenuByMenuInflate(SootMethod onCreateOptionsMenuMethod){
		
		/* is MenuInflater.inflate(...) is called in this method ? */
		int menuResID = AndroidUtility.hasMenuInfate(onCreateOptionsMenuMethod);
		if(menuResID == 0){
			System.out.println("No MenuInflate found!");
			return false;
		}
		menuID = menuResID;
		menuBelongActivityName = onCreateOptionsMenuMethod.getDeclaringClass().toString();
		
		/* find the menu xml file according to $menuResID */
		ResPublic publicRes = ResUtility.getResParser().getPublicResourceByID(menuResID);
		menuResClassName = publicRes.getType();
		menuResFileName = publicRes.getName();
		if(menuResClassName == null || menuResFileName == null){
			System.out.println("Options Menu Resource File Not Found ?? ");
			return false;
		}
		
		/* generate the target resource file path */
		String targetFilePath = AndroidAppAnalysis.mAppFolder + "/res/" + menuResClassName + "/" + menuResFileName + ".xml";
		System.out.println("[Android Analysis] the options menu resource file locates at " + targetFilePath);
		parseOptionsMenuItems(targetFilePath);
		
		return true;
	}
	
	
	private String menuResClassName = null;
	private String menuResFileName = null;
	private String menuBelongActivityName = null;
	private int menuID=0;
	
	/* find the menu resource file 
	 * According to Android Documentation, menu resource files should be located in res/menu/filename.xml,
	 * if this assumption hold, then we can directly search in the $androidPackageName.R$menu class.
	 * But in the current implementation, instead, we search all resource classes,
	 * i.e., $androidPackageName.R$* in case that the app under analysis does not conform to this assumption.
	 * <obsolete>
	 * */
	private void findMenuResFile(int menuResID){
		
		/* get application classes */
		Chain<SootClass> app_classes = Scene.v().getApplicationClasses();
		Iterator<SootClass> class_iter = app_classes.iterator();
		while(class_iter.hasNext()){
			SootClass sootClass = (SootClass) class_iter.next();
			/* search all resource classes */
			String className = sootClass.getName();
			if(className.startsWith(AndroidAppAnalysis.appPackageName + ".R$")){
				try{
					SootMethod clinitMethod = sootClass.getMethodByName("<clinit>");
					Body body = clinitMethod.retrieveActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					Iterator<Unit> unitIter = units.iterator();
					while(unitIter.hasNext()){
						Unit unit = unitIter.next();
						if( unit instanceof AssignStmt){
							Value rightOp = ((AssignStmt) unit).getRightOp();
							int rightOpValue = (int)(Integer.parseInt(rightOp.toString()));
							/* check the ID */
							if(rightOpValue == menuResID){
								System.out.println("find the menu resource file!!!");
								Value leftOp = ((AssignStmt) unit).getLeftOp();
								menuResFileName = leftOp.toString();
								
								/* set the menu resource class name */
								int dollarLoc = className.indexOf('$');
								menuResClassName = className.substring(dollarLoc+1);
								
								break;
							}
						}
					}
					
				}catch(RuntimeException re){
					System.out.println("this class does not have the <clinit> method, runtime exeception caught! ");
				}
			}
		}
		return;
	}
	
	/* parse the menu resource file and collects all menu items 
	 * Although a menu resource file has <menu>, <group> and <item> tags,
	 * Since only menu items are invokable options, we only need to parse the <item> tags.
	 * parse OptionsMenu, see this link: http://developer.android.com/guide/topics/resources/menu-resource.html
	 * */
	private void parseOptionsMenuItems(String fileName){
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
            
            OptionsMenu optionsMenu = new OptionsMenu();
            optionsMenu.setBelongActivity(menuBelongActivityName);
            optionsMenu.setOptionsMenuID(menuID);
            
            for (int i = 0; i < menuItemNodes.getLength(); i++) {
            	Node menuItem = menuItemNodes.item(i);
            	/* get item id "@+id/item1" and get item title "@string/item1" */
            	String itemIDString = menuItem.getAttributes().getNamedItem("android:id").getNodeValue();
            	String itemTitle = menuItem.getAttributes().getNamedItem("android:title").getNodeValue();
            	
            	int parsedItemID = ResUtility.getResParser().parseMenuItemID(itemIDString);
            	String parsedItemTitle = ResUtility.getResParser().parseMenuItemTitle(itemTitle);
            	
            	/* create an item and add it into the corresponding options menu */
            	MenuItem mi = new MenuItem();
            	mi.setViewID(parsedItemID);
            	mi.setViewText(parsedItemTitle);
            	/* add the menu item into the options menu */
            	optionsMenu.addMenuItems(mi);
            }
            
            /* add the options menu into the menu list */
            addOptionsMenus(optionsMenu);
            
		}catch (Exception err) {
			System.out.println("[Android Analysis] Error in scanning menu resource files !");
			err.printStackTrace();
		}
	}
	
	
	/* output options menus */
	public void outputOptionsMenus(){
		System.out.println("options menus:");
		for(OptionsMenu optionsMenu: optionsMenus){
			System.out.println(optionsMenu.toString());
		}
	}
	
	public void writeOptionsMenuItemActions(String testDir){
		if(optionsMenus.size() == 0)
			return;
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(testDir + "/optionsmenuitems.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("can not open the file");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println("---------------------");
		writer.println("Detected OptionsMenu Actions: ");
		writer.println("---------------------");
		Iterator<OptionsMenu> i = optionsMenus.iterator();
		while(i.hasNext()){
			OptionsMenu om = (OptionsMenu)i.next();
			writer.println(om.toString());
		}
		writer.close();
	}
}