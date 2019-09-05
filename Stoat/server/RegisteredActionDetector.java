import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.EnhancedUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;


public class RegisteredActionDetector{
	
	private final static String DEBUG_STRING = "[RegisterAction] ";
	
	/* class instance, singleton pattern */
	private static RegisteredActionDetector detector = new RegisteredActionDetector();
	
	/* the detected registered actions list */
	public List<RegisteredAction> registeredActionList = null;
	
	/* the registered action listeners supported by the Android Framework */
	public final String[] registeredActionListeners={
		"setOnClickListener",  /* click */
		"setOnLongClickListener",  /* long click */
		
		"setOnCreateContextMenuListener", /* context menu */
		
		"setOnDragListener",   /* drag */
		"setOnKeyListener",  /* key event */
		"setOnTouchListener" /* touch event */
		
	};
	/* the registered action methods supported by the Android Framework */
	private final String[] registeredActionMethods = {
			"registerForContextMenu" /* register context menu, it will call "setOnCreateContextMenuListener" */
	};
	
	/* get action type */
	private String getActionType(String listenerName){
		if(listenerName.equals(registeredActionListeners[0])){
			return Action.click;
		}else if(listenerName.equals(registeredActionListeners[1])){
			return Action.longClick;
		}else if(listenerName.equals(registeredActionListeners[2])){
			return Action.longClick;
		}else if(listenerName.equals(registeredActionListeners[3])){
			return "drag";
		}else if(listenerName.equals(registeredActionListeners[4])){
			return "key_event";
		}else if(listenerName.equals(registeredActionListeners[5])){
			return "touch";
		}else if(listenerName.equals(registeredActionMethods[0])){  /* context menu */
			return Action.longClick;
		}else{
			return "unknown_event";
		}
	}
	
	/* private constructor */
	private RegisteredActionDetector(){
		registeredActionList = new ArrayList<RegisteredAction>();
	}
	/* return the class instance */
	public static RegisteredActionDetector getDetector(){
		return detector;
	}
	
	/* is it a registered action listener ? */
	public boolean isRegisteredActionListener(String methodName){
		
		/* iterate all register action listeners */
		for(String listener: registeredActionListeners){
			/* is it a registered action listener ? */
			if(methodName.equals(listener)){
				/* yes */
				System.out.println(DEBUG_STRING + "I: this is a registered action listener: " + methodName);
				return true;
			}
			else
				continue;
		}
		/* no */
		return false;
	}
	
	/* is it a registered action method ? */
	public boolean isRegisteredActionMethod(String methodName){
		for(String mName: registeredActionMethods){
			if(mName.equals(methodName)){
				/* yes */
				System.out.println("[Android Analysis] this is a registered action method: " + methodName);
				return true;
			}
			else
				continue;
		}
		/* no */
		return false;
	}
	
