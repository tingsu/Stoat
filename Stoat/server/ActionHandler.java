import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* the action handler */
public class ActionHandler{
	
	private static final String DEBUG_STRING = "[ActionHandler] ";
	
	/** the global action list which stores all detected actions */
	public static Set<Action> actionList = null;
	/** the length of action list */
	public static int actionCounter = 0; 
	
	/** the statically inferred actions, SIA */
	public static List<Action> siaList = null;
	/** the count of statically inferred actions */
	public static int siaListLength = 0;
	
	/** the activities in this set has been added menu, do not need to add anymore */
	public static Set<String> menuAddedActivities = null;
	
	/** the global unique action handler */
	private static ActionHandler handler = new ActionHandler();
	private ActionHandler(){
		actionList = new HashSet<Action>();
		siaList = new ArrayList<Action>();
		menuAddedActivities = new HashSet<String>();
	}
	public static ActionHandler getHandler(){ return handler;}
	
	/**
	 * This action will return the action id in action list
	 * if the given action exists already, it will return the exist id
	 * otherwise, it will return the given action id.
	 * @param action
	 * @return
	 */
	public int addAction(Action action){
		
		if(actionList.add(action)){
			System.out.println(DEBUG_STRING + "I: add a new action <id:" + action.getActionID() + "> into the global action list.");
			return action.getActionID();
		}
		else{
			System.out.println(DEBUG_STRING + "I: an old action <id:" + action.getActionID() + ">.");
			return action.actionIdExistInActionList;
		}
	}
	
	/* output all actions in the action list */
	public void outputAllActions(){
		for(Action action: actionList){
			System.out.println(action.getActionDescription());
		}
	}
	
	/** output all statically inferred actions */
	public void outputAllSIA(){
		for(Action action: siaList){
			System.out.println(action.getActionDescription());
		}	
	}
	
	public void storeActionsFromStaticAnalysis(){
		
		//store registered actions
		for(RegisteredAction ra: RegisteredActionDetector.getDetector().registeredActionList){
			int actionID = ra.getViewID();
			if(actionID != Action.nilViewID){
				ra.setActionID();
				ra.setActionSource(Action.actionFromStaticAnalysis);
				siaList.add(ra);
			}
		}
		//store option menu items
		for(OptionsMenu om: OptionsMenuDetector.getDetector().optionsMenus){
			for(MenuItem mi: om.menuItems){
				int itemViewID = mi.getViewID();
				if(itemViewID != Action.nilViewID){
					mi.setActionID();
					mi.setActionSource(Action.actionFromStaticAnalysis);
					siaList.add(mi);
				}
			}
		}
		//store context menu items
		for(ContextMenu cm: ContextMenuDetector.getDetector().contextMenus){
			for(MenuItem mi: cm.menuItems){
				int itemID = mi.getViewID();
				if(itemID != Action.nilViewID){
					mi.setActionID();
					mi.setActionSource(Action.actionFromStaticAnalysis);
					siaList.add(mi);
				}
			}
		}
		siaListLength = siaList.size();
	}
	
	/* get an action by its action id */
	public Action getAction(int actionID){
		
		for(Action a: actionList){
			if(a.getActionID() == actionID)
				return a;
		}
		return null;
	}
	
