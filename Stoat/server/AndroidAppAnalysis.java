import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.options.Options;

public class AndroidAppAnalysis{
	
	private static final String[] activityCallbacks = {"onCreate", "onStart", "onResume", "onRestart", "onPause", "onStop", "onDestroy"};
	private static final String[] serviceCallbacks = {"onCreate", "onStart", "onResume", "onRestart", "onPause", "onStop", "onDestroy"};
	
	/* the app location  */
	public static String mAppFolder = null;
	private String mSdkFolder = null;
	private String androidLib = null;
	/* the app package name */
	public static String appPackageName = null;
	
	/* activities and services in the app*/
	private List<String> activities = new ArrayList<String>();
	private List<String> services = new ArrayList<String>();
	/* UI event handlers from layout XML file */
	private List<String> uiCallbacks = new ArrayList<String>();
	
	/* the main */
	public static void main(String[] args){
		System.out.println("[Android Analysis] This is the main entry of MainAndroidAnalysis");
		AndroidAppAnalysis analysis = new AndroidAppAnalysis();
		analysis.run(args);
	}
	
	/* This is the main entry of soot analysis */
	public void run(String[] args){
		
		System.out.println("[Android Analysis] AndroidAppAnalysis.run");
		
		System.out.println("[Android Analysis] command line options: ");
		for(String arg: args){
			System.out.print(arg + " ");
		}
		System.out.println("\n");
		
		if (args.length < 1 || args[0].equals("--help") || args[0].equals("-h")) {
			System.out.println("Usage: MainAndroidAnalysis <main class to be analyzed> [options]");
			System.out.println("Required libraries:  soot, android SDK");
			return;
		}
		if (args[0].equals("--list") && args.length == 2) {
			SootClass mClass = Scene.v().loadClassAndSupport(args[1]);
			Scene.v().loadNecessaryClasses();
			printClassMethods(mClass);
			return;
		}else{
			
			/* the first arg is --apk-name xx */
			if(args[0].equals("--apk-name")){
				mAppFolder = args[1];
			}
            
			mSdkFolder = ConfigOptions.ANDROID_SDK_DIR;
			androidLib = ConfigOptions.ANDROID_LIB_DIR;
			
//			if (args.length > 1) {
//				for (int i = 1; i < args.length; i += 2) {
//					if (args[i].equals("--android-sdk")) {
//						mSdkFolder = args[i + 1];
//						androidLib = mSdkFolder + "/platforms/android-20/android.jar";
//					} 
//					else if (args[i].equals("--android-lib")) {
//						androidLib = args[i + 1];
//					}
//					else {
//						System.out.println("Invalid argument.");
//						return;
//					}
//				}
//			}
			
			/* detect activities, services, ui handlers */
			scanAndroidManifest(mAppFolder);
			//TODO we temporarily do not scan layout file
			//scanLayout(mAppFolder);
			
			// Whole-program mode
			// Set class path: classes needed by app and the android.jar file (hard-coded for now)
			String classPath = androidLib;
			//classPath += ":" + mSdkFolder + "/platforms/android-16/data/layoutlib.jar";
			classPath += ":" + mAppFolder + "/bin/classes";
			classPath += ":" + mAppFolder + "/libs";
			
//			File libsFolder = new File(mAppFolder + "/libs");
//			if (libsFolder != null) {
//				for (File libsFile : libsFolder.listFiles()) {
//					String path = libsFile.getAbsolutePath();
//					
//					if (path.endsWith(".jar") || path.endsWith(".zip")) {
//						classPath += ":" + path;
//					}
//				}
//			}

			String[] argsList = {"-w",
								 "-keep-line-number",
								 //"-verbose",
								 "-allow-phantom-refs",
								 //"-full-resolver",
								 "-f", "J",
					 			 //"-p", "cg", "all-reachable",
								 //"-p", "cg", "safe-newinstance",
								 //"-p", "cg", "safe-forname",
								 "-p", "cg", "verbose:true",
								 "-cp", classPath};
			
			/* prepare the args for soot */
			String[] sootArgs = new String[args.length-2];
			for(int i=0; i<args.length-2; i++){
				sootArgs[i] = args[i+2];
			}
			System.out.println("[Android Analysis]: args passing to soot ");
			for(String arg: sootArgs){
				System.out.print(arg+ " ");
			}
			System.out.println("\n");
			
			/* let soot parse the given command line options */
			Options.v().parse(sootArgs);
			
			/* load our analysis module into the "wjtp" pack */
			PackManager.v().getPack("wjtp").add(new Transform("wjtp.ActionAnalysis", new ActionsAnalysis()));
			//test example
			//PackManager.v().getPack("wjtp").add(new Transform("wjtp.TestAnalysis", new TestAnalysis()));
			
			// Add entry points to scene
			List<SootMethod> entryPoints = new ArrayList<SootMethod>();

			/* set activities and services classes as application classes in the soot analysis */
			for (String activity : activities) {
				SootClass mainClass = Scene.v().forceResolve(activity, SootClass.BODIES);
				mainClass.setApplicationClass();
				Scene.v().loadNecessaryClasses();
			
				// Add activity callbacks as entry points
				for (String callback : activityCallbacks) {
					if (mainClass.declaresMethodByName(callback)) {
						entryPoints.add(mainClass.getMethodByName(callback));
						System.out.println("[Android Analysis] an entry point (\"acticity callback\"): " + callback);
					}
				}
				
//				// Add UI event handlers as entry points
//				for (String callback : uiCallbacks) {
//					if (mainClass.declaresMethodByName(callback)) {
//						entryPoints.add(mainClass.getMethodByName(callback));
//					}
//				}
//				
//				// Check for implementers of OnClickListener and add as entrypoints
//				// TODO: check for other UI listeners (e.g. OnDragListener, etc.)
//				SootClass listenerInterface = Scene.v().getSootClass("android.view.View$OnClickListener");
//				List<SootClass> listenerClasses = Scene.v().getActiveHierarchy().getImplementersOf(listenerInterface);
//				
//				for (SootClass listener : listenerClasses) {
//					entryPoints.add(listener.getMethodByName("onClick"));
//				}
			}
			
			for (String service: services) {				
				// Add service callbacks as entry points
				SootClass mainClass = Scene.v().forceResolve(service, SootClass.BODIES);
				mainClass.setApplicationClass();
				Scene.v().loadNecessaryClasses();
				
				for (String callback : serviceCallbacks) {
					if (mainClass.declaresMethodByName(callback)) {
						entryPoints.add(mainClass.getMethodByName(callback));
						System.out.println("[Android Analysis] an entry point (\"service callback\"): " + callback);
					}
				}
			}
			
			/*
			SootClass contentResolver = Scene.v().forceResolve("android.content.ContentResolver", SootClass.BODIES);
			//System.out.println(contentResolver.getMethods().toString());
			SootMethod queryMethod = Scene.v().getMethod("<android.content.ContentResolver: android.database.Cursor query(android.net.Uri,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String)>");
			List<SootClass> resolverClasses = Scene.v().getActiveHierarchy().getDirectSubclassesOf(contentResolver);
			System.out.println(resolverClasses.size());
			List<SootMethod> queryMethods = Scene.v().getActiveHierarchy().resolveAbstractDispatch(contentResolver, queryMethod);
			System.out.println(queryMethods.size());
			*/
			
			//SootClass leakerClass = Scene.v().getSootClass("com.utoronto.miwong.leaktest.WebHistoryToWebLeaker");
			//leakerClass.setApplicationClass();
			
			Scene.v().setEntryPoints(entryPoints);

			PackManager.v().runPacks();
			PackManager.v().writeOutput();
		}
		
		System.out.println("[Android Analysis] Analysis Finished...");
	}
	
