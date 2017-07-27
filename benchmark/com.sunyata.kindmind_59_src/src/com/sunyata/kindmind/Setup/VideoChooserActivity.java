package com.sunyata.kindmind.Setup;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.sunyata.kindmind.util.DbgU;

public class VideoChooserActivity extends ListActivity {

	public static final String EXTRA_RETURN_VALUE_FROM_VIDEO_CHOOSER_FRAGMENT =
			"RETURN_VALUE_FROM_VIDEO_CHOOSER_FRAGMENT";

	private SimpleCursorAdapter mCursorAdapter;
	
	@Override
	public void onCreate(Bundle iSavedInstanceState){
		super.onCreate(iSavedInstanceState);
		
		String[] tFrom = new String[]{
				MediaStore.Video.Media.DISPLAY_NAME,
				MediaStore.Video.Media.DATA};
		int[] tTo = new int[]{
				android.R.id.text1,
				android.R.id.text2};
		
		

		Cursor[] tCursorArray = new Cursor[2];
		
		Cursor tExternalMediaCursor = getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
		tCursorArray[0] = tExternalMediaCursor;
		
		Cursor tInternalMediaCursor = getContentResolver().query(
				MediaStore.Video.Media.INTERNAL_CONTENT_URI, null, null, null, null);
		tCursorArray[1] = tInternalMediaCursor;
		
		MergeCursor tMergeCursor = new MergeCursor(tCursorArray);

		Log.d(DbgU.getAppTag(), DbgU.getMethodName()
				+ " tMergeCursor.getCount() = " + tMergeCursor.getCount());
		//PLEASE NOTE: The emulator may have to be restarted for the system to detect new files
		
		mCursorAdapter = new SimpleCursorAdapter(
				this, android.R.layout.simple_list_item_2, tMergeCursor,
				tFrom, tTo, 0);
		
		setListAdapter(mCursorAdapter);
		
		
		
		super.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View inView, int inPosition, long inId) {

				String tmpVideoUri = (String)((TextView) inView
						.findViewById(android.R.id.text2)).getText();

				Intent tmpIntent = new Intent();
				tmpIntent.putExtra(EXTRA_RETURN_VALUE_FROM_VIDEO_CHOOSER_FRAGMENT, tmpVideoUri);
				setResult(Activity.RESULT_OK, tmpIntent);
				finish();
			}
		});
	}
}