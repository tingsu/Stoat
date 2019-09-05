import java.util.Iterator;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.VirtualInvokeExpr;

/* the general android analysis utility class */
public class AndroidUtility{
	
	private final static String menuInflateMethodName = "inflate";
	private final static String menuInflateMethodClass = "android.view.MenuInflater";
	
	/* does the onCreateOptionsMenu method invoke MenuInflater.Infate ? */
	public static int hasMenuInfate(SootMethod onCreateMenuMethod){
		
		int menuResID = 0;
		
		Body body = onCreateMenuMethod.retrieveActiveBody();
		PatchingChain<Unit> units = body.getUnits();
		Iterator<Unit> unitIter = units.iterator();
		while(unitIter.hasNext()){
			Unit unit = unitIter.next();
			if(unit instanceof InvokeStmt){ // find this form: $0.bt = $1
				InvokeExpr ie = ((InvokeStmt)unit).getInvokeExpr();
				if(ie instanceof VirtualInvokeExpr){
					SootMethod invokedMethod = ie.getMethod();
					/* is it an VirtualInvoke on MenuInflater.inflate(...) */
					if(invokedMethod.getName().equals(menuInflateMethodName) &&
					   invokedMethod.getDeclaringClass().getName().equals(menuInflateMethodClass)	){
						/* get the value of menuResID */
					   Value menuResIDArg = ie.getArg(0); 
					   menuResID = (int)(Integer.parseInt(menuResIDArg.toString()));
					   System.out.println("Menu -> menuResID: " + menuResID);
					   break;
					}
				}
			}
		}
		return menuResID;
	}
	
	public static String getCurrentAppPackageName(){
		return AndroidAppAnalysis.appPackageName;
	}
	
}