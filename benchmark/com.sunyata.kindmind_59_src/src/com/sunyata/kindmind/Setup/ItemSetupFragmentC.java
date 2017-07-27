package com.sunyata.kindmind.Setup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.List.ListFragmentC;
import com.sunyata.kindmind.List.ListTypeM;
import com.sunyata.kindmind.List.SetupActionOnClickListenerC;
import com.sunyata.kindmind.WidgetAndNotifications.NotificationServiceC;
import com.sunyata.kindmind.util.DbgU;
import com.sunyata.kindmind.util.FileU;
import com.sunyata.kindmind.util.ItemActionsU;
import com.sunyata.kindmind.util.OtherU;

/**
 * \brief Handles data for a single list item (row in the SQL database).
 * 
 * - It handles actions for the kindness list, including layout of buttons and handling
 * of results
 * - It does the setup of the button for choosing time of day for a recurring
 * notification for the needs and kindness lists.
 */
public class ItemSetupFragmentC extends Fragment implements TimePickerFragmentC.OnTimeSetListenerI{
	
	private EditText mItemEditText;
	private Button mNotificationTimePickerButton;
	private Switch mNotificationSwitch;
	private TextView mNotificationTextView;
	private TextView mActionOnClickTextView;
	private Button mNewActionButton;
	
	private int refListType;
	private Boolean mSupressEvents = false;
	private Uri refItemUri; ///< Identifies the item (SQL table row)

	///@name
	///Request codes, also used for identifying button positions
	///{
	private static final int REQ_CODE_IMAGE_CHOOSER = 0;
	private static final int REQ_CODE_AUDIO_CHOOSER = 1;
	private static final int REQ_CODE_VIDEO_CHOOSER = 2;
	private static final int REQ_CODE_CONTACT_CHOOSER = 3;
	private static final int REQ_CODE_BOOKMARK_CHOOSER = 4;
	private static final int REQ_CODE_VIDEO_CHOOSER_INTERNAL = 31; ///< Not used for button position
	///}
	
	/**
	 * \brief newInstance is a static factory method creating and setting up the fragment.
	 * 
	 * It sets the URI that identifies the list item using the setArguments method.
	 * This is done right after creating the actual fragment.
	 * 
	 * @param inAttachedData contains the URI that identifies the list item
	 * @return The newly created fragment
	 */
	public static Fragment newInstance(Object inAttachedData){
		Bundle tmpArguments = new Bundle();
		tmpArguments.putString(ListFragmentC.EXTRA_ITEM_URI, inAttachedData.toString());
		
		Fragment retFragment = new ItemSetupFragmentC(); //-implicit constructor used
		retFragment.setArguments(tmpArguments);
		return retFragment;
	}
	
	///@name Life cycle
	///@{

	/**
	 * \brief onCreateView contains setup of the button and other widgets.
	 * 
	 * Many of the widgets are only shown when we have a specific type of list. These are
	 * the parts of the layout:
	 * - Title [feelings, needs, kindness]
	 * - Notifications [needs, kindness]
	 * - Actions [kindness]
	 */
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		//-super not called because we inflate the view ourselves
		Log.d(DbgU.getAppTag(), DbgU.getMethodName());

		String tItemName = "";

		//Inflating the layout
		View v = inflater.inflate(R.layout.fragment_item_setup, parent, false);
		