	/* add registered action */
	public void addRegisteredAction(RegisteredAction ra){
		registeredActionList.add(ra);
	}
	
	
	/* is the $unit a registered action listener in this $method ? */
	public void isRegisteredActionListener(SootMethod method, Unit unit){
		
		if( unit instanceof InvokeStmt){
			InvokeExpr ie = ((InvokeStmt)unit).getInvokeExpr();
			if( ie instanceof VirtualInvokeExpr ){
				String invokedFunctionName = ie.getMethod().getName();
				/* is it a registered action listener ? */
				if(isRegisteredActionListener(invokedFunctionName)){
					System.out.println(DEBUG_STRING + "I: listener detected: " + invokedFunctionName);
					RegisteredAction ra = new RegisteredAction();
					
					/* get the action listener line number
					 * we have a null-exception here ??
					 *  */
					/*
					 * get the line number of the call site through "LineNumberTag" and specify the option "-keep-line-number" in the command line
					 * some trouble shooting links:
					 * 1. https://groups.google.com/forum/#!topic/soot-list/feRTxuD7ZCQ
					 * 2. http://www.javaprogrammingforums.com/whats-wrong-my-code/4491-java-soot-issue-counting-jimple-line-numbers.html
					 * 3. https://github.com/Sable/soot/issues/169
					 * 4. https://github.com/Sable/soot/issues/91
					 */
					//SourceLineNumberTag tag = (SourceLineNumberTag)unit.getTag("SourceLineNumberTag");
					//System.out.println("at line: " + tag.getLineNumber());
					//ra.setRegisteredActionLine(tag.getLineNumber());
					
					ra.setActionType(detector.getActionType(invokedFunctionName));
					ra.setCallingMethodName(method.getName());
					ra.setActivityName(method.getDeclaringClass().getName());
					System.out.println(DEBUG_STRING + "I: this listener is located in the class: " + method.getDeclaringClass().getName());
					
					/* get the invoking object */
					Value baseValue = ((VirtualInvokeExpr) ie).getBase();
					System.out.println(DEBUG_STRING + "I: the variable : " + baseValue.toString() + " registered this listener");
					/* is it a local variable ?*/
					if( baseValue instanceof Local){
						
						/* build the unit graph */
						Body mbody = method.retrieveActiveBody();
						UnitGraph unitGraph = new EnhancedUnitGraph(mbody);
						SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(unitGraph);
						
						/* find the action ID from this current method */
						int viewID = findActionIDFromCurrentMethod(simpleLocalDefs, unit, (Local)baseValue);
						ra.setViewID(viewID);
						String viewText = ResUtility.getResParser().getViewText(viewID);
						ra.setViewText(viewText);
						detector.addRegisteredAction(ra);
					}else{
						System.out.println("[Android Analysis] WARNING action missed ??");
					}
				/* is it a registered action method ? */
				}else if(isRegisteredActionMethod(invokedFunctionName)){
					System.out.println("method detected: " + invokedFunctionName);
					RegisteredAction ra = new RegisteredAction();
					
					ra.setActionType(detector.getActionType(invokedFunctionName));
					ra.setCallingMethodName(method.getName());
					ra.setActivityName(method.getDeclaringClass().getName());
					System.out.println("in " + method.getDeclaringClass().getName());
					
					/* get the invoking object */
					Value argValue = ((VirtualInvokeExpr) ie).getArg(0);
					System.out.println("the invoking object: " + argValue.toString());
					
					/* is it a local variable ?*/
					if( argValue instanceof Local){
						
						/* build the unit graph */
						Body mbody = method.retrieveActiveBody();
						UnitGraph unitGraph = new EnhancedUnitGraph(mbody);
						SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(unitGraph);
						
						/* find the action ID from this current method */
						int viewID = findActionIDFromCurrentMethod(simpleLocalDefs, unit, (Local)argValue);
						ra.setViewID(viewID);
						String viewText = ResUtility.getResParser().getViewText(viewID);
						ra.setViewText(viewText);
						detector.addRegisteredAction(ra);
						
					}else{
						System.out.println(DEBUG_STRING + "I: WARNING action missed ??");
					}
					
				}else{
				/* other statements */
				}
			}
		}
		
	}
	
	
	/* find the action ID from the method $method, start from the unit $unit w.r.t the used variable $local */
	public int findActionIDFromCurrentMethod(SimpleLocalDefs simpleLocalDefs, Unit unit, Local local){
		
		System.out.println(DEBUG_STRING + "I: search defs for this var: " + local.getName() + ", type: " + local.getType().toString());
		
		int widgetID = 0;
		/* l: the target local variable, u: the unit where the local var locates
		 * the link for getting a def-use chain: 
		 * 1. http://devanla.com/soot-cfg-def-use-chain.html
		 * 2. https://mailman.cs.mcgill.ca/pipermail/soot-list/2006-December/000909.html
		 * */
		List<Unit> defUnits = simpleLocalDefs.getDefsOfAt(local, unit);
		/* we assume there should only one reachable def unit */
		assert(defUnits.size() == 1);
		
		Unit def_unit = defUnits.get(0);
		System.out.println(DEBUG_STRING + "I: found, the def stmt: " + def_unit.toString());
		
		if(def_unit instanceof AssignStmt){
			
			Value rightOp = ((AssignStmt)def_unit).getRightOp();
			if(rightOp instanceof CastExpr){ //$r1=(type)$r0
				//System.out.println("yes1....");
				/* get the used local variable of the def statement */
				Local usedLocalVar = getUsedLocalFromAssignStmt(def_unit);
				assert(usedLocalVar != null);
				/* recursive call */
				widgetID = findActionIDFromCurrentMethod(simpleLocalDefs, def_unit, usedLocalVar);
			}
			else if(rightOp instanceof VirtualInvokeExpr){
				//System.out.println("yes2....");
				SootMethod invoke_method = ((VirtualInvokeExpr) rightOp).getMethod();
				System.out.println(DEBUG_STRING + "I: the invoked virtual method: " + invoke_method.getName());
				/* is it the invoked virtual method to find a widget */
				if(invoke_method.getName().equals("findViewById")){
					Value v = ((VirtualInvokeExpr) rightOp).getArg(0);
					String viewID = Integer.toHexString(Integer.parseInt(v.toString()));
					System.out.println(DEBUG_STRING + "the associated view componnet id: " + viewID);
					return (int)(Integer.parseInt(v.toString()));
				}
			}else if(rightOp instanceof FieldRef){   //$r1 = $r0.x
				System.out.println(DEBUG_STRING + "I: FieldRef: " + rightOp.getType().toString());
				SootField fieldVar = ((FieldRef)rightOp).getField();
				widgetID = findActionIDFromOtherMethod(fieldVar);
			}
		}
		return widgetID;
	}
	