	/** 
	 * get an action from the global action list of the app under test
	 * @param view the associated view component
	 * @param actionType the associated action type
	 * */
	public Action getAction(ViewComponent view, String actionType){
		int viewID = view.getViewID();
		String viewType = view.getViewType();
		
		//find the action with the same view id
		for(Action a: actionList){
			if(a.getViewComponnet() == null){
				//if we do not know the action's view component, we can only compare its view id.
				if(a.getViewID() == viewID && 
					a.getActionType().equals(actionType))
					if(a.getViewText()!=null){
						//if it does have a view text, compare its view text to make sure they are same
						if(a.getViewText().equals(view.getTextContent()))
							return a;
					}else{
						return a;
					}
			}else{
				//if we do know the action's associated view component, compare its view component
				//to make sure they are same
				if(a.getViewComponnet().isEqual(view) && 
					a.getActionType().equals(actionType))
					if(a.getViewText()!=null){
						//if it does have a view text, compare its view text to make sure they are same
						if(a.getViewText().equals(view.getTextContent()))
							return a;
					}else{
						return a;
					}
			}
		}
		
		/* Special handling for a TextView which has an internal view id (different from resource ids from the application level).
		 *  We guess they are titles of menu items.
		 * We observe that robotium does not provide APIs for getting views of menu items. 
		 * 	And Android uses internal classes like "com.android.internal.view.menu.ListMenuItemView" to represent
		 *  menu items from which we can not get their resource ids (their default ids are -1).
		 * 	In addition, the tiles of the menu items in the same menu (options or context menus) are parsed as 
		 * 	TextViews with same resource ids (unbelievable! why?). 
		 * 	As a result, it is not possible to associate item menu actions 
		 * 	with menu item ids from the app state alone.
		 * 	It is also not easy to use the analyzed actions from static analysis alone to generate invokable actions
		 *  because the views of menu items are implicitly declared as internal classes instead of classes like "view.MenuItem".
		 * So we exploit the following observations to link the menu items with their titles from both phases
		 * 	First, these special TextViews use internal resource ids which are different 
		 * 	from resource ids parsed during static analysis.
		 *  Second, menu item titles are also available from static analysis.
		 */
		if(viewType.contains("TextView")){
			for(Action a: actionList){
				if(a instanceof MenuItem){
					if( a.getViewText().equals(view.getTextContent())
							&& a.getActionType().equals(actionType))
						return a;
				}
			}
		}
		
		return null;
	}
	
	public void addKeyEventBackAction(AppState state){
		System.out.println(DEBUG_STRING + "I: add a keyevent \"back\" action" );
		SystemAction keyevent_back_action = new SystemAction();
		keyevent_back_action.setActionID();
		keyevent_back_action.setViewCIS("");
		keyevent_back_action.setViewID(Action.nilViewID);
		keyevent_back_action.setActionSource(Action.actionFromSystem);
		keyevent_back_action.setActivityName(state.getActivityName());
		keyevent_back_action.setActionCmd(SystemAction.KeyeventBackCmd,"");
		//add the keyevent_back action into action list
		Integer actionID = ActionHandler.getHandler().addAction(keyevent_back_action);
		state.addInvokableAction(actionID);
	}
	
	public void addResetAction(AppState state){
		System.out.println(DEBUG_STRING + "I: add a \"reset\" action" );
		SystemAction reset_action = new SystemAction();
		reset_action.setActionID();
		reset_action.setViewID(Action.nilViewID);
		reset_action.setViewCIS("");
		reset_action.setActionSource(Action.actionFromSystem);
		reset_action.setActivityName(state.getActivityName());
		reset_action.setActionCmd(SystemAction.ResetCmd,"");
		//add the reset action into action list
		Integer actionID = ActionHandler.getHandler().addAction(reset_action);
		state.addInvokableAction(actionID);
	}
	
