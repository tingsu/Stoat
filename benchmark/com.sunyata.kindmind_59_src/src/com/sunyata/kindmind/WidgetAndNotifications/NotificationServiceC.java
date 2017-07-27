package com.sunyata.kindmind.WidgetAndNotifications;

import java.util.Date;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
//-NotificationCompat is for api lvl 15 and downwards
import com.sunyata.kindmind.util.DatabaseU;
import com.sunyata.kindmind.util.DbgU;
import com.sunyata.kindmind.util.OtherU;

/*
 * Overview: NotificationServiceC both contain the static method for setting alarms (setServiceNotificationSingle)
 *  and also handles the Intent contained in the pending intent given to the alarm manager. Also in this class
 *  is the method setServiceNotificationAll which is called from a broadcast receiver started at boot and which
 *  iterates through all database list items that have a notification
 * Extends: IntentService
 * Sections:
 *  //-------------------Fields and constructor
 *  //-------------------Static methods for setting alarms
 *  //-------------------Overridden IntentService method: onHandleIntent
 * Used in: 
 * Notes: 
 * Improvements: 
 * Documentation: http://developer.android.com/guide/topics/ui/notifiers/notifications.html
 */
public class NotificationServiceC extends IntentService {

	//-------------------Fields and constructor
	
	private static final String TAG = "NotificationServiceC";
	public static final String PREFERENCES_NOTIFICATION_LIST = "NotificationList";
	private static final String ITEM_ID = "NotificationUUID";
	private static final String NOTIFICATION_TITLE = "NotificationTitle";

	public NotificationServiceC() {
		super(TAG);
	}

	
	//-------------------Static methods for setting alarms
	
