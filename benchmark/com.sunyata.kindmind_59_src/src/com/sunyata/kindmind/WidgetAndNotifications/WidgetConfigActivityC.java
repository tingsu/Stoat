package com.sunyata.kindmind.WidgetAndNotifications;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sunyata.kindmind.R;
import com.sunyata.kindmind.List.ListTypeM;
import com.sunyata.kindmind.util.DbgU;

public class WidgetConfigActivityC extends Activity {

	private int mWidgetId;
	
	private RadioButton mFeelingsRadioButton;
	private RadioButton mNeedsRadioButton;
	private RadioButton mKindnessRadioButton;
	private Button mOkButton;
	private Button mCancelButton;
	private RadioGroup mRadioGroup;

	public static String WIDGET_CONFIG_LIST_TYPE_PREFERENCES = "widgetConfigListTypePreferences";
	//public static String WIDGET_CONFIG_LIST_TYPE_PREFERENCES_DEFAULT = "error";

	
	//https://developer.android.com/reference/android/widget/RadioGroup.html
	@Override
	public void onCreate(Bundle inSavedInstanceState){
		super.onCreate(inSavedInstanceState);
		
		//Loading the layout
		super.setContentView(R.layout.activity_widgetconfig);
		
		//Extracting the id of the widget
		Bundle inExtras = super.getIntent().getExtras();
		if(inExtras != null){
			mWidgetId = inExtras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		//Setting the result to cancelled so that if the activity exits, we will get this result
		super.setResult(RESULT_CANCELED, null);
		
		mRadioGroup = (RadioGroup)this.findViewById(R.id.widgetConfig_radioGroup);
		mFeelingsRadioButton = ((RadioButton)this.findViewById(R.id.widgetConfigFeelings_radioButton));
		mNeedsRadioButton = ((RadioButton)this.findViewById(R.id.widgetConfigNeeds_radioButton));
		mKindnessRadioButton = ((RadioButton)this.findViewById(R.id.widgetConfigKindness_radioButton));
		
		//Setting up the Ok button
		mOkButton = (Button) super.findViewById(R.id.widgetConfigOk_button);
		mOkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//First checking if no radio button has been selected..
				if(mRadioGroup.getCheckedRadioButtonId() == -1){
					//..exiting with RESULT_CANCELED
					setResult(RESULT_CANCELED, null);
					finish();
				}
				
				//Saving the list type to a special preferences file containing widget ids and list types
				int tmpListType = ListTypeM.NOT_SET;
				if(mFeelingsRadioButton.isChecked()){
					tmpListType = ListTypeM.FEELINGS;
				}else if(mNeedsRadioButton.isChecked()){
					tmpListType = ListTypeM.NEEDS;
				}else if(mKindnessRadioButton.isChecked()){
					tmpListType = ListTypeM.KINDNESS;
				}else{
					Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + 
							" Radio button not covered in if statements");
					finish();
				}
				getSharedPreferences(WIDGET_CONFIG_LIST_TYPE_PREFERENCES, Context.MODE_PRIVATE).edit()
						.putInt(String.valueOf(mWidgetId), tmpListType).commit();

				//Exiting with an intent (holding the id as an extra) and RESULT_OK
				Intent retResultIntent = new Intent();
				retResultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
				setResult(RESULT_OK, retResultIntent);
				finish();
			}
		});

		//Setting up the Cancel button
		mCancelButton = (Button) super.findViewById(R.id.widgetConfigCancel_button);
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Setting the result as cancelled and exiting
				setResult(RESULT_CANCELED, null);
				finish();
			}
		});
	}

	/*
	public void onRadioButtonClicked(View inView){
		boolean
		
		switch(inView.getId()){
		case R.id.widgetConfigFeelings_radioButton:
			
			break;
		case R.id.widgetConfigNeeds_radioButton:
			break;
		case R.id.widgetConfigKindness_radioButton:
			break;
		default:
		}
	}
	*/
	
}
