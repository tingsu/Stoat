package com.frankcalise.h2droid;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends Activity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inflate the layout
        setContentView(R.layout.activity_about);
    }
    
    public void onCloseButtonClick(View v) {
    	finish();
    }
}