	/*
	 * Overview: setServiceNotificationAll iterates through the rows in the database and calls
	 *  setServiceNotificationSingle for each row/listitem that has an active notification
	 * Usage: Only BootCompleteReceiverC.onReceive()
	 * Uses app internal: this.setServiceNotificationSingle()
	 */
	public static void setServiceNotificationAll(Context inContext){
		//Creating SQL cursor
		Cursor tItemCr = inContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, null, null, ContentProviderM.sSortType);
		try{
			long tmpNotification = -1;
			Uri tmpItemUri = null;
			if(tItemCr != null && tItemCr.moveToFirst()){

				//Iterating through all the database rows..
				for(tItemCr.moveToFirst(); tItemCr.isAfterLast() == false; tItemCr.moveToNext()){
					
					//..extracting notification data and list item URI
					tmpNotification = tItemCr.getLong(tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_NOTIFICATION));
					tmpItemUri = Uri.withAppendedPath(
							ContentProviderM.ITEM_CONTENT_URI,
							"/" +
							(tItemCr.getLong(tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_ID))));
					
					//..if the notification is active, calling setServiceNotificationSingle
					if(tmpNotification > -1){
						setServiceNotificationSingle(inContext, tmpItemUri);
					}
					
				}
			}else{
				Log.w(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor was null or empty");
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), e);
		}finally{
			if(tItemCr != null){
				tItemCr.close();
			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), new Exception());
			}
		}
	}
	
	
	/*
	 * Overview: setServiceNotificationSingle sets a repeating notification for a single list item
	 * Usage: this.setServiceNotificationAll(), DetailsFragmentC.changeNotificationService()
	 * Uses Android lib: AlarmManager.setRepeating()
	 */
	public static void setServiceNotificationSingle(Context inContext, Uri inItemUri){
		//Extracting several values from the db
		long tItemId = 0;
		String tmpItemIdAsString = "";
		String tmpItemName = "";
		boolean tmpItemNotificationIsActive = false;
		long tmpItemTimeInMilliSeconds = DbgU.NO_VALUE_SET;
		Cursor tItemCr = inContext.getContentResolver().query(
				inItemUri, null, null, null, ContentProviderM.sSortType);
		try{
			if(tItemCr != null && tItemCr.moveToFirst()){

				//Extracting data values from the cursor/database-row for use later in this method..
				tItemId = tItemCr.getLong(tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_ID));
				tmpItemIdAsString = Long.valueOf(tItemId).toString();
				tmpItemName = tItemCr.getString(tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME));
				tmpItemNotificationIsActive = true;
				tmpItemTimeInMilliSeconds = tItemCr.getLong(tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_NOTIFICATION));

			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor was null or empty",
						new Exception());
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), e);
		}finally{
			if(tItemCr != null){
				tItemCr.close();
			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), new Exception());
			}
		}
		
		//Checking whether or not notifications are active for this list item
		if(tmpItemTimeInMilliSeconds == ItemTableM.FALSE ){
			tmpItemNotificationIsActive = false;
		}
		
		//Creation and setup of an Intent pointing to this class which has the onHandleIntent method
		Intent tmpIntent = new Intent(inContext, NotificationServiceC.class);
		tmpIntent.setType(tmpItemIdAsString); //This is what makes the intents differ
		tmpIntent.putExtra(ITEM_ID, tmpItemIdAsString);
		tmpIntent.putExtra(NOTIFICATION_TITLE, tmpItemName);

		//Setting the repeating alarm, or cancelling it (depending on database value)
		PendingIntent tPendingIntentToRepeat = PendingIntent.getService(inContext,
				OtherU.longToIntCutOff(tItemId), tmpIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		AlarmManager tmpAlarmManager = (AlarmManager)inContext.getSystemService(Context.ALARM_SERVICE);

		/*
		long tmpNextTimeInFuture = findNextTimeInFuture(tmpItemTimeInMilliSeconds);
		*/
		
		if(tmpItemNotificationIsActive == true){
			tmpAlarmManager.cancel(tPendingIntentToRepeat); //-Cancelling first
			Log.i(DbgU.getAppTag(), "date = " + new Date(tmpItemTimeInMilliSeconds));
			tmpAlarmManager.set(AlarmManager.RTC, tmpItemTimeInMilliSeconds, tPendingIntentToRepeat);
			/*
			tmpAlarmManager.setRepeating(AlarmManager.RTC, tmpNextTimeInFuture, AlarmManager.INTERVAL_DAY,
					tmpPendingIntentToRepeat);
			*/
			//-PLEASE NOTE: Initial time inUserTimeInMillseconds is not modified with
			// TimeZone.getDefault().getRawOffset() in spite of the documentation for AlarmManager.RTC which indicates
			// that UTC is used.
		}else{
			//Cancel the notifications
			tmpAlarmManager.cancel(tPendingIntentToRepeat);
			tPendingIntentToRepeat.cancel();
		}
	}
	
	
	//-------------------Overridden IntentService method: onHandleIntent
	
	/*
	 * Overview: onHandleIntent is called at a regular interval set by AlarmManager.setRepeating()
	 *  and shows a notification to the user. Also updates the notification value in the database so that
	 *  a notification will be shown in 24 hours
	 * Usage: this.setServiceNotificationSingle()
	 * Uses Android libs: NotificationCompat.Builder, NotificationManager
	 * Notes: Please note that there are two pending intents in this method, one that is sent to the method from the
	 *  alarm, and one that is created inside the method and is used for the action we get when clicking on the
	 *  notification
	 * Improvements: Launch the associated action or actions if any. To make this change we may need to redesign
	 *  MediaFileActionBehaviour
	 * Documentation: https://developer.android.com/reference/android/app/IntentService.html#onHandleIntent%28android.content.Intent%29
	 */
	@Override
	protected void onHandleIntent(Intent inIntent) {
		Log.d(DbgU.getAppTag(), "In method onHandleIntent: One intent received");
		
		//Extracting data attached to the intent coming in to this method
		String tmpIdStringFromListDataItem = inIntent.getStringExtra(ITEM_ID);
		Uri tmpItemUri = DatabaseU.getItemUriFromId(Long.valueOf(tmpIdStringFromListDataItem));
		String tmpTitleStringFromListDataItem = inIntent.getStringExtra(NOTIFICATION_TITLE);

		//Building the pending intent that will start LauncherServiceC
		Intent tmpIntentToAttach = new Intent(this, LauncherServiceC.class);
		tmpIntentToAttach.setData(tmpItemUri);
		PendingIntent tmpPendingIntent = PendingIntent.getService(this, 0, tmpIntentToAttach, 0);
		
		//Building the notification
		Notification tmpNotification = new NotificationCompat.Builder(this)
				.setTicker(tmpTitleStringFromListDataItem)
				.setSmallIcon(R.drawable.kindmind_icon)
				.setContentTitle(tmpTitleStringFromListDataItem)
				.setContentIntent(tmpPendingIntent)
				.setAutoCancel(true)
				.build();
		///setContentText(tmpTitleStringFromListDataItem)
		
		//Displaying the notification
		NotificationManager tmpNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		tmpNotificationManager.notify(tmpIdStringFromListDataItem,
				OtherU.longToIntCutOff(Long.parseLong(tmpIdStringFromListDataItem)), tmpNotification);
		
		//Updating the value of the notification in the database
		Context tmpContext = DatabaseU.getContentProviderContext(this.getApplicationContext());
		long tmpNewNotificationValue = findNextTimeInFuture(tmpContext, tmpItemUri);
		ContentValues tmpContentValue = new ContentValues();
		tmpContentValue.put(ItemTableM.COLUMN_NOTIFICATION, tmpNewNotificationValue);
		tmpContext.getContentResolver().update(tmpItemUri, tmpContentValue, null, null);
	}
	
	/**
	 * \brief findNextTimeInFuture finds and returns the next notification time in the
	 * future that occurs on the same time of day as the old notification time.
	 */
	private static long findNextTimeInFuture(Context inContentProviderContext, Uri inItemUri){
		long retNotificationTime = DbgU.NO_VALUE_SET;
		
		//Extracting notification time from database
		Cursor tmpCursor = inContentProviderContext.getContentResolver().query(inItemUri, null, null, null, null);
		
		try{
			if(tmpCursor != null && tmpCursor.moveToFirst()){
				retNotificationTime = tmpCursor.getLong(
						tmpCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NOTIFICATION));
			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), new Exception());
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), e);
		}finally{
			if(tmpCursor != null){
				tmpCursor.close();
			}
		}
		if(retNotificationTime == DbgU.NO_VALUE_SET){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), new Exception());
		}

		//Loop past previous times until we find a time in the future
		while(retNotificationTime <= System.currentTimeMillis()){
			retNotificationTime = retNotificationTime + AlarmManager.INTERVAL_DAY;
		}
		
		return retNotificationTime;
	}
}
