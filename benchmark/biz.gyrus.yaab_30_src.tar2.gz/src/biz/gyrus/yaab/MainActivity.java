/*
 * YAAB concept proof project, (C) Gyrus Solutions, 2011
 * http://www.gyrus.biz
 * 
 */

package biz.gyrus.yaab;

import java.util.Observable;
import java.util.Observer;

import biz.gyrus.yaab.BrightnessController.BrightnessStatus;
import biz.gyrus.yaab.BrightnessController.ServiceStatus;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends ThemedActivity {
	
	private Button _btnStartStop = null;
	private CheckBox _cbAutoStart = null;
	private TextView _txtStatus = null;
	private SeekBar _sbAdjLevel = null;
	private TextView _lblManualAdj = null;
	private TextView _lblBtmComment = null;
	private ProgressBar _pbCurrent = null;
	private Button _btnNight = null;
	private CheckBox _cbAutoNight = null;
	private Handler _h = new Handler();
	private SeekBar _sbNightBrightness = null;
	private Button _btnDonate = null;
	
	private Observer _oServiceStatus = new Observer() {
		
		@Override
		public void update(Observable observable, Object data) {
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					updateControls();
				}
			});
		}
	};
	
	private Observer _oBrightnessStatus = new Observer() {
		
		@Override
		public void update(Observable observable, Object data) {

			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					updateControls();
				}
			});
		}
	};
	
	private Observer _oRunningBrightness = new Observer() {
		
		@Override
		public void update(Observable observable, Object data) {
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					
					_pbCurrent.setProgress(BrightnessController.get().getRunningBrightness());
					
				}
			});
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setTitle(R.string.title_activity_main);
        
        _btnStartStop = (Button) findViewById(R.id.btnOnOff);
        _cbAutoStart = (CheckBox) findViewById(R.id.cbAutostart);
        _txtStatus = (TextView) findViewById(R.id.txtStatus);
        _lblManualAdj = (TextView) findViewById(R.id.lblManualAdjustment);
        _lblBtmComment = (TextView) findViewById(R.id.lblHowToUse);
        _sbAdjLevel = (SeekBar) findViewById(R.id.sbAdjLevel);
        _pbCurrent = (ProgressBar) findViewById(R.id.pbCurrent);
        _btnNight = (Button) findViewById(R.id.btnNight);
        _cbAutoNight = (CheckBox) findViewById(R.id.cbAutonight);
        _sbNightBrightness = (SeekBar) findViewById(R.id.sbNightBrightness);
        _btnDonate = (Button) findViewById(R.id.btnDonate);
        
        _pbCurrent.setMax(Globals.MAX_BRIGHTNESS_INT);
        
        _sbAdjLevel.setMax(Globals.ADJUSTMENT_RANGE_INT);
        _sbAdjLevel.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { BrightnessController.get().blockEffects(false); }
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {	BrightnessController.get().blockEffects(true); }
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser)
				{
					BrightnessController.get().setManualAdjustment(progress - seekBar.getMax()/2);
					BrightnessController.get().updateRunningBrightness();
				}
			}
		});
        
        _btnStartStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(BrightnessController.get().getServiceStatus() != ServiceStatus.Running)
				{
					Log.i(Globals.TAG, "Starting service...");
					saveManualAdjustment();
					ComponentName cn = startService(new Intent(MainActivity.this, LightMonitorService.class));
					if(cn != null)
					{
						Log.i(Globals.TAG, String.format("Service Component name: %s", cn.toShortString()));
					}
					else
						Log.i(Globals.TAG, "Can't start it!");
				}
				else
				{
					Log.i(Globals.TAG, "Stopping service...");
					stopService(new Intent(MainActivity.this, LightMonitorService.class));
				}
			}
        });
        
        _btnNight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				BrightnessController bc = BrightnessController.get();
				
				bc.setForceNight(!bc.isForceNight());
				bc.updateRunningBrightness();
				
				AppSettings s = new AppSettings(MainActivity.this);
				s.setManualNight(bc.isForceNight());
				
				_h.post(new Runnable() {
					
					@Override
					public void run() {
						
						updateControls();
					}
				});
			}
		});
        
        _cbAutoStart.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.i(Globals.TAG, String.format("Saving autostart: %b", isChecked));
				AppSettings s = new AppSettings(MainActivity.this);
				s.setAutostart(isChecked);
			}
		});
        
        _cbAutoNight.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.i(Globals.TAG, String.format("Saving autonight: %b", isChecked));
				AppSettings s = new AppSettings(MainActivity.this);
				s.setAllowAutoNight(isChecked);
				BrightnessController.get().setAutoNight(isChecked);
				BrightnessController.get().updateRunningBrightness();
			}
		});
        
        _sbNightBrightness.setMax(Globals.MAX_NM_BRIGHTNESS - Globals.MIN_NM_BRIGHTNESS);
        _sbNightBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {	}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser)
				{
					BrightnessController bc = BrightnessController.get();
					bc.setRunningDimAmount(bc.getDimAmount(progress + Globals.MIN_NM_BRIGHTNESS));
					bc.updateRunningBrightness();
				}
			}
		});
        
        _btnDonate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
		        startActivity(new Intent("biz.gyrus.yaab.DONATE"));
			}
		});
		
	}
	
	protected void saveManualAdjustment()
	{
		AppSettings as = new AppSettings(this);
		as.setAdjshift(_sbAdjLevel.getProgress() - _sbAdjLevel.getMax()/2);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		BrightnessController.get().blockEffects(false);
		
		BrightnessController.get().removeServiceStatusObserver(_oServiceStatus);
        BrightnessController.get().removeRunningBrightnessObserver(_oRunningBrightness);
        BrightnessController.get().removeBrightnessStatusObserver(_oBrightnessStatus);

		saveManualAdjustment();
		
		AppSettings as = new AppSettings(this);
		as.setNMBrightness(_sbNightBrightness.getProgress() + Globals.MIN_NM_BRIGHTNESS);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		AppSettings s = new AppSettings(this);
        _sbAdjLevel.setProgress(_sbAdjLevel.getMax()/2 + s.getAdjshift());
		_sbNightBrightness.setProgress(s.getNMBrightness() - Globals.MIN_NM_BRIGHTNESS);
        BrightnessController.get().setManualAdjustment(s.getAdjshift());

        if(!BrightnessController.get().isLightSensorPresent(this))
		{
			_txtStatus.setText(R.string.status_nosensor);
			_txtStatus.setTextColor(getResources().getColor(R.color.StatusError));
			
			_btnStartStop.setEnabled(false);
			_btnNight.setEnabled(false);
			
			_cbAutoStart.setEnabled(false);
			_cbAutoStart.setChecked(false);
			_cbAutoNight.setEnabled(false);
			_cbAutoNight.setChecked(false);
			
			_sbAdjLevel.setEnabled(false);
			
			_lblBtmComment.setText(R.string.txt_nolightsensor_sorry);
			
			_pbCurrent.setVisibility(View.GONE);
		}
		else
		{
			_cbAutoStart.setEnabled(true);
			_cbAutoStart.setChecked(s.getAutostart());
			_lblBtmComment.setText(R.string.txt_howto_use);

			_cbAutoNight.setEnabled(true);
			_cbAutoNight.setChecked(s.getAllowAutoNight());
			
			_pbCurrent.setVisibility(View.VISIBLE);
			
			updateControls();
		}
        
        BrightnessController.get().addServiceStatusObserver(_oServiceStatus);
        BrightnessController.get().addRunningBrightnessObserver(_oRunningBrightness);
        BrightnessController.get().addBrightnessStatusObserver(_oBrightnessStatus);
	}
	
	protected void updateControls()
	{
		final ServiceStatus ssCurrent = BrightnessController.get().getServiceStatus();
		
		_btnStartStop.setEnabled(true);
		_btnStartStop.setText((ssCurrent != ServiceStatus.Running)?R.string.btn_start_text:R.string.btn_stop_text);
		_sbAdjLevel.setEnabled(ssCurrent == ServiceStatus.Running);
		
		if(ssCurrent == ServiceStatus.Running)
		{
			switch(BrightnessController.get().getBrightnessStatus())
			{
			case Auto:
				_txtStatus.setText(R.string.brightness_status_auto);
				break;
			case AutoNight:
				_txtStatus.setText(R.string.brightness_status_autonight);
				break;
			case ForceNight:
				_txtStatus.setText(R.string.brightness_status_manualnight);
				break;
			default:
				_txtStatus.setText(R.string.brightness_status_off);
				break;
			}
			_txtStatus.setTextColor(getResources().getColor(R.color.StatusHealthy));
			_pbCurrent.setProgress(BrightnessController.get().getRunningBrightness());
			_btnNight.setEnabled(true);
			_btnNight.setText(BrightnessController.get().isForceNight()?R.string.txt_normal:R.string.txt_night);
			
			if(BrightnessController.get().getBrightnessStatus() == BrightnessStatus.ForceNight || BrightnessController.get().getBrightnessStatus() == BrightnessStatus.AutoNight)
			{
				_lblManualAdj.setText(R.string.txt_night_brightness);
				_sbAdjLevel.setVisibility(View.GONE);
				_sbNightBrightness.setVisibility(View.VISIBLE);
			}
			else
			{
				_lblManualAdj.setText(R.string.manual_adjust);
				_sbAdjLevel.setVisibility(View.VISIBLE);
				_sbNightBrightness.setVisibility(View.GONE);
			}
		}
		if(ssCurrent == ServiceStatus.Stopped)
		{
			_txtStatus.setText(R.string.status_stopped);
			_txtStatus.setTextColor(_lblManualAdj.getTextColors().getDefaultColor());
			_pbCurrent.setProgress(0);
			_btnNight.setEnabled(false);
			
			_lblManualAdj.setText(R.string.manual_adjust);
			_sbAdjLevel.setVisibility(View.VISIBLE);
			_sbNightBrightness.setVisibility(View.GONE);
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater i = getMenuInflater();
		i.inflate(R.menu.activity_main, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId())
		{
		case R.id.miPreferences:
			try
			{
				Intent prefsIntent = new Intent(this, PrefsActivity.class);
				startActivity(prefsIntent);
		        Log.i(Globals.TAG, "Preferences activity started");
			} catch(Exception e)
			{
				Log.e(Globals.TAG, e.getMessage(), e);
			}
			break;
		case R.id.miCredits:
			try
			{
				Intent creditsIntent = new Intent("biz.gyrus.yaab.CREDITS");
		        startActivity(creditsIntent);
		        Log.i(Globals.TAG, "Credits activity started");
			} catch(Exception e)
			{
				Log.e(Globals.TAG, e.getMessage(), e);
			}
			
			return true;
			
		case R.id.miDonate:
			try
			{
				Intent donateIntent = new Intent("biz.gyrus.yaab.DONATE");
		        startActivity(donateIntent);
		        Log.i(Globals.TAG, "Donate activity started");
			} catch(Exception e)
			{
				Log.e(Globals.TAG, e.getMessage(), e);
			}
			
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
