import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.callgraph.Sources;
import soot.jimple.toolkits.callgraph.TransitiveTargets;
import soot.jimple.toolkits.callgraph.Units;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.EnhancedUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.util.*;

public class ActionsAnalysis extends SceneTransformer{
	
	private static final String DEBUG_STRING = "[Action Analysis] ";
	
	//private CallGraph mCallGraph = null;
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		
		System.out.println("[Android Analysis] SimpleAnalysis.internalTransform");
		
		// TODO Auto-generated method stub
		
		/* construct the call graph of the whole program */
		//mCallGraph = Scene.v().getCallGraph();
		outputAllAppMethods();
		
		/* find the inherited and registered actions */
		findInheritedAction();
		findRegisteredActions();
		
		/* output actions */
		InheritedActionDetector.getDetector().outputInheritedActions();
		RegisteredActionDetector.getDetector().outputRegisteredActions();
		InheritedActionDetector.getDetector().writeInheritedActions(ConfigOptions.APP_FSM_BUILDING_OUTPUT_DIR);
		RegisteredActionDetector.getDetector().writeRegisteredActions(ConfigOptions.APP_FSM_BUILDING_OUTPUT_DIR);
		
//		/* construct transitive targets */
//		TransitiveTargets trans = new TransitiveTargets(mCallGraph);
//		
//		/* get the entry points of the whole program */
//		List<SootMethod> entries = Scene.v().getEntryPoints();
//		
//		/* create the registered action detector */
//		RegisteredActionDetecter raDetecter = new RegisteredActionDetecter();
//		
//		for (SootMethod entry : entries) {
//			System.out.println("Processing entrypoint: " + entry.toString());
//			
//			/* get all transitive methods called by the entry point function */
//			Iterator<MethodOrMethodContext> targets = trans.iterator(entry);
//			
//			//List<MethodOrMethodContext> currentEntry = new ArrayList<MethodOrMethodContext>();
//			//currentEntry.add((MethodOrMethodContext)entry);
//			/* get all methods which are reachable from the entry function */
//			//ReachableMethods reachable = new ReachableMethods(mCallGraph, currentEntry);
//			//reachable.update();
//			
//			while (targets.hasNext()) {
//				
//				MethodOrMethodContext transitiveCalledMethod = (MethodOrMethodContext)targets.next();
//				System.out.println("the method transitively called by entrypoint and its context: "+ transitiveCalledMethod.method().getName());
//				
//				String methodName = transitiveCalledMethod.method().getName();
//				
//				
//				
//				if( raDetecter.isRegisteredActionListener(methodName) ){
//					
//					/* 
//					 * get the callers and the call sites 
//					 * Will it contain multiple callers or call sites for a method ?
//					 * */
//					Sources callers = new Sources(mCallGraph.edgesInto(transitiveCalledMethod));
//					Units callsites = new Units(mCallGraph.edgesInto(transitiveCalledMethod));
//					
//					while(callers.hasNext()){
//						
//						SootMethod caller_method = (SootMethod)callers.next();
//						System.out.println("the context: " + caller_method.context().getClass().getName() );
//						Unit callsite_unit = (Unit)callsites.next();
//						
//						System.out.println("calling method: " + caller_method.getName());
//						System.out.println("call site stmt: " + callsite_unit.toString());
//						
//						/* find the widget info */
//						RegisteredAction ra = findWidgetInfo(caller_method, callsite_unit);
//						raDetecter.addRegisteredAction(ra);
//					}
//				}
//				
//
//				//System.out.println("Possible source: " + current.toString());
//
//				//if (sourceAPIs.contains(current.toString())) {
//					//System.out.println("Uses source:\t" + current.toString());
//				//	checkTransitiveSources(current, current.toString(), reachable);
//				//}
//				
//				//if (current.toString() == sinkAPIs[0][0]) {
//				//	System.out.println("Uses sink:\t" + current.toString());
//				//}
//			}
//		}
		
//		/* output detected registered actions*/
//		raDetecter.outputDectectedRegisteredActions();
	}
	
	/* find inherited actions */
	public void findInheritedAction(){
		
		/* get application classes */
		Chain<SootClass> app_classes = Scene.v().getApplicationClasses();
		Iterator<SootClass> class_iter = app_classes.iterator();
		while(class_iter.hasNext()){
			SootClass sootClass = (SootClass) class_iter.next();
			System.out.println("app class: " + sootClass.toString());
			List<SootMethod> methods = sootClass.getMethods();
			Iterator<SootMethod> mIter = methods.iterator();
			while(mIter.hasNext()){
				SootMethod method = mIter.next();
				//System.out.println(" method: " + method.getName());
				/* detect inherited action */
				InheritedActionDetector.getDetector().detectAction(method);
			}
		}
	}
	
	/* find registered actions */
	public void findRegisteredActions(){
		/* get application classes */
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		Iterator<SootClass> classIter = appClasses.iterator();
		while(classIter.hasNext()){
			SootClass mClass = classIter.next();
			System.out.println(DEBUG_STRING + "I: app class: " + mClass.getName());
			List<SootMethod> methodList = mClass.getMethods();
			Iterator<SootMethod> methodIter = methodList.iterator();
			while(methodIter.hasNext()){
				SootMethod method = methodIter.next();
				System.out.println(DEBUG_STRING + "I: method: " + method.getName());
				//if(!method.hasActiveBody()){
				//	System.out.println("no active body.");
				//	continue;
				//}
				/* get the body of the method 
				 * If i directly use method.getActiveBody(), i can not get the body of <init> function and some other user-defined
				 * function. It is strange. Anyway, i find it is better to use method.retrieveActiveBody() instead.
				 * refer to this link for details: https://github.com/Sable/soot/issues/130
				 * */
				try{
					Body body = method.retrieveActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					Iterator<Unit> unitIter = units.iterator();
					while(unitIter.hasNext()){
						Unit unit = unitIter.next();
						//System.out.println(DEBUG_STRING + "I: start processing the unit: " + unit.toString());
						
						//TODO we temporarily do not analyze registered actions
						//RegisteredActionDetector.getDetector().isRegisteredActionListener(method, unit);
						//System.out.println(DEBUG_STRING + "I: finished processing one Unit. ");
					}
					//System.out.println("out the while");
				}catch(RuntimeException re){
					/* some method's body can not be retrieved */
					System.out.println(DEBUG_STRING + "I: catch a runtime exception when retrieve active body. continue...");
					re.printStackTrace();
				}
			}
		}
		
	}
	
	/* find the local object reference from an invoke stmt, 
	 * like "obj.setXX(...), $obj is the target variable 
	 * */
	private Local findWidgetObjectVariable(Unit u){
		
		/* is it an invoke stmt, like obj.setXX(...) ? */
		if(u instanceof InvokeStmt){
			System.out.println("it is an invoke stmt.");
			InvokeExpr ie = ((InvokeStmt)u).getInvokeExpr();
			/* is it a virtual invoke stmt ? */
			if( ie instanceof VirtualInvokeExpr){
				System.out.println("it is a virtual invoke stmt.");
				Value base_value = ((VirtualInvokeExpr) ie).getBase();
				System.out.println("the object ref: " + base_value.toString());
				/* is it a local var ?*/
				if( base_value instanceof Local){
					return (Local)base_value;
				}
			}
		}
		return null; /* return null */
	}
	
	
	public void outputAllAppMethods(){
		
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		Iterator<SootClass> classIter = appClasses.iterator();
		while(classIter.hasNext()){
			SootClass mClass = classIter.next();
			System.out.println("app class: " + mClass.getName());
			List<SootMethod> methodList = mClass.getMethods();
			Iterator<SootMethod> methodIter = methodList.iterator();
			while(methodIter.hasNext()){
				SootMethod method = methodIter.next();
				System.out.println(" method: " + method.getName());
			}
		}
	}
	
}