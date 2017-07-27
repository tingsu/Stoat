package com.sunyata.kindmind.List;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sunyata.kindmind.AboutActivityC;
import com.sunyata.kindmind.BuildConfig;
import com.sunyata.kindmind.R;
import com.sunyata.kindmind.SortTypeM;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.DatabaseHelperM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Main.MainActivityCallbackListenerI;
import com.sunyata.kindmind.Main.ToastOrActionC;
import com.sunyata.kindmind.Setup.ItemSetupActivityC;
import com.sunyata.kindmind.util.DatabaseU;
import com.sunyata.kindmind.util.DbgU;
import com.sunyata.kindmind.util.ItemActionsU;
import com.sunyata.kindmind.util.OtherU;

/**
 * \brief ListFragmentC shows a list of items, each item corresponding to a row in an SQL database
 * 
 * Uses Android libs:
 * + *ListView*, http://developer.android.com/reference/android/widget/ListView.html
 * + ListFragment, http://developer.android.com/reference/android/support/v4/app/ListFragment.html
 * + LoaderManager, http://developer.android.com/reference/android/support/v4/app/LoaderManager.html
 * + CursorLoader, http://developer.android.com/reference/android/support/v4/content/CursorLoader.html
 * 
 * Notes:
 * + *Support classes are used*, the reason for this is that the ViewPager only is available in a support version
 * and is not compatible with other versions.
 *  + import android.support.v4.app.ListFragment;
 *  + import android.support.v4.app.LoaderManager;
 *  + import android.support.v4.content.CursorLoader;
 *  
 *  \nosubgrouping
 */
