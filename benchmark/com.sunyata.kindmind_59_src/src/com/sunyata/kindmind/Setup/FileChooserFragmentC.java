package com.sunyata.kindmind.Setup;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.util.DbgU;
import com.sunyata.kindmind.util.FileU;

public class FileChooserFragmentC extends ListFragment {
	
	File mDirectoryPath;
	static final String EXTRA_RETURN_VALUE_FROM_FILE_CHOOSER_FRAGMENT = "RETURN_VALUE_FROM_FILECHOOSERFRAGMENT";
	
	public static FileChooserFragmentC newInstance(){
		FileChooserFragmentC retListFragment = new FileChooserFragmentC();
		return retListFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inInflater, ViewGroup inParent, Bundle inSavedInstanceState){
    	View retView = super.onCreateView(inInflater, inParent, inSavedInstanceState);
    	return retView;
	}

	//We get to onActivityCreated after onAttach and onCreateView.
    //Alternatively after onAttach, onCreate and onCreateView
    @Override
    public void onActivityCreated(Bundle inSavedInstanceState){
    	super.onActivityCreated(inSavedInstanceState);

    	this.initialize();
    }
    private void initialize(){
    	
    	mDirectoryPath = new File(
    			Environment.getExternalStorageDirectory().getAbsolutePath());
    	/*-From the javadoc for getExternalStorageDirectory:
    	 * "Note: don't be confused by the word "external" here.
    	 * This directory can better be thought as media/shared storage.
    	 * It is a filesystem that can hold a relatively large amount of
    	 * data and that is shared across all applications (does not
    	 * enforce permissions). Traditionally this is an SD card,
    	 * but it may also be implemented as built-in storage in a
    	 * device that is distinct from the protected internal storage
    	 * and can be mounted as a filesystem on a computer."
    	 */

    	/*
    	List<String> tmpList = new ArrayList<String>();
    	tmpList.add("..");
    	
    	//Setting up the path to the directory to be displayed and the adapter
    	if(mDirectoryPath == null){
    		
    	}
    	*/
    	
    	
    	List<String> tmpList = Arrays.asList(mDirectoryPath.list());
    	
    	final String tUpInTree = "..";
    	
    	tmpList.add(0, tUpInTree);
    	
    	if(tmpList == null || tmpList.size() == 0){
    		Log.w(DbgU.getAppTag(), "No files in directory or directory not present");
    		getActivity().finish();
    		return;
    	}
    	FileChooserListDataAdapterC adapter = new FileChooserListDataAdapterC(tmpList);
		setListAdapter(adapter);
    }
    
	class FileChooserListDataAdapterC extends ArrayAdapter<String>{
		
		public FileChooserListDataAdapterC(List<String> inListData){
			super(getActivity(), android.R.layout.simple_list_item_1, inListData);
		}
		
		@Override
		public View getView(int inPosition, View inConvertView, ViewGroup inParent){
			if (inConvertView == null){
				inConvertView = getActivity().getLayoutInflater().inflate(R.layout.file_list_item, null);
			}
			
			String tmpString = getItem(inPosition);
			
			//Setting a prefix that describes if the item that the user is choosing is a file or a directory
			File tmpFileOrDirectory = new File(mDirectoryPath + "/" + tmpString);
			Log.i(DbgU.getAppTag(), "tmpFileOrDirectory = " + tmpFileOrDirectory);
			String tmpDirectoryOrFileString = "";
			if(tmpFileOrDirectory.isDirectory() == true){
				tmpDirectoryOrFileString = "[Dir]  ";
			}else{
				tmpDirectoryOrFileString = "[File] ";
			}
			TextView tmpDirectoryOrFileTextView = (TextView)inConvertView.findViewById(R.id.file_list_item_directoryOrFile);
			tmpDirectoryOrFileTextView.setText(tmpDirectoryOrFileString);

			//Setting up the area where the name of the file is
			TextView tmpTitleTextView = (TextView)inConvertView.findViewById(R.id.file_list_item_titleTextView);
			tmpTitleTextView.setText(tmpString);
			
			//Setting an on click listener for the whole area
			inConvertView.setOnClickListener(new CustomOnClickListener());

			return inConvertView;
		}

		private class CustomOnClickListener implements OnClickListener{
			public CustomOnClickListener(){}

			@Override
			public void onClick(View inView) {
				
				String tmpFilePath = 
						mDirectoryPath + "/"
						+ (String)((TextView) inView.findViewById(R.id.file_list_item_titleTextView)).getText();

				Intent tmpIntent = new Intent();
				tmpIntent.putExtra(EXTRA_RETURN_VALUE_FROM_FILE_CHOOSER_FRAGMENT, tmpFilePath);
				getActivity().setResult(Activity.RESULT_OK, tmpIntent);
				getActivity().finish();
			}
		}
	}
}