		//Using the app icon and left caret for hierarchical navigation
		if(NavUtils.getParentActivityName(getActivity()) != null){
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		//Extracting the URI associated with this list item and storing it a member variable
		String tmpArgumentsString = this.getArguments().getString(
				ListFragmentC.EXTRA_ITEM_URI);
		refItemUri = Uri.parse(tmpArgumentsString);

		//Extracting list type and name from the database
		String[] tmpProjection = {ItemTableM.COLUMN_LIST_TYPE,
				ItemTableM.COLUMN_NAME, ItemTableM.COLUMN_NOTIFICATION};
		Cursor tItemCr = getActivity().getApplicationContext().getContentResolver().query(
				refItemUri, tmpProjection, null, null, null);
		try{
			if(tItemCr != null && tItemCr.moveToFirst()){
				//Setting the list type enum value
				refListType = tItemCr.getInt(tItemCr.getColumnIndexOrThrow(
						ItemTableM.COLUMN_LIST_TYPE));

				//Setting the name of the item
				tItemName = tItemCr.getString(tItemCr.getColumnIndexOrThrow(
						ItemTableM.COLUMN_NAME));
			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor is null or empty",
						new Exception());
				getActivity().finish();
				return v;
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Exception for cursor", e);
			getActivity().finish();
			return v;
		}finally{
			if(tItemCr != null){
				tItemCr.close();
			}else{
				Log.w(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor has already been closed",
						new Exception());
			}
		}

		//--------------Title
		//Storing reference to EditText button containing list item name (from the SQL database)..
		mItemEditText = (EditText)v.findViewById(R.id.listitem_name);
		mItemEditText.setText(tItemName);

		//..adding a text changed listener for when the text changes..
		mItemEditText.addTextChangedListener(new TextWatcherC(getActivity(), refItemUri));

		//--------------Daily Reminder
		mNotificationTextView = (TextView) v.findViewById(R.id.notificationTextView);
		mNotificationTimePickerButton = (Button) v.findViewById(R.id.timePickerButton);
		mNotificationSwitch = (Switch) v.findViewById(R.id.notificationSwitch);

		if(refListType == ListTypeM.NEEDS || refListType == ListTypeM.KINDNESS){

			mNotificationTimePickerButton.setOnClickListener(
					new OnClickOnNotificationListener(this));

			//Setup of notification checkbox which indicates if a recurring notification will be shown..
			mNotificationSwitch.setOnCheckedChangeListener(
					new OnNotificationCheckedChangeListenerC(this));

			this.updateSwitchAndNotificationButton();
		}else{
			mNotificationTextView.setVisibility(View.GONE);
			mNotificationTimePickerButton.setVisibility(View.GONE);
			mNotificationSwitch.setVisibility(View.GONE);
		}

		//--------------Actions on click
		mActionOnClickTextView = (TextView) v.findViewById(R.id.actionOnClickTextView);
		mNewActionButton = (Button) v.findViewById(R.id.newActionButton);
		if(refListType == ListTypeM.KINDNESS){
			mNewActionButton.setOnClickListener(new OnClickOnAddNewActionsListenerC(this));
		}else{
			mActionOnClickTextView.setVisibility(View.GONE);
			mNewActionButton.setVisibility(View.GONE);
		}
		this.updateActionList(v);

		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(DbgU.getAppTag(), DbgU.getMethodName());
		setRetainInstance(true);
		//-Recommended by CommonsWare:
		// http://stackoverflow.com/questions/11160412/why-use-fragmentsetretaininstanceboolean
		// but not in Reto's book: "generally not recommended"
		setHasOptionsMenu(true); //-for the up navigation button (left caret)
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		//Setting focus and bringing up the soft keyboard if no title has been set for this item
		if(mItemEditText.getText().toString().equals(ItemTableM.NO_NAME)){
			mItemEditText.setFocusableInTouchMode(true);
			mItemEditText.requestFocus();
		}
	}
	
	///}
	///@name Other callback methods
	///{

	/**
	 * \brief onActivityResult handles the results from the various activities started when the
	 * user chooses to add one type of new action
	 * 
	 * @todo Which of these are preferable?
	 * - /mnt/sdcard/DCIM/100ANDRO/DSC_0018.jpg
	 * - /media/external/images/media/1993
	 */
	@Override
	public void onActivityResult(int inRequestCode, int inResultCode, Intent inIntent){
		Log.d(DbgU.getAppTag(), DbgU.getMethodName());
		String tmpFilePath = "";

		if(inResultCode != Activity.RESULT_OK){
			Log.w(DbgU.getAppTag(), DbgU.getMethodName()+ " inResultCode was not RESULT_OK");
			return;
		}
		
		//Handling the results that comes back after various intents have been sent
		switch(inRequestCode){
		case REQ_CODE_IMAGE_CHOOSER:
		case REQ_CODE_AUDIO_CHOOSER:
		case REQ_CODE_VIDEO_CHOOSER:
			tmpFilePath = FileU.getFilePathFromMediaIntent(getActivity(), inIntent);
			break;
		case REQ_CODE_VIDEO_CHOOSER_INTERNAL:
			tmpFilePath = inIntent.getStringExtra(
					VideoChooserActivity.EXTRA_RETURN_VALUE_FROM_VIDEO_CHOOSER_FRAGMENT);
			break;
		case REQ_CODE_CONTACT_CHOOSER:
			Uri tmpLookupUri = getContactLookupUri(inIntent);
			if(tmpLookupUri == null){
				return;
			}
			tmpFilePath = tmpLookupUri.toString();
			break;
		case REQ_CODE_BOOKMARK_CHOOSER:
			tmpFilePath = inIntent.getStringExtra(
					BookmarkChooserFragmentC.EXTRA_RETURN_VALUE_FROM_BOOKMARK_CHOOSER_FRAGMENT);
			break;
		default:
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Case not covered");
			return;
		}

		//Updating file/dir string value in the database
		ItemActionsU.addAction(this.getActivity(), refItemUri, tmpFilePath, true);

		//Creating a new action line in the layout
		this.updateActionList(this.getView());
	}
	