public class ListFragmentC extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private int refListType = ListTypeM.NOT_SET; //-saved in onSaveInstanceState
	private static MainActivityCallbackListenerI sCallbackListener;
	//-static because we want to be sure that it is the same for all fragments
	private SimpleCursorAdapter mCursorAdapter;
	private LinearLayout mLoadingLayout;
	private Button mAddNewItemButtonFooter;
	private Button mAddNewItemButtonEmptyView;

	public static final String EXTRA_ITEM_URI = "EXTRA_LIST_DATA_ITEM_ID";
	public static final String EXTRA_LIST_TYPE = "EXTRA_LIST_TYPE";
  public static final String EXTRA_KINDSORT_RESULT = "kindsort_result";

	public static ListFragmentC newInstance(int inListTypeInt, MainActivityCallbackListenerI inCallbackListener){
		ListFragmentC retListFragment = new ListFragmentC();
		retListFragment.refListType = inListTypeInt;
		sCallbackListener = inCallbackListener;
		return retListFragment;
	}

	
	/**@name Loader
	 * A very good example is available at the top of the following page:
	 * http://developer.android.com/reference/android/app/LoaderManager.html
	 * 
	 * Improvement: Since we are using a standard loader and have not made our own implementation we get many calls
	 * to onLoadFinished (one of the methods below) when we click on an item and sort the data. If we made our own
	 * Loader implementation we could for example supress updates until the end of the sorting.
	 * 
	 * Note: We never close the cursor that we get through the loader since this is handled by the
	 * loader (closing ourselves may cause problems)
	 */
	///@{
	
	/**
	 * \brief onCreateLoader creates and returns the CursorLoader, which contains the mapping to the database
	 * 
	 * Documentation: 
	 * + http://www.grokkingandroid.com/using-loaders-in-android/
	 * + http://developer.android.com/reference/android/app/LoaderManager.LoaderCallbacks.html#onCreateLoader%28int,%20android.os.Bundle%29
	 */
	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int inIdUnused, Bundle inArgumentsUnused) {
		Log.d(DbgU.getAppTag(), DbgU.getMethodName(refListType));
		
		//Setup of variables used for selecting the database colums of rows
		String[] tmpProjection = {ItemTableM.COLUMN_ID, ItemTableM.COLUMN_NAME,
				ItemTableM.COLUMN_ACTIVE, ItemTableM.COLUMN_KINDSORT_VALUE, ItemTableM.COLUMN_ACTIONS};
		//-kindsortvalue only needed here when used for debug purposes
		String tmpSelection = ItemTableM.COLUMN_LIST_TYPE + "=?";
		String[] tmpSelectionArguments = {String.valueOf(refListType)};

		//Creating the CursorLoader
		CursorLoader retCursorLoader = new CursorLoader(
				getActivity(), ContentProviderM.ITEM_CONTENT_URI,
				tmpProjection, tmpSelection, tmpSelectionArguments, ContentProviderM.sSortType);
		return retCursorLoader;
	}
	
	/**
	 * \brief onLoadFinished changes (swaps) the old cursor in the adapter to the new one
	 * 
	 * Android reference docs: "Called when a previously created loader has finished its load"
	 * 
	 * Documentation *interesting to read*:
	 *  http://developer.android.com/reference/android/app/LoaderManager.LoaderCallbacks.html#onLoadFinished%28android.content.Loader%3CD%3E,%20D%29
	 */
	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> inCursorLoader, Cursor inCursor) {
		Log.d(DbgU.getAppTag(), DbgU.getMethodName(refListType));
		
		mCursorAdapter.swapCursor(inCursor);
	}
	
	/**
	 * \brief onLoaderReset clears the cursor in the adapter
	 * 
	 * Android reference docs: "Called when a previously created loader is being reset, and thus making its data
	 * unavailable"
	 */
	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> inCursorUnused) {
		Log.d(DbgU.getAppTag(), DbgU.getMethodName(refListType));
		
		mCursorAdapter.swapCursor(null);
	}
	
	///@}
	///@name Sorting
	///@{
	
	/**
	 * Overview: sortDataWithService displays a loading indicator and starts a service which sorts the
	 *  rows in the item table (for all three lists: feelings, needs, kindness).
	 * Details: A ResultReceiver is sent along as an extra, which is used for communication back to this
	 *  fragment once the service has completed its calculations
	 * Improvements: 
	 * Documentation: 
	 */
	public void sortDataWithService(){

		try{
			
			if(mLoadingLayout == null){
				mLoadingLayout = (LinearLayout)getView().findViewById(R.id.loadingLinearLayout);
				Log.w(DbgU.getAppTag(), DbgU.getMethodName()
						+ " mLoadingLayout was null and was recreated");
			}

		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), e);
		}

		/*
    	for(int i = 0; mLoadingLinearLayout != null; i++){
    		Utils.waitForCondition(500, 10, i);
    	}
		 */
		/* -the lines above were added because of the following problem:
java.lang.NullPointerException
at com.sunyata.kindmind.List.ListFragmentC.sortDataWithService(ListFragmentC.java:483)
at com.sunyata.kindmind.MainActivityC.fireClearDatabaseAndUpdateGuiEvent(MainActivityC.java:287)
at com.sunyata.kindmind.MainActivityC.fireSavePatternEvent(MainActivityC.java:225)
at com.sunyata.kindmind.List.ListFragmentC.onOptionsItemSelected(ListFragmentC.java:362)
at android.support.v4.app.Fragment.performOptionsItemSelected(Fragment.java:1568)
		 */

		
		//Showing the Loading progress bar / "spinner"
		if(mLoadingLayout != null){
			//-checking against null again because of npe on my device (sony xperia)
			mLoadingLayout.setVisibility(View.VISIBLE);
			getListView().setVisibility(View.GONE);
		}

		//Sorting data (for all lists)
		Intent tmpIntent = new Intent(getActivity(), SortingAlgorithmServiceM.class);
		AlgorithmServiceResultReceiver tReceiver = new AlgorithmServiceResultReceiver(
				new Handler(), mLoadingLayout, getListView());
		tmpIntent.putExtra(ListFragmentC.EXTRA_KINDSORT_RESULT, tReceiver);
		getActivity().startService(tmpIntent);
	}
	
	private static class AlgorithmServiceResultReceiver extends ResultReceiver{
		
		private final WeakReference<LinearLayout> mWeakRefToLoadingLayout;
		private final WeakReference<ListView> mWeakRefToListView;
		
		public AlgorithmServiceResultReceiver(Handler handler,
				LinearLayout iLoadingLayout, ListView iListView) {
			super(handler);
			mWeakRefToLoadingLayout = new WeakReference<LinearLayout>(iLoadingLayout);
			mWeakRefToListView = new WeakReference<ListView>(iListView);
		}
		
		@Override
		public void onReceiveResult(int inResultCode, Bundle inResultData){
			super.onReceiveResult(inResultCode, inResultData);
			if(inResultCode == SortingAlgorithmServiceM.UPDATE_SERVICE_DONE){
				mWeakRefToLoadingLayout.get().setVisibility(View.GONE);
				mWeakRefToListView.get().setVisibility(View.VISIBLE);

				mWeakRefToListView.get().smoothScrollToPositionFromTop(
						0, 0, (int)ToastOrActionC.BREATHING_LENGTH_IN);
				//-http://stackoverflow.com/questions/11334207/smoothscrolltoposition-only-scrolls-partway-in-android-ics
				sCallbackListener.fireUpdateTabTitlesEvent();
			}
		}
	}

  ///@}
	/**@name Lifecycle
	 * Please note that one click method is inside onActivityCreated and the other is outside
	 */
	///@{
	
	/**
	 * \brief onActivityCreated restores the state, does fundamental setup for the fragment
	 *  and setup for the long click
	 *  
	 * Notes:
	 * + *Contains event handler for long clicks*. Long click is handled separately from the short click because
	 * there is no method to override for the long click
	 */
	@Override
	public void onActivityCreated(Bundle inSavedInstanceState){
		super.onActivityCreated(inSavedInstanceState);
		Log.d(DbgU.getAppTag(), DbgU.getMethodName(refListType));

		//Restoring state
		if(inSavedInstanceState != null){
			refListType = inSavedInstanceState.getInt(EXTRA_LIST_TYPE);
		}

		//Fundamental setup
		super.setRetainInstance(true);
		//-Recommended by CommonsWare:
		// http://stackoverflow.com/questions/11160412/why-use-fragmentsetretaininstanceboolean
		// but not in Reto's book: "generally not recommended"
		super.setHasOptionsMenu(true);
		
		
		
		
		//Adding footer view
		View mFooterView = getActivity().getLayoutInflater()
				.inflate(R.layout.list_fragment_footer, null);
		getListView().addFooterView(mFooterView);
		
		
		
		this.fillListWithDataFromAdapter();

		//Setup for long click listener
		super.getListView().setOnItemLongClickListener(new OnItemLongClickListenerC(getActivity()));
		

		
		mAddNewItemButtonFooter = (Button)getView().findViewById(R.id.listFragment_addNewItem_button);
		mAddNewItemButtonFooter.setOnClickListener(new OnClickNewItemListener());
		/*
		 * -we cannot have this code in onCreateView, if so we will get null from the first line
		 * TODO: Why??
		 */
		
		mAddNewItemButtonEmptyView = (Button)getView().findViewById(R.id.listFragment_empty_addFirstItem_button);
		mAddNewItemButtonEmptyView.setOnClickListener(new OnClickNewItemListener());
		//The same click listener is used here as for the button above

	}
	
	private static class OnItemLongClickListenerC implements OnItemLongClickListener{
		
		private final WeakReference<Activity> mWeakRefToActivity;
		
		public OnItemLongClickListenerC(Activity iActivity){
			mWeakRefToActivity = new WeakReference<Activity>(iActivity);
		}
		
		@Override
		public boolean onItemLongClick(AdapterView<?> a1, View a2, int a3, long inId) {
			//Opening the details for the list item
			Uri tmpUri = Uri.parse(ContentProviderM.ITEM_CONTENT_URI + "/" + inId);
			Intent intent = new Intent(mWeakRefToActivity.get(), ItemSetupActivityC.class);
			String tmpExtraString = tmpUri.toString();
			intent.putExtra(EXTRA_ITEM_URI, tmpExtraString);
			//-Extracted in DataDetailsFragmentC
			mWeakRefToActivity.get().startActivityForResult(intent, 0);
			//-Calling DataDetailsActivityC
			return true;
			//-important that we return true here, otherwise we will sometimes also get
			//an ordinary click
		}
	}

	
	/*
	 * Overview: onCreateView inflates the layout and prepares for the loading by storing a reference to
	 *  the loading layout
	 * Notes: 
	 * Improvements: 
	 * Documentation: 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		super.onCreateView(inflater, parent, savedInstanceState);
		//-TODO: Verify ok (super not called in the Big Nerd Ranch book, or in Reto's book)
		Log.d(DbgU.getAppTag(), DbgU.getMethodName());

		//Inflating the layout
		View v = inflater.inflate(R.layout.fragment_list, parent, false);

		//Getting reference to the views
		mLoadingLayout = (LinearLayout)v.findViewById(R.id.loadingLinearLayout);
		//mProgressBar = (ProgressBar)v.findViewById(R.id.listProgressBar);
		//-we can access the listview with getListView() so we don't need to save this reference
		mLoadingLayout.setVisibility(View.GONE);


		/*
		//Adding footer view
		Button tFooterView = (Button) View.inflate(getActivity(), R.id.listFragment_footer, (ViewGroup) v);
		getListView().addFooterView(tFooterView);
		 */
		
		return v;
	}

	/*
	 * Overview: onSaveInstanceState saves the state of the list fragment into a bundle.
	 *  Loading is done in onActivityCreated()
	 */
	@Override
	public void onSaveInstanceState(Bundle outBundle){
		Log.d(DbgU.getAppTag(), DbgU.getMethodName(refListType));

		outBundle.putInt(EXTRA_LIST_TYPE, refListType); //-saving the list type

		super.onSaveInstanceState(outBundle);
	}

  ///@}
	///@name onListItemClick and options menu
	///@{

	/**
	 * \brief onListItemClick handles clicks on a list item: it updates the DB and refreshes the GUI
	 * 
	 * Improvement: This method is called when long clicking as well, why is unknown.
	 */
	@Override
	public void onListItemClick(ListView l, View inView, int pos, long inId){ //[list update]
		super.onListItemClick(l, inView, pos, inId);

		//Switching the check box off/on
		CheckBox tmpCheckBox = ((CheckBox)inView.findViewById(R.id.list_item_activeCheckBox));
		boolean tmpNewCheckedState = !tmpCheckBox.isChecked();

		//Updating the database value
		Uri tmpUri = Uri.parse(ContentProviderM.ITEM_CONTENT_URI + "/" + inId);
		ContentValues tmpContentValues = new ContentValues();
		tmpContentValues.put(ItemTableM.COLUMN_ACTIVE, tmpNewCheckedState ? 1 : ItemTableM.FALSE);
		getActivity().getContentResolver().update(tmpUri, tmpContentValues, null, null);

		//Performing the various toasts or actions
		String tName = (String)((TextView)inView.findViewById(R.id.list_item_titleTextView)).getText();
		if(refListType == ListTypeM.FEELINGS && tmpNewCheckedState){
			ToastOrActionC.feelingsToast(getActivity(), tName);
		}else if(refListType == ListTypeM.NEEDS && tmpNewCheckedState){
			ToastOrActionC.needsToast(getActivity(), tName);
		}else if(refListType == ListTypeM.KINDNESS && tmpNewCheckedState){
			ToastOrActionC.randomKindAction(getActivity(), DatabaseU.getItemUriFromId(inId));
		}

		//Sorting
		this.sortDataWithService();
	}

	/*
	 * Overview: onCreateOptionsMenu inflates the options menu and hides a few options if we have a release build
	 */
	@Override
	public void onCreateOptionsMenu(Menu inMenu, MenuInflater inMenuInflater){
		super.onCreateOptionsMenu(inMenu, inMenuInflater);
		inMenuInflater.inflate(R.menu.list_menu, inMenu);

		//If we are not running in debug mode, hide the following option items
		if(DbgU.isReleaseVersion(getActivity())){
			inMenu.findItem(R.id.menu_item_share_experience).setVisible(false);
			inMenu.findItem(R.id.menu_item_backup_database).setVisible(false);
			inMenu.findItem(R.id.menu_item_reset_database).setVisible(false);
			inMenu.findItem(R.id.menu_item_clear_all_list_selections).setVisible(false);
			inMenu.findItem(R.id.menu_item_sort_alphabetically).setVisible(false);
			inMenu.findItem(R.id.menu_item_kindsort).setVisible(false);
		}
	}
	
	/*
	 * Overview: onOptionsItemSelected is called when an options item is clicked
	 * Details: Please see each section in the method for details about the different responses for the buttons
	 * Documentation:
	 *  http://developer.android.com/reference/android/app/Activity.html#onOptionsItemSelected%28android.view.MenuItem%29
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem inMenuItem){
		
		switch (inMenuItem.getItemId()){
		case R.id.menu_item_save_pattern:
			sCallbackListener.fireSavePatternEvent();
			
			return true;
		case R.id.menu_item_sort_alphabetically:
			//Changing the sort method used and refreshing list
			DatabaseU.setItemTableSortType(SortTypeM.ALPHABETASORT);
			getLoaderManager().restartLoader(refListType, null, this);
			getListView().smoothScrollToPositionFromTop(0, 0);
			
			return true;
		case R.id.menu_item_kindsort:
			//Changing the sort method used and refreshing list
			DatabaseU.setItemTableSortType(SortTypeM.KINDSORT);
			getLoaderManager().restartLoader(refListType, null, this);
			this.sortDataWithService();
			getListView().smoothScrollToPositionFromTop(0, 0);
			
			return true;
		case R.id.menu_item_clear_all_list_selections:
			//Clearing activated and going left
			getListView().smoothScrollToPositionFromTop(0, 0);
			sCallbackListener.fireClearDatabaseAndUpdateGuiEvent();
			
			return true;
		case R.id.menu_item_send_as_text_all:
			String tmpAllListsAsText =
				this.getFormattedStringForListType(ListTypeM.FEELINGS) +
				this.getFormattedStringForListType(ListTypeM.NEEDS) +
				this.getFormattedStringForListType(ListTypeM.KINDNESS);
			OtherU.sendAsEmail(getActivity(), "KindMind all lists as text", tmpAllListsAsText, null);

			return true;
		case R.id.menu_item_about:
			startActivity(new Intent(getActivity(), AboutActivityC.class));
			
			return true;
		case R.id.menu_item_help:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
					"http://sites.google.com/site/mindfulnessandhealing/kindmind")));
			
			return true;
		case R.id.menu_item_share_experience: //TODO: Do this as a hard coded action instead
			/*
			sendAsEmail(
					"From the Kind Mind (Android app): My present experience",
					"I am feeling "
					+ KindModelM.get(getActivity()).getToastString(ListTypeM.SUFFERING)
					+ ", because i am needing "
					+ KindModelM.get(getActivity()).getToastString(ListTypeM.NEEDS)
					+ "\n\nSent from the Android app Kind Mind, can be found in the google play store");
					//"Please help");
			//Asking for help
			//Asking for empathy (Can you reflect back what you are hearing/reading)
			 */
			return true;
		case R.id.menu_item_backup_database: //------------Backup of database by sending database file
			OtherU.sendAsEmail(getActivity(), "Backup of KindMind database", "Database file is attached",
					getActivity().getDatabasePath(DatabaseHelperM.DATABASE_NAME));

			return true;
		case R.id.menu_item_reset_database: //------------Resetting database
			sCallbackListener.fireResetDataEvent();

			return true;
		default:
			return super.onOptionsItemSelected(inMenuItem);
		}
	}
	
  ///@}
	//-------------------------------------------Private-------------------------------------------
	
	/*
	 * Overview: getFormattedStringForListType formats a list of a given type into a string
	 * Used in: Used together with sendAsEmail
	 */
	private String getFormattedStringForListType(int inListType){
		String retString = "\n" + "===" + ListTypeM.getListTypeString(inListType) + "===" + "\n\n";
		
		String tmpSelection = ItemTableM.COLUMN_LIST_TYPE + "=" + inListType;
		Cursor tItemCr = getActivity().getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tmpSelection, null, null);
		tItemCr.moveToFirst();
		try{
			//For each list data item..
			for(; tItemCr.isAfterLast() == false; tItemCr.moveToNext()){
				//..adding the name to the string
				retString = retString + tItemCr.getString(tItemCr.getColumnIndexOrThrow(
						ItemTableM.COLUMN_NAME)) + "\n";
			}
		}catch(Exception e){Log.wtf(DbgU.getAppTag(), DbgU.getMethodName());
		}finally{
			if(tItemCr != null){
				tItemCr.close();
			}
		}

		return retString;
	}
	
	/**
	 * \brief createListDataSupport fills the data from the database into the loader
	 * by (re-/)creating a cursor adapter (with mapping to database columns) and initiating the loader
	 * 
	 * Used in: onActivityCreated (not used by update methods)
	 * 
	 * Uses app internal: CursorAdapterM
	 * 
	 * Uses Android lib: setListAdapter, initLoader
	 * 
	 * Notes: The synching of the state of the checkboxes with the database is not done automatically,
	 *  this is handled in another place (getView in CustomCursorAdapter)
	 */
	private void fillListWithDataFromAdapter(){
		Log.i(DbgU.getAppTag(), DbgU.getMethodName(refListType));
		
		if(refListType == ListTypeM.NOT_SET){
			Log.wtf(DbgU.getAppTag(), "Error in fillListWithDataFromAdapter, refListType has not been set");
		}

		//Creating (or re-creating) the loader
		getLoaderManager().initLoader(refListType, null, this);
		//-using the non-support LoaderManager import gives an error
		//-initLoader seems to be preferrable to restartLoader
		//-refListType used as the identifier

		//Creating the SimpleCursorAdapter for the specified database columns linked to the specified GUI views..
		String[] tmpDatabaseFrom = {ItemTableM.COLUMN_NAME, ItemTableM.COLUMN_ACTIVE,
				ItemTableM.COLUMN_ACTIONS};
		int[] tmpDatabaseTo = {R.id.list_item_titleTextView, R.id.list_item_activeCheckBox,
				R.id.list_item_indicatorRectangle,}; ///, R.id.list_item_tagsTextView
		mCursorAdapter = new SimpleCursorAdapter(
				getActivity(), R.layout.fnk_list_item, null, tmpDatabaseFrom, tmpDatabaseTo,
				android.support.v4.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER); //, refListType

		if(BuildConfig.DEBUG){
			//ItemTableM.COLUMN_KINDSORT_VALUE
		}
		
		mCursorAdapter.setViewBinder(new ViewBinderM((Context)getActivity()));
		
		
		//..using this CursorAdapter as the adapter for this ListFragment
		super.setListAdapter(mCursorAdapter);
		
	}
	
	/**
	 * \brief ViewBinderM is a binder which binds values in the database to parts of the
	 * listitem views.
	 * 
	 * Some bindings are supported by default, in those cases we can simply return false
	 * and the adapter will take care of the mapping. The modifications we have made
	 * include:
	 * + We do the binding for checkboxes ourselves
	 * + ViewBinderM also displays the coloured rectangles indicating no, a single, or
	 * multiple actions.
	 * 
	 * The class is static to avoid memory leaks and holds a WeakReference to a Context.
	 * For more info about memory leaks, please see this link:
	 * http://www.androiddesignpatterns.com/2013/01/inner-class-handler-memory-leak.html
	 * 
	 * Note: When running with the debug window open in eclipse we can see that many
	 * binder threads are created, more than we would expect normally, but this is not an
	 * error according to one info source, and we can see that even though many binder
	 * threads are created, there is a limit
	 */
	private static class ViewBinderM
			implements android.support.v4.widget.SimpleCursorAdapter.ViewBinder{
		private final WeakReference<Context> mWeakRefToContext;

		public ViewBinderM(Context inContext){
			mWeakRefToContext = new WeakReference<Context>(inContext);
		}

		@Override
		public boolean setViewValue(View inView, Cursor inCursor, int inColumnIndex) {
			//if(inView.getId() == R.id.list_item_activeCheckBox){

			if(inColumnIndex == inCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ACTIVE)){
				//Setting status of the checkbox (checked / not checked)
				long tmpActive = Long.parseLong(inCursor.getString(
						inCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ACTIVE)));
				CheckBox tmpCheckBox = ((CheckBox)inView.findViewById(
						R.id.list_item_activeCheckBox));
				if (tmpCheckBox != null){
					tmpCheckBox.setChecked(tmpActive != ItemTableM.FALSE);
				}
				return true;
				//-hilarious if we don't have this, the checkboxes displays the expected state,
				//but we get a number representation as well to the right of the checkboxes! 

			}else if(inColumnIndex == inCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ACTIONS)){
				//Updating the action indications
				String tmpActions = inCursor.getString(inCursor.getColumnIndexOrThrow(
						ItemTableM.COLUMN_ACTIONS));
				LinearLayout tmpRectangle = (LinearLayout)inView.findViewById(
						R.id.list_item_indicatorRectangle);
				if(tmpActions == null || tmpActions.equals(ItemActionsU.ACTIONS_DELINEATOR)){
					tmpRectangle.setVisibility(View.INVISIBLE);
				}else if(ItemActionsU.numberOfActions(tmpActions) == 1){
					tmpRectangle.setVisibility(View.VISIBLE);
					tmpRectangle.setBackgroundColor(mWeakRefToContext.get().getResources()
							.getColor(R.color.one_action));
				}else if(ItemActionsU.numberOfActions(tmpActions) > 1){
					tmpRectangle.setVisibility(View.VISIBLE);
					tmpRectangle.setBackgroundColor(mWeakRefToContext.get().getResources()
							.getColor(R.color.multiple_actions));
				}
				return true;
			}

			//Returning false for other values, which means that default mapping will be done
			return false;
		}
	}
	
	//TODO: Static + Weak Ref
	//used both by ___ and ___
	private class OnClickNewItemListener implements OnClickListener{
		@Override
		public void onClick(View inView) {
			//Creating and inserting the new list item into the database
			ContentValues tmpContentValuesToInsert = new ContentValues();
			tmpContentValuesToInsert.put(ItemTableM.COLUMN_LIST_TYPE, refListType);
			Uri tmpUriOfNewItem = getActivity().getContentResolver().insert(
					ContentProviderM.ITEM_CONTENT_URI, tmpContentValuesToInsert);
	    	
	    //Launching the details fragment for the newly created item
			Intent intent = new Intent(getActivity(), ItemSetupActivityC.class);
			String tmpExtraString = tmpUriOfNewItem.toString();
			intent.putExtra(EXTRA_ITEM_URI, tmpExtraString);
			//-Extracted in SingleFragmentActivityC and sent to DataDetailsFragmentC
			startActivityForResult(intent, 0);

		}
	}
}