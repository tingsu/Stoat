import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.SootMethod;
import soot.tagkit.LineNumberTag;

public class InheritedActionDetector{
	
	private static InheritedActionDetector detector = new InheritedActionDetector();
	/* the detected inherited actions list */
	public List<InheritedAction> inheritedActionList = null;
	
	/* the inherited action override methods supported by the Android Framework */
	public final String[] inheritedActionOverrideMethods={
		"onCreateOptionsMenu",  /* option menu --> menu key or menu button? */
		"onCreateContextMenu", /* context menu --> long press */
	};
	
	private InheritedActionDetector(){
		inheritedActionList = new ArrayList<InheritedAction>();
	}
	public static InheritedActionDetector getDetector(){
		return detector;
	}
	
	/** add inherited action */
	public void addInheritedActions(InheritedAction ia){
		inheritedActionList.add(ia);
	}
	
	/** does the activity has options menu? */
	public boolean hasOptionsMenu(String activityName){
		if(inheritedActionList.size() == 0){
			return false;
		}else{
			for(InheritedAction action: inheritedActionList){
				if(action.getOverrideMethodName().equals(inheritedActionOverrideMethods[0]) &&
						action.getActivityName().equals(activityName)){
					//inheritedActionOverrideMethods[0] --> "onCreateOptionsMenu"
					return true;
				}
			}
		}
		return false;
	}
	
	public void outputInheritedActions(){
		if(inheritedActionList.size() == 0)
			return;
		
		System.out.println("---------------------");
		System.out.println("Detected Inherited Actions: ");
		System.out.println("ActionID	ActionName 		OverrideMethodLine		OverrideMethod		ActivityName");
		System.out.println("---------------------");
		Iterator<InheritedAction> i = inheritedActionList.iterator();
		while(i.hasNext()){
			InheritedAction ia = (InheritedAction)i.next();
			System.out.println(ia.toString());
		}
	}
	
	public void writeInheritedActions(String testDir){
		if(inheritedActionList.size() == 0)
			return;
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(testDir + "/inheritedActions.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("can not open the file");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println("---------------------");
		writer.println("Detected Inherited Actions: ");
		writer.println("ActionID	ActionName 		OverrideMethodLine		OverrideMethod		ActivityName");
		writer.println("---------------------");
		Iterator<InheritedAction> i = inheritedActionList.iterator();
		while(i.hasNext()){
			InheritedAction ia = (InheritedAction)i.next();
			writer.println(ia.toString());
		}
		
		/* write options and context menu items */
		OptionsMenuDetector.getDetector().writeOptionsMenuItemActions(testDir);
		ContextMenuDetector.getDetector().writeContextMenuItemActions(testDir);
		
		writer.close();
	}
	
	/* detect whether it is an inherited action override method */
	public void detectAction(SootMethod method){
		
		String methodName = method.getName();
		if(methodName.equals(inheritedActionOverrideMethods[0])){   //option menu
			System.out.println("[Android Analysis] \"onCreateOptionsMenu\" --> option menu ");
			InheritedAction ia = new InheritedAction();
			ia.setActionName("OptionsMenu");
			ia.setOverrideMethodName(methodName);
			//LineNumberTag tag = (LineNumberTag)method.getTag("LineNumberTag");
			//System.out.println("override method location: " + tag.getLineNumber());
			//ia.setActionOverrideMethodLine(tag.getLineNumber());
			ia.setActivityName(method.getDeclaringClass().toString());
			
			/* detect options menu by MenuInflater.inflate(...) */
			//TODO we temporarily do not analyze the menu items of option menus
			//System.out.println("[Android Analysis] Start to analyze options menu...");
			//OptionsMenuDetector.getDetector().detectOptionsMenuByMenuInflate(method);
			//OptionsMenuDetector.getDetector().outputOptionsMenus();
			
			/* add into the action list */
			addInheritedActions(ia);
		}else if(methodName.equals(inheritedActionOverrideMethods[1])){ //context menu
			System.out.println("[Android Analysis] \"onCreateContextMenu\" --> context menu ");
			InheritedAction ia = new InheritedAction();
			ia.setActionName("ContextMenu");
			ia.setOverrideMethodName(methodName);
			//LineNumberTag tag = (LineNumberTag)method.getTag("LineNumberTag");
			//System.out.println("override method location: " + tag.getLineNumber());
			//ia.setActionOverrideMethodLine(tag.getLineNumber());
			ia.setActivityName(method.getDeclaringClass().toString());
			
			/* detect context menu by MenuInflater.inflate(...) */
			//TODO we temporarily do not analyze the menu items of context menus
			//System.out.println("[Android Analysis] Start to analyze context menu...");
			//ContextMenuDetector.getDetector().detectContextMenuByMenuInflate(method);
			//ContextMenuDetector.getDetector().outputContextMenus();
			
			/* add into the action list */
			addInheritedActions(ia);
		}else{
			//System.out.println("[Android Analysis] this is not an inherited action method.  skip ....");
		}
		return;
	}
}