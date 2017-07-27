package com.sunyata.kindmind.WidgetAndNotifications;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.List.ListTypeM;
import com.sunyata.kindmind.List.SortingAlgorithmServiceM;
import com.sunyata.kindmind.util.DatabaseU;
import com.sunyata.kindmind.util.DbgU;

public class RemoteViewsServiceC extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent inIntent) {
		return new RemoteViewsFactoryC(this.getApplicationContext(), inIntent);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		
		Log.d(DbgU.getAppTag(), DbgU.getMethodName());
	}
	
	/**
	 * \brief RemoteViewsFactoryC works as an adapter giving the process showing/running the home
	 * screen views that can be displayed (the data is taken from the database)
	 * 
	 * Implements: RemoteViewsService.RemoteViewsFactory which is a thin wrapper for an Adapter
	 * 
	 * Used in: Called by RemoteViewsServiceC.onGetViewFactory above
	 * 
	 * Documentation: See Reto's book p596-597
	 * 
	 * \nosubgrouping
	 */
	private class RemoteViewsFactoryC implements RemoteViewsService.RemoteViewsFactory{

		private Context mContext;
		private Cursor mItemCursor;
		private int mWidgetId;
		
		/**
		 * \brief The RemoteViewsFactoryC constructor extracts the id of the widget from an intent, and also
		 * stores an ApplicationContext for later use
		 */
		RemoteViewsFactoryC(Context inContext, Intent inIntent){
			Log.d(DbgU.getAppTag(), DbgU.getMethodName());
			mContext = inContext;
			mWidgetId = inIntent.getExtras().getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			if(mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID){
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " INVALID_APPWIDGET_ID");
				return;
			}
		}

		/**
		 * \brief onCreate sets up the list of data by calling private method createItemCursor
		 * 
		 * The type of list has - in the widget configuration activity - been stored in a special
		 * preferences file which is read in createItemCursor()
		 */
		@Override
		public void onCreate() {
			Log.d(DbgU.getAppTag(), DbgU.getMethodName());
			mItemCursor = createItemCursor();
		}
		@Override
		public void onDestroy() {
			Log.d(DbgU.getAppTag(), DbgU.getMethodName());
			if(mItemCursor != null){
				mItemCursor.close();
			}
			/*
			 * Null-check added because we want to avoid this problem which happens after first restoring a backup file
			 * and then removing the widget:
	java.lang.NullPointerException
	at com.sunyata.kindmind.WidgetAndNotifications.RemoteViewsFactoryC.onDestroy(RemoteViewsServiceC.java:68)
	at android.widget.RemoteViewsService$RemoteViewsFactoryAdapter.onDestroy(RemoteViewsService.java:220)
	at com.android.internal.widget.IRemoteViewsFactory$Stub.onTransact(IRemoteViewsFactory.java:69)
	at android.os.Binder.execTransact(Binder.java:338)
	at dalvik.system.NativeStart.run(Native Method)
			 */
			
			//TODO: add (manual) test for removing, scrolling, clicking in widget list after backup restore
		}

		@Override
		public int getCount() {
			Log.d(DbgU.getAppTag(), DbgU.getMethodName());
			if(mItemCursor != null){
				return mItemCursor.getCount();
			}else{
				return 0;
			}
		}

		@Override
		public long getItemId(int inPosition) {
			Log.d(DbgU.getAppTag(), DbgU.getMethodName());
			if(mItemCursor != null){
				return mItemCursor.getLong(mItemCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ID));
			}else{
				return inPosition;
			}
		}

		@Override
		public boolean hasStableIds() {
			Log.d(DbgU.getAppTag(), DbgU.getMethodName());
			return true;
		}
		
		@Override
		public int getViewTypeCount() {
			Log.d(DbgU.getAppTag(), DbgU.getMethodName());
			return 1;
		}
		
		@Override
		public RemoteViews getLoadingView() {
			Log.d(DbgU.getAppTag(), DbgU.getMethodName());
			return null;
		}
		
		/**
		 * \brief onDataSetChanged updates/recreates the list of data
		 * 
		 * It is called when WidgetManager.notifyAppWidgetViewDataChanged has been invoked
		 * and can be used for updating the list of data. It is the most efficient way since the other
		 * three alternatives all recreate the whole widget).
		 * 
		 * Notes: This callback method will always be called by the system before the Widget is updated
		 * 
		 * Improvements: In the future we may want to implement this method as another way to update the
		 * widget (currently the widget is updated after an interval)
		 * 
		 * Documentation: PA4AD p598
		 */
		@Override
		public void onDataSetChanged() {
			Log.d(DbgU.getAppTag(), DbgU.getMethodName());
			
			//Creating a new cursor
			mItemCursor = createItemCursor();
		}
		
		/**
		 * \brief getViewAt (1) updates the template intent with an URI which can be used for launching
		 * actions and (2) updates and returns a RemoteViews view hierarchy (in our case only one view)
		 *  
		 * Documentation: 
		 * + https://developer.android.com/reference/android/widget/RemoteViewsService.RemoteViewsFactory.html#getViewAt%28int%29
		 * + https://developer.android.com/reference/android/widget/RemoteViews.html
		 */
		@Override
		public RemoteViews getViewAt(int inPosition) {
			Log.d(DbgU.getAppTag(), DbgU.getMethodName() + ", inPosition = " + inPosition);

			//Moving the cursor to the current position
			mItemCursor.moveToPosition(inPosition);
			
			//Extracting values from the database
			String tmpName = mItemCursor.getString(
					mItemCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME));
			long tmpItemId = mItemCursor.getLong(
					mItemCursor.getColumnIndexOrThrow(ItemTableM.COLUMN_ID));
			Uri tmpItemUri = DatabaseU.getItemUriFromId(tmpItemId);
			
			//Setting up the remote views object
			RemoteViews retRemoteViews = new RemoteViews(
					mContext.getPackageName(), R.layout.widget_listitem);
			retRemoteViews.setTextViewText(R.id.widget_listitem_textView, tmpName);
			
			//Adding action URI to the intent template which was set for all the
			//list rows in WidgetProviderC.onUpdate
			Intent tmpFillInIntent = new Intent();
			tmpFillInIntent.setData(tmpItemUri);
			retRemoteViews.setOnClickFillInIntent(R.id.widget_listitem_textView,
					tmpFillInIntent);
			
			return retRemoteViews;
		}
		
		/**
		 * \brief createItemCursor (1) updates sort values with KindSort, and (2) returns a cursor pointing to
		 *  a data set for one of the three ListTypeM values
		 *  
		 *  This method is called from both onCreate and onDataSetChanged
		 */
		private Cursor createItemCursor(){
			Log.d(DbgU.getAppTag(), DbgU.getMethodName());
			
			//Updating sort values
			mContext.startService(new Intent(mContext, SortingAlgorithmServiceM.class));

			//Setting the type of list we like to display
			int tmpListType = mContext.getSharedPreferences(
					WidgetConfigActivityC.WIDGET_CONFIG_LIST_TYPE_PREFERENCES,
					Context.MODE_PRIVATE).getInt(String.valueOf(mWidgetId),
					ListTypeM.NOT_SET);
			if(tmpListType == ListTypeM.NOT_SET){
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " No list type given");
				return null;
			}
			
			//Returning a reference to the cursor
			String tmpSortType = ItemTableM.COLUMN_KINDSORT_VALUE + " DESC";
			String tmpSelection = ItemTableM.COLUMN_LIST_TYPE + "=?";
			String[] tmpSelectionArguments = {String.valueOf(tmpListType)};
			return mContext.getContentResolver().query(
					ContentProviderM.ITEM_CONTENT_URI, null, tmpSelection, tmpSelectionArguments, tmpSortType);
		}
	}
}

