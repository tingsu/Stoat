package com.threedlite.urforms;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;

import com.ipaulpro.afilechooser.FileChooserActivity;

public class UrformsFileChooserActivity extends FileChooserActivity {

	private String mAttributeName;
	private String mFilePath;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
			Bundle bundle = getIntent().getExtras();
	
			if (bundle != null) mAttributeName = bundle.getString(EnterDataActivity.ATTRIBUTE_NAME);
	
			showFileChooser();
		}
	}

	@Override
	protected void onFileSelect(File file) {
		if (file != null) {
			mFilePath = file.getAbsolutePath();
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString(EnterDataActivity.FILE_NAME, mFilePath); 
			bundle.putString(EnterDataActivity.ATTRIBUTE_NAME, mAttributeName); 
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
		}	
	}
	



}