	/* scan androidManifest.xml to detect activities and services
	 * Activities or services can be entry points in an android app.
	 *  */
	private void scanAndroidManifest(String fileLocation){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            Document manifest = docBuilder.parse(new File(fileLocation + "/AndroidManifest.xml"));
            
            NodeList manifestNode = manifest.getElementsByTagName("manifest");
            NamedNodeMap manifestAttr = manifestNode.item(0).getAttributes();
            String packageName = manifestAttr.getNamedItem("package").getNodeValue();
            
            /* set the android app package name */
            appPackageName = new String(packageName);
            System.out.println("[Android Analysis] app package name: " + appPackageName);
            
            NodeList activityNodes = manifest.getElementsByTagName("activity");
            System.out.println("[Android Analysis] activities count: " + activityNodes.getLength());
            
            for (int i = 0; i < activityNodes.getLength(); i++) {
            	Node activity = activityNodes.item(i);
            	String activityName = activity.getAttributes().getNamedItem("android:name").getNodeValue();
            	
            	/* parse activites
            	 * see this link: http://developer.android.com/guide/topics/manifest/activity-element.html
            	 */
            	if (activityName.startsWith(".")) { /* a simplified activity name starting with "." */
            		activityName = packageName + activityName;
            		System.out.println("[Android Analysis] [detected activity]: " + activityName);
            	}else if(!activityName.contains(".")){ /* another simplified activity name starting without "." */
            		activityName = packageName + "." + activityName;
            		System.out.println("[Android Analysis] [detected activity]: " + activityName);
            	}
            	else{ /* a default fully-specified activity name */
            		System.out.println("[Android Analysis] [detected activity]: " + activityName);
            	}
            	
            	activities.add(activityName);
            }
            
            NodeList serviceNodes = manifest.getElementsByTagName("service");
            System.out.println("[Android Analysis] services count: " + serviceNodes.getLength());
            
            for (int i = 0; i < serviceNodes.getLength(); i++) {
            	Node service = serviceNodes.item(i);
            	String serviceName = service.getAttributes().getNamedItem("android:name").getNodeValue();
            	
            	/* parse services
            	 * see this link: http://developer.android.com/guide/topics/manifest/service-element.html
            	 */
            	if (serviceName.startsWith(".")) { /* the parsing rules is similar with activities */
            		serviceName = packageName + serviceName;
            		System.out.println("[Android Analysis] [detected service]: " + serviceName);
            	}else if(!serviceName.contains(".")){ 
            		serviceName = packageName + "." + serviceName;
            		System.out.println("[Android Analysis] [detected service]: " + serviceName);
            	}
            	else{
            		System.out.println("[Android Analysis] [detected service]: " + serviceName);
            	}
            	
            	services.add(serviceName);
            }

		} catch (Exception err) {
			System.out.println("[Android Analysis] Error in scanning AndroidManifest.xml: " + err);
		}
	}
	
	/* scan layout files to detect ui events */
	private void scanLayout(String fileLocation){

		try {		
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
			File layoutFolder = new File(fileLocation + "/res/layout");

			for (File layoutFile : layoutFolder.listFiles()) {
				if (layoutFile.getName().endsWith(".xml")) {
		            Document layout = docBuilder.parse(layoutFile);
		            
		            // TODO: add support for other UI elements/events
		            NodeList buttons = layout.getElementsByTagName("Button");
		            
		            for (int i = 0; i < buttons.getLength(); i++) {
		            	Node node = buttons.item(i);
		            	NamedNodeMap nodeAttr = node.getAttributes();
		            	
		            	if (nodeAttr != null) {
		            		Node onclick = nodeAttr.getNamedItem("android:onClick");
		            		if (onclick != null) {
		            			uiCallbacks.add(onclick.getNodeValue());
		            		}
		            	}
		            }
				}
			}
			
		} catch (Exception err) {
			System.out.println("[Android Analysis] Error in obtaining UI event handlers: " + err);
		}
	}
	
	
	/* Doesn't use whole program mode */
	private void printClassMethods(SootClass mclass) {
		System.out.println(mclass.toString());
		//out = new BufferedWriter(new FileWriter(FILE));

		List<SootMethod> methods = mclass.getMethods();
		Iterator<SootMethod> iter = methods.iterator();

		while (iter.hasNext()) {
			SootMethod m = iter.next();
			if (!m.isConcrete()) {
				continue;
			}

			System.out.println("\t" + m.toString());

			Body b = m.retrieveActiveBody();
			Iterator<ValueBox> iter_v = b.getUseBoxes().iterator();
			while (iter_v.hasNext()) {
				Value v = iter_v.next().getValue();

				if (v instanceof InvokeExpr) {
					InvokeExpr iv = (InvokeExpr) v;
					System.out.println("\t\t" + iv.getMethod().toString());
				}
			}
		}
	}
		
}