	/** add system actions, e.g., menu, back, down, up, orientation. */
	public void addSystemActions(AppState state){
		String activityName = state.getActivityName() ;
		System.out.println(DEBUG_STRING + "I: add possible SYSTEM actions for the app state in the Activity: " + activityName);
		int actionID;
		
		//does the activity has options menu?
		if(InheritedActionDetector.getDetector().hasOptionsMenu(activityName) 
				|| ConfigOptions.ENTRY_ACTIVITY_NAME.equals(activityName) ){ //add the "menu" event for the entry activity
			
			
			//add "menu" action
			SystemAction menuAction = new SystemAction();
			menuAction.setActionID();
			// the view ID is set as NIL
			menuAction.setViewID(Action.nilViewID);
			menuAction.setViewCIS("");  //set as "" for system actions
			menuAction.setActionSource(Action.actionFromSystem);
			menuAction.setActivityName(activityName);
			menuAction.setActionCmd(SystemAction.MenuActionCmd,"");
			// add the menu action into action list
			actionID = ActionHandler.getHandler().addAction(menuAction);
			state.addInvokableAction(actionID);
			
		}
		
		// ensure the entry activity will not invoke "back" action
 		// Comment out this checking, avoiding invoking "back" action for the entry activity sometimes does not make sense
		//if(! ConfigOptions.ENTRY_ACTIVITY_NAME.equals(activityName)){
			// add "back" action
			SystemAction backAction = new SystemAction();
			backAction.setActionID();
			// the view ID is set as NIL
			backAction.setViewID(Action.nilViewID);
			backAction.setViewCIS("");  //set as "" for system actions
			backAction.setActionSource(Action.actionFromSystem);
			backAction.setActivityName(activityName);
			backAction.setActionCmd(SystemAction.BackActionCmd,"");
			// add the "back" action into action list
			actionID = ActionHandler.getHandler().addAction(backAction);
			state.addInvokableAction(actionID);
		//}
		
		//add "scroll up/down" action
		if( state.getUIPage().isScrollable()){
			//Generate a scroll down action 
			SystemAction da_scroll_down = new SystemAction();
	    	da_scroll_down.setActionID();
	    	da_scroll_down.setViewCIS(state.getUIPage().getScrollViewCIS());
	    	da_scroll_down.setActivityName(state.getActivityName());
	    	da_scroll_down.setActionSource(Action.actionFromScreenAnalysis);
	    	da_scroll_down.setActionType(Action.scroll);
			String actionCmd_scroll_down = Action.scroll + "(direction=\'down\')\n";
			da_scroll_down.setActionCmd(actionCmd_scroll_down, "");
			
			// add this new action into the action list
			int actionID_scroll_down = ActionHandler.getHandler().addAction(da_scroll_down);
			// add the action into the app state's action list
			state.addInvokableAction(actionID_scroll_down);
			
			System.out.println(DEBUG_STRING + "I: create a *Scroll Down* Action!");
			
			
			////////////////
			
			//Generate scroll up action 
			SystemAction da_scroll_up = new SystemAction();
	    	da_scroll_up.setActionID();
	    	da_scroll_up.setViewCIS(state.getUIPage().getScrollViewCIS());
	    	da_scroll_up.setActivityName(state.getActivityName());
	    	da_scroll_up.setActionSource(Action.actionFromScreenAnalysis);
	    	da_scroll_up.setActionType(Action.scroll);
			String actionCmd_scroll_up = Action.scroll + "(direction=\'up\')\n";
			da_scroll_up.setActionCmd(actionCmd_scroll_up,"");
			
			// add this new action into the action list
			int actionID_scroll_up = ActionHandler.getHandler().addAction(da_scroll_up);
			// add the action into the app state's action list
			state.addInvokableAction(actionID_scroll_up);
			
			System.out.println(DEBUG_STRING + "I: create a *Scroll Up* Action!");
		}
	}
	
	/** find static actions from a TextView component */
	public void addInferredActionsForTextView(AppState state, ViewComponent vc){
		
		System.out.println(DEBUG_STRING + "I: add possible actions on *TextView* from static analysis");
		
		int viewID = vc.getViewID();
		for(Action staticAction: siaList){
			if(staticAction.getViewID() == viewID){
				//if the action type is not "click", we should add this new action into the action list and invokable action list
				if(staticAction.getActionType().equals(Action.longClick))
				{
					String text = vc.getTextContent();
					Action longClickAction=new Action();
					longClickAction.setActionID();
					longClickAction.setViewID(vc.getViewID());
					longClickAction.setActivityName(state.getActivityName());
					longClickAction.setViewText(vc.getTextContent());
					longClickAction.setActionSource(Action.actionFromStaticAnalysis);
					longClickAction.setActionType(Action.textViewLongClickByIndex);
					 if ((text.contains("\"")) || ((text.contains("(")) && (text.contains(")")))) {
                         text = text.replace("\"", "");
                         text = text.replace("(", "");
                         text = text.replace(")", "");
                     }
	        		//String actionCmd = Action.longClickLocSensitive + "(" + vc.getViewIndex() + ", \"" + text + "\")\n";
					String actionCmd = Action.textViewLongClickByIndex + "(" + vc.getViewIndex() + ")\n";
					longClickAction.setActionCmd(actionCmd,"TextView");
					//add into the global action list 
        			Integer actionID=ActionHandler.getHandler().addAction(longClickAction);
        			//add into the app state's action list
					state.addInvokableAction(actionID);
				}
			}
		}
	}
	
