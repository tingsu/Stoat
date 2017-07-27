package com.sunyata.kindmind.WidgetAndNotifications;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.sunyata.kindmind.Main.MainActivityC;
import com.sunyata.kindmind.Main.ToastOrActionC;
import com.sunyata.kindmind.util.DbgU;

/*
 * Overview: LauncherServiceC starts actions using OnClickToastOrAction.kindAction after the user has
 *  (1) clicked on a notification for an item with an attached action or (2) clicked in a widget list
 *  where the item had an attached action
 * Extends: IntentService
 * Used in: NotificationServiceC and RemoteViewsFactoryC (in RemoteViewsServiceC.java)
 * Notes: Please note that even though NotificationServiceC has its own onHandleIntent method, this cannot be used
 *  for launching since that method is used for displaying the notification itself
 * Improvements: 
 * Documentation: 
 */
public class LauncherServiceC extends IntentService {

	private static final String TAG = "LauncherServiceC";
	
	public static final String EXTRA_START_MAIN_ACTIVITY_RESULT = "start_main_activity_result";
	
	public LauncherServiceC() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent inIntent) {
		Log.d(DbgU.getAppTag(), DbgU.getMethodName());
		
		Uri tmpItemUri = inIntent.getData();
		if(tmpItemUri != null){
			//Marking the item
			Intent tmpMainActivityIntent = new Intent(getApplicationContext(), MainActivityC.class);
			tmpMainActivityIntent.setFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK
					|Intent.FLAG_ACTIVITY_CLEAR_TASK);
			tmpMainActivityIntent.putExtra(MainActivityC.EXTRA_URI_AS_STRING, tmpItemUri.toString());
			startActivity(tmpMainActivityIntent); //-intent used in MainActivityC.onCreate

			//Waiting for the application to launch for two reasons:
			// 1. otherwise we will not be able to reach the database (probably because when not
			// (even the process exists we cannot even reach the contentprovider since the manifest file
			// is probably not loaded)
			// 2. We want to show the user that the option he has selected is active in the list
			
			
			
			
			
			
			
			try {
				Thread.sleep(1800);
			} catch (InterruptedException e) {
				Log.e(DbgU.getAppTag(), DbgU.getMethodName() + " Thread.sleep interrupted");
				e.printStackTrace();
			}
			
			////Context tmpContentProviderContext = Utils.getContentProviderContext(getApplicationContext());
			ToastOrActionC.randomKindAction(getApplicationContext(), tmpItemUri);
		}
	}
}