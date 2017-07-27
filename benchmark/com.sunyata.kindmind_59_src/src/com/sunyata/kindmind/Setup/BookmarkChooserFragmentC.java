package com.sunyata.kindmind.Setup;

import com.sunyata.kindmind.util.DbgU;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Browser;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BookmarkChooserFragmentC extends ListFragment {

	public static final String EXTRA_RETURN_VALUE_FROM_BOOKMARK_CHOOSER_FRAGMENT =
			"RETURN_VALUE_FROM_BOOKMARKCHOOSERFRAGMENT";
	
	public static BookmarkChooserFragmentC newInstance(){
		BookmarkChooserFragmentC retListFragment = new BookmarkChooserFragmentC();
		return retListFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inInflater, ViewGroup inParent, Bundle inSavedInstanceState){
    	View retView = super.onCreateView(inInflater, inParent, inSavedInstanceState);
    	return retView;
	}


	//-------------------Methods for LoaderManager.LoaderCallbacks<Cursor>
	
	private SimpleCursorAdapter mCursorAdapter;

	private void updateListWithNewData(){
		
		/*
		String[] tmpProjection = new String[]{
				Browser.BookmarkColumns._ID, Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL};
		*/
		String[] tmpDatabaseFrom = new String[]{
				Browser.BookmarkColumns.TITLE,
				Browser.BookmarkColumns.URL};
		int[] tmpDatabaseTo = new int[]{
				android.R.id.text1,
				android.R.id.text2};
		
		String tmpSelection = android.provider.Browser.BookmarkColumns.BOOKMARK;
		
		ContentResolver tmpContentResolver = getActivity().getContentResolver();
		Cursor tmpBookmarksCursorForAdapter = tmpContentResolver.query(
				android.provider.Browser.BOOKMARKS_URI, null, tmpSelection, null, null);
		// The selection "android.provider.Browser.BOOKMARKS_URI" gives only the bookmarks (not any history)
		// "Browser.getAllBookmarks(tmpContentResolver);" will only give the urls
		
		mCursorAdapter = new SimpleCursorAdapter(
				getActivity(), android.R.layout.simple_list_item_2, tmpBookmarksCursorForAdapter,
				tmpDatabaseFrom, tmpDatabaseTo, 0);
		
		setListAdapter(mCursorAdapter);
		
		//Not closing the cursor since it is used for the adapter
	}

	@Override
	public void onActivityCreated(Bundle inSavedInstanceState){
		super.onActivityCreated(inSavedInstanceState);
		Log.d(DbgU.getAppTag(), DbgU.getMethodName());


		this.updateListWithNewData();


		super.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View inView, int inPosition, long inId) {

				String tmpBookmarkUrl = (String)((TextView) inView.findViewById(android.R.id.text2)).getText();

				Intent tmpIntent = new Intent();
				tmpIntent.putExtra(EXTRA_RETURN_VALUE_FROM_BOOKMARK_CHOOSER_FRAGMENT, tmpBookmarkUrl);
				getActivity().setResult(Activity.RESULT_OK, tmpIntent);
				getActivity().finish();
			}
		});
	}
}