	/** find static actions from a Button component */
	public void addInferredActionsForButton(AppState state, ViewComponent vc){
		System.out.println(DEBUG_STRING + "I: add possible actions on *Button* from static analysis");
		int viewID = vc.getViewID();
		for(Action staticAction: siaList){
			if(staticAction.getViewID() == viewID){
				//if the action type is not "click", we should add this new action into the action list and invokable action list
				if(staticAction.getActionType().equals(Action.longClick))
				{
					String text = vc.getTextContent();
					DynamicAction longClickAction=new DynamicAction();
					longClickAction.setActionID();
					longClickAction.setViewID(vc.getViewID());
					longClickAction.setActivityName(state.getActivityName());
					longClickAction.setViewText(vc.getTextContent());
					longClickAction.setActionSource(Action.actionFromStaticAnalysis);
					longClickAction.setActionType(Action.longClick);
					 if ((text.contains("\"")) || ((text.contains("(")) && (text.contains(")")))) {
                         text = text.replace("\"", "");
                         text = text.replace("(", "");
                         text = text.replace(")", "");
                     }
	        		String actionCmd = Action.longClick + "(\"" + text + "\")\n";
					longClickAction.setActionCmd(actionCmd,"Button");
        			Integer actionID=ActionHandler.getHandler().addAction(longClickAction);
					state.addInvokableAction(actionID);
				}
			}
		}	
	}

	/** find static actions from an EditText component */
	public void addInferredActionsForEditText(AppState state, ViewComponent vc){
		System.out.println(DEBUG_STRING + "I: add possible actions on EditText from static analysis");
		int viewID = vc.getViewID();
		for(Action staticAction: siaList){
			if(staticAction.getViewID() == viewID){
				//if the action type is not "click", we should add this new action into the action list and invokable action list
				if(staticAction.getActionType().equals(Action.longClick))
				{
					//TODO checkout robotium documentation
				}
			}
		}	
	}
	
	/** find static actions from an ImageView component */
	public void addInferredActionsForImageView(AppState state, ViewComponent vc){
		System.out.println(DEBUG_STRING + "I: add possible actions on ImageView from static analysis");
		int viewID = vc.getViewID();
		for(Action staticAction: siaList){
			if(staticAction.getViewID() == viewID){
				//if the action type is not "click", we should add this new action into the action list and invokable action list
				if(staticAction.getActionType().equals(Action.longClick))
				{
					//TODO checkout robotium documentation
				}
			}
		}	
	}
	
	/** find static actions from an ImageButton component */
	public void addInferredActionsForImageButton(AppState state, ViewComponent vc){
		System.out.println(DEBUG_STRING + "I: add possible actions on ImageButton from static analysis");
		int viewID = vc.getViewID();
		for(Action staticAction: siaList){
			if(staticAction.getViewID() == viewID){
				//if the action type is not "click", we should add this new action into the action list and invokable action list
				if(staticAction.getActionType().equals(Action.longClick))
				{
					//TODO checkout robotium documentation
				}
			}
		}	
	}
	
	/* does the action with $viewID and $actionType already exist? */
	public static boolean hasAction(int viewID, String actionType){
		for(Action action: actionList){
			if(action.getViewID() == viewID && action.getActionType().equals(actionType) )
				return true;
		}
		return false;
	}
}
