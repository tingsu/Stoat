package com.nanoconverter.zlab;

import android.app.Activity;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

	public class Widget_1x1_config extends Activity {
	Button configOkButton;
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	Spinner spinFrom,spinTo,spinBank,spinTheme;
    CheckBox roundedcorner,circle,showupdate,showsource,showcur,checkOnlyFrom,checkOnlyTo;
    TextView bank_preview,cur_preview,rate_preview;
    ImageView update_button;
    LinearLayout widget_preview_layout,config_layout,sourceLayout,curLayout;
    RelativeLayout preview_window;
    String[] all_cur = { "USD", "EUR", "CHF", "GBP", "JPY", "UAH", "RUB", "MDL", "BYR", "PLN", "LTL", "LVL", "AZN", "AUD", "AMD", "BGN", "BRL", "HUF", "DKK", "INR", "KZT", "CAD", "KGS", "CNY", "NOK", "RON", "XDR", "SGD", "TJS", "TRY", "TMT", "UZS", "CZK", "SEK", "ZAR", "KRW", "FOO" };
    String[] all_bank_id = { "CBR", "NBU", "NBRB", "BNM", "AZ", "ECB", "FOREX" };
    String[] theme = {"black", "white"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	 final Context configContext = Widget_1x1_config.this;
	 
	setResult(RESULT_CANCELED);

	     setContentView(R.layout.widget_duel_config);

	     configOkButton = (Button)findViewById(R.id.okconfig);
	     configOkButton.setOnClickListener(configOkButtonOnClickListener);

	     Intent intent = getIntent();
	     Bundle extras = intent.getExtras();
	     if (extras != null) {
	         mAppWidgetId = extras.getInt(
	                 AppWidgetManager.EXTRA_APPWIDGET_ID,
	                 AppWidgetManager.INVALID_APPWIDGET_ID);
	     }
	  
	     if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
	         finish();
	     }

	     spinFrom = (Spinner)findViewById(R.id.spinFrom);
	     spinTo = (Spinner)findViewById(R.id.spinTo);
	     spinBank = (Spinner)findViewById(R.id.spinBank);
	     spinTheme = (Spinner)findViewById(R.id.spinTheme);
	     
	     roundedcorner = (CheckBox)findViewById(R.id.checkRounded);
	     circle = (CheckBox)findViewById(R.id.checkCircle);
	     showupdate = (CheckBox)findViewById(R.id.checkUpdateShow);
	     showsource = (CheckBox)findViewById(R.id.checkBank);
	     showcur = (CheckBox)findViewById(R.id.checkCur);
	     checkOnlyFrom = (CheckBox)findViewById(R.id.checkOnlyFrom);
	     checkOnlyTo= (CheckBox)findViewById(R.id.checkOnlyTo);
	     
	     showupdate.setChecked(true);
	     showsource.setChecked(true);
	     showcur.setChecked(true);
	     
	     bank_preview = (TextView)findViewById(R.id.bank_preview);
	     cur_preview = (TextView)findViewById(R.id.cur_preview);
	     rate_preview = (TextView)findViewById(R.id.rate_preview);
	     update_button = (ImageView)findViewById(R.id.update_button);
	     
	     widget_preview_layout = (LinearLayout)findViewById(R.id.widget_preview_layout);
	     sourceLayout = (LinearLayout)findViewById(R.id.sourceLayout);;
	     curLayout = (LinearLayout)findViewById(R.id.curLayout);;
	     config_layout = (LinearLayout)findViewById(R.id.config_layout);
	     preview_window = (RelativeLayout)findViewById(R.id.preview_window);
	     //config_layout.setBackgroundDrawable(getWallpaper());

	     roundedcorner.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (roundedcorner.isChecked()){
					circle.setChecked(false);
					if (spinTheme.getSelectedItemPosition()==0){
						widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square_rounded));
						bank_preview.setTextColor(Color.WHITE);
						cur_preview.setTextColor(Color.WHITE);
						rate_preview.setTextColor(Color.WHITE);
		    	    	} else {
		    	    	widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square_rounded_white));
						bank_preview.setTextColor(Color.BLACK);
						cur_preview.setTextColor(Color.BLACK);
						rate_preview.setTextColor(Color.BLACK);
		    	    	}
				} else {
					if (spinTheme.getSelectedItemPosition()==0){
						widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square));
						bank_preview.setTextColor(Color.WHITE);
						cur_preview.setTextColor(Color.WHITE);
						rate_preview.setTextColor(Color.WHITE);
		    	    	} else {
		    	    	widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square_white));
						bank_preview.setTextColor(Color.BLACK);
						cur_preview.setTextColor(Color.BLACK);
						rate_preview.setTextColor(Color.BLACK);
		    	    	}
				}
				}});
	     
	     circle.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (circle.isChecked()){
						roundedcorner.setChecked(false);
						if (spinTheme.getSelectedItemPosition()==0){
							widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_circle));
							bank_preview.setTextColor(Color.WHITE);
							cur_preview.setTextColor(Color.WHITE);
							rate_preview.setTextColor(Color.WHITE);
			    	    	} else {
			    	    	widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_circle_white));
							bank_preview.setTextColor(Color.BLACK);
							cur_preview.setTextColor(Color.BLACK);
							rate_preview.setTextColor(Color.BLACK);
			    	    	}
						update_button.setVisibility(View.INVISIBLE);
						showupdate.setChecked(false);
					} else {
						if (spinTheme.getSelectedItemPosition()==0){
							widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square));
							bank_preview.setTextColor(Color.WHITE);
							cur_preview.setTextColor(Color.WHITE);
							rate_preview.setTextColor(Color.WHITE);
			    	    	} else {
			    	    	widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square_white));
							bank_preview.setTextColor(Color.BLACK);
							cur_preview.setTextColor(Color.BLACK);
							rate_preview.setTextColor(Color.BLACK);
			    	    	}
					}
				}});
	     showupdate.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (showupdate.isChecked()){
						update_button.setVisibility(View.VISIBLE);
						circle.setChecked(false);
						if (!roundedcorner.isChecked()){
							if (spinTheme.getSelectedItemPosition()==0){
								widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square));
								bank_preview.setTextColor(Color.WHITE);
								cur_preview.setTextColor(Color.WHITE);
								rate_preview.setTextColor(Color.WHITE);
				    	    	} else {
				    	    	widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square_white));
								bank_preview.setTextColor(Color.BLACK);
								cur_preview.setTextColor(Color.BLACK);
								rate_preview.setTextColor(Color.BLACK);
				    	    	}
							}
					} else {
						update_button.setVisibility(View.INVISIBLE);}
				}});
	     showsource.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (showsource.isChecked()){
						bank_preview.setVisibility(View.VISIBLE);
					} else {
						bank_preview.setVisibility(View.INVISIBLE);}
				}});
	     showcur.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (showcur.isChecked()){
						cur_preview.setVisibility(View.VISIBLE);
						checkOnlyFrom.setEnabled(true);
						checkOnlyTo.setEnabled(true);
						cur_preview.setText(all_cur[spinFrom.getSelectedItemPosition()]+"/"+all_cur[spinTo.getSelectedItemPosition()]);
					} else {
						cur_preview.setVisibility(View.INVISIBLE);
						checkOnlyTo.setChecked(false);
						checkOnlyFrom.setChecked(false);
						checkOnlyFrom.setEnabled(false);
						checkOnlyTo.setEnabled(false);
					}
				}});
	     
	     checkOnlyFrom.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (checkOnlyFrom.isChecked()){
						checkOnlyTo.setChecked(false);
						cur_preview.setText(all_cur[spinFrom.getSelectedItemPosition()]);
					} else {
						cur_preview.setText(all_cur[spinFrom.getSelectedItemPosition()]+"/"+all_cur[spinTo.getSelectedItemPosition()]);
						}
				}});
	     checkOnlyTo.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (checkOnlyTo.isChecked()){
						checkOnlyFrom.setChecked(false);
						cur_preview.setText(all_cur[spinTo.getSelectedItemPosition()]);
					} else {
						cur_preview.setText(all_cur[spinFrom.getSelectedItemPosition()]+"/"+all_cur[spinTo.getSelectedItemPosition()]);
						}
				}});
	     
	     spinFrom.setOnItemSelectedListener(new OnItemSelectedListener() {
	    	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	    	    	if (checkOnlyFrom.isChecked()){
	    	    		cur_preview.setText(all_cur[position]);
	    	    	} else if (checkOnlyTo.isChecked()){
	    	    	} else {
	    	    		cur_preview.setText(all_cur[position]+"/"+all_cur[spinTo.getSelectedItemPosition()]);
	    	    	}
	    	    }
	    	    public void onNothingSelected(AdapterView<?> parentView) {
	    	    }
	    	});
	     spinTo.setOnItemSelectedListener(new OnItemSelectedListener() {
	    	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	    	    	if (checkOnlyFrom.isChecked()){
	    	    	} else if (checkOnlyTo.isChecked()){
	    	    		cur_preview.setText(all_cur[position]);
	    	    	} else {
	    	    		cur_preview.setText(all_cur[spinFrom.getSelectedItemPosition()]+"/"+all_cur[position]);
	    	    	}
	    	    }
	    	    public void onNothingSelected(AdapterView<?> parentView) {
	    	    }
	    	});
	     spinBank.setOnItemSelectedListener(new OnItemSelectedListener() {
	    	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	    	    	bank_preview.setText(all_bank_id[position]);
	    	    }
	    	    public void onNothingSelected(AdapterView<?> parentView) {
	    	    }
	    	});
	     spinTheme.setOnItemSelectedListener(new OnItemSelectedListener() {
	    	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	    	    	if (circle.isChecked()){
	    	    		if (position==0){
						widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_circle));
						bank_preview.setTextColor(Color.WHITE);
						cur_preview.setTextColor(Color.WHITE);
						rate_preview.setTextColor(Color.WHITE);
	    	    		} else {
	    	    		widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_circle_white));
						bank_preview.setTextColor(Color.BLACK);
						cur_preview.setTextColor(Color.BLACK);
						rate_preview.setTextColor(Color.BLACK);
	    	    		}
	    	    	} else if (roundedcorner.isChecked()) {
	    	    		if (position==0){
						widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square_rounded));
						bank_preview.setTextColor(Color.WHITE);
						cur_preview.setTextColor(Color.WHITE);
						rate_preview.setTextColor(Color.WHITE);
		    	    	} else {
		    	    	widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square_rounded_white));
						bank_preview.setTextColor(Color.BLACK);
						cur_preview.setTextColor(Color.BLACK);
						rate_preview.setTextColor(Color.BLACK);
		    	    	}
	    	    	} else {
	    	    		if (position==0){
						widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square));
						bank_preview.setTextColor(Color.WHITE);
						cur_preview.setTextColor(Color.WHITE);
						rate_preview.setTextColor(Color.WHITE);
		    	    	} else {
		    	    	widget_preview_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_square_white));
						bank_preview.setTextColor(Color.BLACK);
						cur_preview.setTextColor(Color.BLACK);
						rate_preview.setTextColor(Color.BLACK);
		    	    	}
	    	    	}
	    	    }
	    	    public void onNothingSelected(AdapterView<?> parentView) {
	    	    }
	    	});
	}

	private Button.OnClickListener configOkButtonOnClickListener
	= new Button.OnClickListener(){

	public void onClick(View arg0) {

	 final Context context = Widget_1x1_config.this;
	 String design = "square";if (roundedcorner.isChecked()){design="rounded";} else if (circle.isChecked()){design="circle";}
	 String update = "show";if (!showupdate.isChecked()){update="false";}
	 String source = "show";if (!showsource.isChecked()){source="false";}
	 String cur = "show";if (!showcur.isChecked()){cur="false";} else 
		 if (checkOnlyFrom.isChecked()){cur="from";} else
			 if (checkOnlyTo.isChecked()){cur="to";}

     SharedPreferences.Editor DuelPrefs = context.getSharedPreferences("DuelPrefs", 0).edit();
     DuelPrefs.putString("CUR_FROM"+mAppWidgetId, all_cur[spinFrom.getSelectedItemPosition()]);
     DuelPrefs.putString("CUR_FROM_ID"+mAppWidgetId, String.valueOf(spinFrom.getSelectedItemId()));
     DuelPrefs.putString("CUR_TO"+mAppWidgetId, all_cur[spinTo.getSelectedItemPosition()]);
     DuelPrefs.putString("CUR_TO_ID"+mAppWidgetId, String.valueOf(spinTo.getSelectedItemId()));
     DuelPrefs.putString("BANK_IS"+mAppWidgetId, all_bank_id[spinBank.getSelectedItemPosition()]);
     DuelPrefs.putString("DESIGN"+mAppWidgetId, design);
     DuelPrefs.putString("UPDATE"+mAppWidgetId, update);
     DuelPrefs.putString("SOURCE"+mAppWidgetId, source);
     DuelPrefs.putString("CUR"+mAppWidgetId, cur);
     DuelPrefs.putString("THEME"+mAppWidgetId, theme[spinTheme.getSelectedItemPosition()]);
     
     DuelPrefs.commit();
     
	 AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

	 Widget_1x1.updateAppWidget(context, appWidgetManager, mAppWidgetId);

	 Intent resultValue = new Intent();
	 resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
	 setResult(RESULT_OK, resultValue);
	 
	 finish();
	}};

	}