  //Callback method called from the TimePickerFragmentC class with the hour and minute values as arguments
	//(The alternative to send the uri of the list item into TimePickerFragmentC would not work well
	// since we still would need to be able to change the notification by simply clickling on the checkbox, 
	// without the user launching the timepicker)
	@Override
	public void fireOnTimeSetEvent(int inHourOfDay, int inMinute) {
		//Use the current date, but change the hour and minute to what was given by the user
		Calendar c = Calendar.getInstance();
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
				inHourOfDay, inMinute, 0);
		long tmpTimeInMilliSeconds = c.getTimeInMillis();
		
		this.updateTimeInDB(tmpTimeInMilliSeconds);

		
		//Update the GUI
		this.updateSwitchAndNotificationButton();
		
		this.changeNotificationService();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu inMenu, MenuInflater inMenuInflater){
		super.onCreateOptionsMenu(inMenu, inMenuInflater);
		inMenuInflater.inflate(R.menu.details_menu, inMenu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem inMenuItem){
		switch (inMenuItem.getItemId()){
		case android.R.id.home:
			//Navigating upwards in the activity hierarchy
			if(NavUtils.getParentActivityName(getActivity()) != null){

				//NavUtils.navigateUpFromSameTask(getActivity()); //-this will recreate MainActivityC

				Intent tmpLaunchParentIntent = NavUtils.getParentActivityIntent(getActivity());
				tmpLaunchParentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				NavUtils.navigateUpTo(getActivity(), tmpLaunchParentIntent);
			}
			return true;
		case R.id.menu_item_delete_listitem:
			AlertDialog.Builder tBuilder= new AlertDialog.Builder(getActivity());
			tBuilder.setTitle(R.string.itemSetup_menu_deleteListItem_dialog_title)
			.setMessage(R.string.itemSetup_menu_deleteListItem_dialog_message)
			.setNegativeButton(R.string.itemSetup_menu_deleteListItem_dialog_cancel,
					new OnClickOnCancelButton())
			.setPositiveButton(R.string.itemSetup_menu_deleteListItem_dialog_delete,
					new OnClickOnConfirmButton(this))
			.create().show();
			return true;
		default:
			return super.onOptionsItemSelected(inMenuItem);
		}
	}
	
	///}
	
	
	
	private Uri getContactLookupUri(Intent iIntent){

		Uri rLookupUri = null;
		
		Cursor tContactsCr = getActivity().getContentResolver().query(
				iIntent.getData(), null, null, null, null);
		try{
			if(tContactsCr != null && tContactsCr.moveToFirst()){

				rLookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI,
						tContactsCr.getString(tContactsCr.getColumnIndexOrThrow(
								ContactsContract.Contacts.LOOKUP_KEY)));

			}else{
				Log.w(DbgU.getAppTag(), DbgU.getMethodName() + " Contacts cursor empty");
				return null;
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Contacts cursor exception");
			return null;
		}finally{
			if(tContactsCr != null){
				tContactsCr.close();
			}
		}
		if(rLookupUri == null){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " tmpLookupUri is null");
		}
		
		return rLookupUri;
	}
	
	private void updateTimeInDB(long inTimeInMilliSeconds) {
		long tmpTimeInMilliSeconds = inTimeInMilliSeconds;
		
		//Updating the time for the alarm so that it is not triggered now
		final int tExtraBufferTime = 1000 * 5;
		if(tmpTimeInMilliSeconds != ItemTableM.FALSE
				&& tmpTimeInMilliSeconds < Calendar.getInstance().getTimeInMillis()
				+ tExtraBufferTime){
			tmpTimeInMilliSeconds = tmpTimeInMilliSeconds + 1000 * 60 * 60 * 24;
		}
		
		//Updating the SQL column "NOTIFICATION" with the new value
		ContentValues tmpContentValues = new ContentValues();
		tmpContentValues.put(ItemTableM.COLUMN_NOTIFICATION, tmpTimeInMilliSeconds);
		getActivity().getContentResolver().update(refItemUri, tmpContentValues, null, null);
	}


	private void updateSwitchAndNotificationButton() {
		
		//Getting the value that will be used for both updates..
		Cursor tmpCursor = getActivity().getContentResolver().query(refItemUri, null, null, null, null);
		tmpCursor.moveToFirst();
		long tmpTimeInMilliseconds = tmpCursor.getLong(
				tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NOTIFICATION));
		tmpCursor.close();

		//..preparing extraction of minutes and hours from the result
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(tmpTimeInMilliseconds);
		
		if(tmpTimeInMilliseconds != ItemTableM.FALSE){
			//Updating the time picker button
			String tmpMinuteString = "" + c.get(Calendar.MINUTE);
			if(tmpMinuteString.length() == 1){tmpMinuteString = "0" + tmpMinuteString;}
			mNotificationTimePickerButton.setText(c.get(Calendar.HOUR_OF_DAY) + ":" + tmpMinuteString);
		}else{
			mNotificationTimePickerButton.setText("--:--");
		}
		
		//Updating the switch
		mSupressEvents = true;
		mNotificationSwitch.setChecked(tmpTimeInMilliseconds != ItemTableM.FALSE);
		mSupressEvents = false;
	}

	
	//Updates the list of actions
	private void updateActionList(View inView) {

		LinearLayout tmpVerticalList = (LinearLayout)inView.findViewById(R.id.actionsLinearLayout);

		//Clearing the layout
		tmpVerticalList.removeAllViews();

		LayoutInflater tmpLayoutInflater = LayoutInflater.from(this.getActivity().getApplicationContext());


		String[] tmpProjection = {ItemTableM.COLUMN_ACTIONS};
		Cursor tmpItemCur = getActivity().getContentResolver().query(
				this.refItemUri, tmpProjection, null, null, null);
		tmpItemCur.moveToFirst();
		String tmpActionsString = tmpItemCur.getString(tmpItemCur.getColumnIndexOrThrow(ItemTableM.COLUMN_ACTIONS));
		ArrayList<String> tmpActionsArrayList = ItemActionsU.actionsStringToArrayList(tmpActionsString);

		LinearLayout tmpActionItem;
		for(String action : tmpActionsArrayList){

			tmpActionItem = (LinearLayout)tmpLayoutInflater.inflate(
					R.layout.action_list_item, tmpVerticalList, false); //-please note "attachToRoot = false"

			TextView tmpTextView = (TextView)tmpActionItem.findViewById(R.id.action_list_item_itemStringTextView);
			tmpTextView.setText(action);

			//Launching the action if the user presses the text
			tmpTextView.setOnClickListener(new SetupActionOnClickListenerC(getActivity(), action));

			Button tmpDeleteButton = (Button)tmpActionItem.findViewById(
					R.id.action_list_item_deleteButton);

			//Using the action name as tag so we can remove this when the delete button is clicked
			tmpDeleteButton.setTag(action);

			//TODO
			tmpDeleteButton.setOnClickListener(new OnClickOnDeleteActionButtonListener(
					this, refItemUri));

			//Adding the action to the list
			tmpVerticalList.addView(tmpActionItem);
		}
	}

	private void changeNotificationService(){
		NotificationServiceC.setServiceNotificationSingle(getActivity().getApplicationContext(), refItemUri);
	}
	
	private static class OnClickOnAddNewActionsListenerC implements OnClickListener {
	
		private final WeakReference<ItemSetupFragmentC> mWeakRefToItemSetupFragment;
		private ArrayAdapter<CharSequence> mTypeChooserButtonAdapter;
		
		public OnClickOnAddNewActionsListenerC(ItemSetupFragmentC iItemSetupFragment){
			mWeakRefToItemSetupFragment = new WeakReference<ItemSetupFragmentC>(
					iItemSetupFragment);
			
			CharSequence[] tArray = new String[5];
			
			
  		tArray[REQ_CODE_IMAGE_CHOOSER] = iItemSetupFragment.getActivity()
  				.getResources().getString(R.string.itemSetup_imageChooser);
  		
  		tArray[REQ_CODE_AUDIO_CHOOSER] = iItemSetupFragment.getActivity()
  				.getResources().getString(R.string.itemSetup_audioChooser);
  				
  		tArray[REQ_CODE_VIDEO_CHOOSER] = iItemSetupFragment.getActivity()
  				.getResources().getString(R.string.itemSetup_videoChooser);

  		tArray[REQ_CODE_CONTACT_CHOOSER] = iItemSetupFragment.getActivity()
  				.getResources().getString(R.string.itemSetup_contactChooser);

  		tArray[REQ_CODE_BOOKMARK_CHOOSER] = iItemSetupFragment.getActivity()
  				.getResources().getString(R.string.itemSetup_bookmarkChooser);

  		List<CharSequence> tArrayList = Arrays.asList(tArray);

  		/*
  		tmpArrayList.set(REQ_CODE_IMAGE_CHOOSER,
  				iItemSetupFragment.getActivity().getResources().getString(
  						R.string.image_file_chooser_button_title));
  		tmpArrayList.add(REQ_CODE_AUDIO_CHOOSER,
  				iItemSetupFragment.getActivity().getResources().getString(
  						R.string.audio_file_chooser_button_title));
  		tmpArrayList.add(REQ_CODE_VIDEO_CHOOSER,
  				iItemSetupFragment.getActivity().getResources().getString(
  						R.string.video_file_chooser_button_title));
  		tmpArrayList.add(REQ_CODE_CONTACT_CHOOSER,
  				iItemSetupFragment.getActivity().getResources().getString(
  						R.string.contact_chooser_button_title));
  		tmpArrayList.add(REQ_CODE_BOOKMARK_CHOOSER,
  				iItemSetupFragment.getActivity().getResources().getString(
  						R.string.bookmark_chooser_button_title));
  		 */
  		//ArrayList<CharSequence> tmpArrayList = new ArrayList<CharSequence>();

  		
  		mTypeChooserButtonAdapter = new ArrayAdapter<CharSequence>(
  				iItemSetupFragment.getActivity().getApplicationContext(),
  				android.R.layout.simple_list_item_1, tArrayList);
		}
		
		@Override
		public void onClick(View v) {
			new AlertDialog.Builder(mWeakRefToItemSetupFragment.get().getActivity())
			.setTitle("Type of action").setAdapter(
					mTypeChooserButtonAdapter,
					new OnClickOnActionTypeListener(mWeakRefToItemSetupFragment.get())).create().show();
		}
	}
	private static class TextWatcherC implements TextWatcher{

		private final WeakReference<Activity> mWeakRefToActivity;
		private final WeakReference<Uri> mWeakRefToItemUri;
		
		public TextWatcherC(Activity iActivity, Uri iRefItemUri){
			mWeakRefToActivity = new WeakReference<Activity>(iActivity);
			mWeakRefToItemUri = new WeakReference<Uri>(iRefItemUri);
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			try{
				//..update the SQL database
				ContentValues tContentVals = new ContentValues();
				tContentVals.put(ItemTableM.COLUMN_NAME, s.toString());
				mWeakRefToActivity.get().getContentResolver().update(
						mWeakRefToItemUri.get(), tContentVals, null, null);
			}catch(NullPointerException npe){
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + "NPE");
			}
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void afterTextChanged(Editable s) {}
	}
	
	private static class OnNotificationCheckedChangeListenerC implements OnCheckedChangeListener{
		
		private final WeakReference<ItemSetupFragmentC> mWeakRefToFragment;
		
		public OnNotificationCheckedChangeListenerC(ItemSetupFragmentC iItemSetupFragment){
			mWeakRefToFragment = new WeakReference<ItemSetupFragmentC>(iItemSetupFragment);
		}
		
		@Override
		public void onCheckedChanged(CompoundButton inCompoundButton, boolean inChecked) {
			mWeakRefToFragment.get().fireCheckedChangedEvent();
		}
	}

	public void fireCheckedChangedEvent() {
		if(mSupressEvents == false){
			//Set the time in the database table
			if(mNotificationSwitch.isChecked() == false){
				updateTimeInDB(ItemTableM.FALSE);
			}else{
  				updateTimeInDB(Calendar.getInstance().getTimeInMillis());
  				// + 1000 * 60 * 60 * 24
  				//-Minus one minute so that the alarm is not set off
			}
			
			//Update GUI
			ItemSetupFragmentC.this.updateSwitchAndNotificationButton();
			
			//Update notification
			ItemSetupFragmentC.this.changeNotificationService();
		}else{
			Log.w(DbgU.getAppTag(), DbgU.getMethodName() +
					" mSupressEvents was true when calling this method, no update done");
		}
	}
	
	private static class OnClickOnNotificationListener implements OnClickListener{
		private final WeakReference<ItemSetupFragmentC> mWeakRefToFragment;
		
		public OnClickOnNotificationListener(ItemSetupFragmentC iItemSetupFragment){
			mWeakRefToFragment = new WeakReference<ItemSetupFragmentC>(iItemSetupFragment);
		}
		
		@Override
		public void onClick(View v) { //-Alt: Using the xml property "android:onClick"
			//..starting a new (app internal) fragment for for choosing time of day
			DialogFragment tmpTimePickerFragment = TimePickerFragmentC.newInstance(
					mWeakRefToFragment.get());
			tmpTimePickerFragment.show(mWeakRefToFragment.get().getFragmentManager(), "TimePicker");
		}
	}
	
	private static class OnClickOnDeleteActionButtonListener implements OnClickListener{

		private final WeakReference<ItemSetupFragmentC> mWeakRefToItemSetupFragment;
		private final WeakReference<Uri> mWeakRefToItemUri;
		private WeakReference<View> mWeakRefToOnClickView = null;

		public OnClickOnDeleteActionButtonListener(ItemSetupFragmentC iItemSetupFragment,
				Uri iRefItemUri){
			mWeakRefToItemSetupFragment = new WeakReference<ItemSetupFragmentC>(
					iItemSetupFragment);
			mWeakRefToItemUri = new WeakReference<Uri>(iRefItemUri);
		}

		@Override
		public void onClick(View v) {
			mWeakRefToOnClickView = new WeakReference<View>(v);
			//Read the current string containing the actions
			String tmpActions = "";
			String[] tmpProjection = {ItemTableM.COLUMN_ACTIONS};

			Cursor tItemCr = mWeakRefToItemSetupFragment.get().getActivity()
					.getContentResolver().query(
							mWeakRefToItemUri.get(), tmpProjection, null, null, null);
			try{
				if(tItemCr != null && tItemCr.moveToFirst()){

					tmpActions = tItemCr.getString(tItemCr.getColumnIndexOrThrow(
							ItemTableM.COLUMN_ACTIONS)); 

				}else{
					Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor is null or empty",
							new Exception());
					//error handling
				}
			}catch(Exception e){
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Exception for cursor", e);
				//error handling
			}finally{
				if(tItemCr != null){
					tItemCr.close();
				}else{
					Log.w(DbgU.getAppTag(), DbgU.getMethodName() +
							" Cursor was null when trying to close it");
				}
			}

			//Removing the matching string
			tmpActions = ItemActionsU.removeAction(
					tmpActions, mWeakRefToOnClickView.get().getTag().toString());

			//Update the database string
			ContentValues tmpContentValues = new ContentValues();
			tmpContentValues.put(ItemTableM.COLUMN_ACTIONS, tmpActions);
			mWeakRefToItemSetupFragment.get().getActivity().getContentResolver().update(
					mWeakRefToItemUri.get(), tmpContentValues, null, null);

			mWeakRefToItemSetupFragment.get().updateActionList(
					mWeakRefToOnClickView.get().getRootView());
			//-not 100% sure how getRootView works
		}
	}
	
	private static class OnClickOnCancelButton implements DialogInterface.OnClickListener{
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			return;
		}
	}
	
	private static class OnClickOnConfirmButton implements DialogInterface.OnClickListener{
		private final WeakReference<ItemSetupFragmentC> mWeakRefToItemSetupFragment;
		public OnClickOnConfirmButton(ItemSetupFragmentC iItemSetupFragment){
			mWeakRefToItemSetupFragment = new WeakReference<ItemSetupFragmentC>(
					iItemSetupFragment);
		}
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			//Removing any alarms
			mWeakRefToItemSetupFragment.get().updateTimeInDB(ItemTableM.FALSE);
			mWeakRefToItemSetupFragment.get().changeNotificationService();

			//This may not be necessary:
			//Updating the adapter in ListFragmentC
	    //this.updateCursorAdapter();
			
			//Updating the app widgets
			OtherU.updateWidgets(mWeakRefToItemSetupFragment.get().getActivity());

			//Remove the list item from the database
			mWeakRefToItemSetupFragment.get().getActivity().getContentResolver().delete(
					mWeakRefToItemSetupFragment.get().refItemUri, null, null);
			
			//Exiting the details view of the removed item
			mWeakRefToItemSetupFragment.get().getActivity().finish();
		}
	}
	
	private static class OnClickOnActionTypeListener implements DialogInterface.OnClickListener {
		private final WeakReference<ItemSetupFragmentC> mWeakRefToItemSetupFragment;
		
		public OnClickOnActionTypeListener(ItemSetupFragmentC iItemSetupFragment){
			mWeakRefToItemSetupFragment = new WeakReference<ItemSetupFragmentC>(
					iItemSetupFragment);
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {

			switch(which){
			case REQ_CODE_IMAGE_CHOOSER:
				//Setup of image chooser button..
				//..using an external image app for choosing an image
				final Intent tmpImageIntent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //-Images
				mWeakRefToItemSetupFragment.get().startActivityForResult(
						tmpImageIntent, REQ_CODE_IMAGE_CHOOSER);
				//-results handled below in the "onActivityResult" method
				break;
				
			case REQ_CODE_AUDIO_CHOOSER:
				final Intent tmpAudioIntent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
				mWeakRefToItemSetupFragment.get().startActivityForResult(
						tmpAudioIntent, REQ_CODE_AUDIO_CHOOSER);
				break;
					
			case REQ_CODE_VIDEO_CHOOSER:
				final Intent tmpVideoIntent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
				try{
					mWeakRefToItemSetupFragment.get().startActivityForResult(
							tmpVideoIntent, REQ_CODE_VIDEO_CHOOSER);
				}catch(Exception e){
					//There is a bug in Android that gives an error when launching
					//this intent for some Android versions. More info:
					//http://stackoverflow.com/questions/19181432/java-lang-securityexception-permission-denial-intent-in-new-version-4-3
					//http://code.google.com/p/android/issues/detail?id=60725
					Log.w(DbgU.getAppTag(), DbgU.getMethodName()
							+ " In some Android versions the video chooser activity is not exported, "
							+ "using internal instead");

					//Launching internal video chooser instead
					final Intent tInternalVideoIntent = new Intent(
							mWeakRefToItemSetupFragment.get().getActivity(),
							VideoChooserActivity.class);
					mWeakRefToItemSetupFragment.get().startActivityForResult(
							tInternalVideoIntent, REQ_CODE_VIDEO_CHOOSER_INTERNAL);
				}
				break;
				
			case REQ_CODE_CONTACT_CHOOSER:
				final Intent tmpContactIntent = new Intent(
						Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
				mWeakRefToItemSetupFragment.get().startActivityForResult(tmpContactIntent, REQ_CODE_CONTACT_CHOOSER);
				break;
				
			case REQ_CODE_BOOKMARK_CHOOSER:
				final Intent tmpBookmarkIntent = new Intent(
						mWeakRefToItemSetupFragment.get().getActivity(), BookmarkChooserActivityC.class);
				//-Extracted in SingleFragmentActivityC
				mWeakRefToItemSetupFragment.get().startActivityForResult(
						tmpBookmarkIntent, REQ_CODE_BOOKMARK_CHOOSER); //Calling FileChooserActivityC
				break;
				
				/*
			case 5: //--------------Custom file
    			//Alternative solution that searches through a volume:
    			// http://stackoverflow.com/questions/10384080/mediastore-uri-to-query-all-types-of-files-media-and-non-media
    			//..starting a new (app internal) activity (and fragment) for for choosing a file
				final Intent customFileIntent = new Intent(getActivity(), FileChooserActivityC.class);
				customFileIntent.putExtra(ListFragmentC.EXTRA_AND_BUNDLE_LIST_TYPE, refListType.toString());
    			//-Extracted in SingleFragmentActivityC
    			startActivityForResult(customFileIntent, REQUEST_CUSTOMFILECHOOSER);
    			//-Calling FileChooserActivityC
				break;
				 */
				
			default:
				break;
			}

			dialog.dismiss();
		}
	}
}