	/* find action ID from other methods */
	public int findActionIDFromOtherMethod(SootField field){
		
		int widgetID = 0;
		/* try to find action ID from the <init> method first */
		widgetID = findActionIDFromInitMethod(field);
		if( 0 == widgetID ){
			/* try to find action ID from those remaining methods, last try ! */
			widgetID = findActionIDFromNonInitMethod(field);
		}
		return widgetID;
	}
	
	/* find action ID from the <init> method */
	public int findActionIDFromInitMethod(SootField field){
	
		int actionID = 0;
		SootClass mClass = field.getDeclaringClass();
		System.out.println("try to find the action ID from the <init> function of the class " + mClass.getName());
		/* get the method body of <init> */ 
		Body body = mClass.getMethodByName("<init>").retrieveActiveBody();
		PatchingChain<Unit> units = body.getUnits();
		Iterator<Unit> unitIter = units.iterator();
		while(unitIter.hasNext()){
			Unit unit = unitIter.next();
			if(unit instanceof AssignStmt){ // find this form: $0.bt = $1
				Value leftOp = ((AssignStmt)unit).getLeftOp();
				if(leftOp instanceof FieldRef){
					SootField fieldFromLeftOp = ((FieldRef)leftOp).getField();
					/* are they the same class field ? */
					if(fieldFromLeftOp.equals(field)){  
						System.out.println("find it!!");
						System.out.println("the def stmt: " + unit.toString());
						
						/* build the unit graph */
						UnitGraph unitGraph = new EnhancedUnitGraph(body);
						SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(unitGraph);
						/* get the rightOp */
						Value v = ((AssignStmt)unit).getRightOp();
						System.out.println("rightOp type: " + v.getType().toString());
						if(v instanceof Local){
							/* start the RD analysis */
							actionID = findActionIDFromCurrentMethod(simpleLocalDefs, unit, (Local)v);
							/* only one widget initialization statement can appear in <init>, stop when we find the action ID */
							break;
						}else
						/* a null type? */
						if(v.getType().toString().equals("null_type")){
							/* the widget is not initialized in <init> */
							System.out.println("the widget is not initialized in <init> ? " + v.getType().toString());
							actionID = 0;
							break;
						}
					}
				}
			}
		}
		
		System.out.println("actionID: " + actionID);
		return actionID;
	}
	
	public int findActionIDFromNonInitMethod(SootField field){
		
		int actionID = 0;
		SootClass mClass = field.getDeclaringClass();
		System.out.println("try to find the action ID from those remaining methods of the class " + mClass.getName());
		List<SootMethod> methods = mClass.getMethods();
		Iterator<SootMethod> methodIter = methods.iterator();
		while(methodIter.hasNext()){
			SootMethod sm = (SootMethod)methodIter.next();
			/* do not try <init> again */
			if(sm.getName().equals("<init>")){
				continue;
			}
			else{
				Body body = sm.retrieveActiveBody();
				PatchingChain<Unit> units = body.getUnits();
				Iterator<Unit> unitIter = units.iterator();
				while(unitIter.hasNext()){
					Unit unit = unitIter.next();
					if(unit instanceof AssignStmt){ // find this form: $0.bt = $1
						Value leftOp = ((AssignStmt)unit).getLeftOp();
						if(leftOp instanceof FieldRef){
							SootField fieldFromLeftOp = ((FieldRef)leftOp).getField();
							/* are they the same class field ? */
							if(fieldFromLeftOp.equals(field)){  
								System.out.println("find it!!");
								System.out.println("the def stmt: " + unit.toString());
								
								/* build the unit graph */
								UnitGraph unitGraph = new EnhancedUnitGraph(body);
								SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(unitGraph);
								/* get the rightOp */
								Value v = ((AssignStmt)unit).getRightOp();
								System.out.println("rightOp type: " + v.getType().toString());
								if(v instanceof Local){
									/* start the RD analysis */
									actionID = findActionIDFromCurrentMethod(simpleLocalDefs, unit, (Local)v);
									/* only one widget initialization statement can appear in <init>, stop when we find the action ID */
									break;
								}
							}
						}
					}
				}
				if(actionID !=0 )
					break;
			}
		}
		
		System.out.println("actionID: " + actionID);
		return actionID;
	}
	
	/* output the detected registered actions */
	public void outputRegisteredActions(){
		if(detector.registeredActionList.size() == 0)
			return;
		
		System.out.println("---------------------");
		System.out.println("Detected Registered Actions: ");
		System.out.println("ActionID	ActionType 		ListnerLine		CallingMethod	activityName");
		System.out.println("---------------------");
		Iterator<RegisteredAction> i = detector.registeredActionList.iterator();
		while(i.hasNext()){
			RegisteredAction ra = (RegisteredAction)i.next();
			System.out.println(ra.toString());
		}
	}
	
	public void writeRegisteredActions(String testDir){
		if(detector.registeredActionList.size() == 0)
			return;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(testDir + "/registeredActions.txt", "UTF-8");
		} catch (IOException e) {
			System.out.println("can not open the file");
			e.printStackTrace();
		}
		writer.println("---------------------");
		writer.println("Detected Registered Actions: ");
		writer.println("ActionID	ActionType 		ListnerLine		CallingMethod	activityName");
		writer.println("---------------------");
		Iterator<RegisteredAction> i = detector.registeredActionList.iterator();
		while(i.hasNext()){
			RegisteredAction ra = (RegisteredAction)i.next();
			writer.println(ra.toString());
		}
		writer.close();
	}
	
	
	private Local getUsedLocalFromAssignStmt(Unit unit){
		
		System.out.println(DEBUG_STRING + "I: it is an assignment statement.");
		/* get the locals of the rightop of this def unit */
		List<ValueBox> usedVars = unit.getUseBoxes();
		/* we assume there should be only one use var on the rightop */
		assert(usedVars.size()==1);
		Iterator<ValueBox> iter = usedVars.iterator();
		while(iter.hasNext()){
			ValueBox vbox = (ValueBox) iter.next();
			if( vbox.getValue() instanceof Local){
				Local usedLocalVar = (Local)vbox.getValue();
				return usedLocalVar;
			}
		}
		return null;
	}
	
}