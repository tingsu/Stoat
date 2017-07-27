package biz.gyrus.yaab;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import biz.gyrus.yaab.BrightnessController.BrightnessStatus;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class RangesActivity extends ThemedActivity {

	private SeekBar _sbMin = null;
	private SeekBar _sbMax = null;
	
	private SeekBar _sbNightThreshold = null;
	private SeekBar _sbNightBrightness = null;
	
	private TextView _txtRunningReading = null;
	private TextView _txtCurrentTheshold = null;
	
	private TextView _txtNotAutoWarn = null;
	
	private Button _btnTestNight = null;
	private boolean _bInTest = false;
	
	private DecimalFormat _df = new DecimalFormat("#.##");
	
	private boolean _bLowNightmodeValues = false;
	
	private OnSeekBarChangeListener _sbMinMaxListener = new OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if(fromUser)
			{
				BrightnessController bc = BrightnessController.get();
				if(seekBar == _sbMin)
				{
					if(progress > _sbMax.getProgress())
					{
						_sbMax.setProgress(progress);
						bc.setBrightnessMax(progress + Globals.MIN_BRIGHTNESS_INT);
					}
					bc.setBrightnessMin(progress + Globals.MIN_BRIGHTNESS_INT);
				}
				else if(seekBar == _sbMax)
				{
					if(progress < _sbMin.getProgress())
					{
						_sbMin.setProgress(progress);
						bc.setBrightnessMin(progress + Globals.MIN_BRIGHTNESS_INT);
					}
					bc.setBrightnessMax(progress + Globals.MIN_BRIGHTNESS_INT);
				}
				bc.updateRunningBrightness();
			}
		}
	}; 
	
	private Observer _oRunningLightSensorValue = new Observer() {
		
		@Override
		public void update(Observable observable, Object data) {
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					
					Float fval = BrightnessController.get().getRunningReading();
					if(fval != null)
						_txtRunningReading.setText(_df.format(fval));
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.avtivity_ranges);
		
		_sbMin = (SeekBar) findViewById(R.id.sbMin);
		_sbMax = (SeekBar) findViewById(R.id.sbMax);
		_sbNightBrightness = (SeekBar) findViewById(R.id.sbNightBrightness);
		_sbNightThreshold = (SeekBar) findViewById(R.id.sbNightThreshold);
		_txtRunningReading = (TextView) findViewById(R.id.txtLastReading);
		_txtCurrentTheshold = (TextView) findViewById(R.id.txtCurrentThreshold);
		_btnTestNight = (Button) findViewById(R.id.btnTestBrightness);
		_txtNotAutoWarn = (TextView) findViewById(R.id.txtNotAutoWarn);
		
		_sbMin.setMax(Globals.MAX_BRIGHTNESS_INT - Globals.MIN_BRIGHTNESS_INT);
		_sbMax.setMax(Globals.MAX_BRIGHTNESS_INT - Globals.MIN_BRIGHTNESS_INT);
		
		_sbMin.setOnSeekBarChangeListener(_sbMinMaxListener);
		_sbMax.setOnSeekBarChangeListener(_sbMinMaxListener);
		
		_sbNightBrightness.setMax(Globals.MAX_NM_BRIGHTNESS - Globals.MIN_NM_BRIGHTNESS);
		_sbNightThreshold.setMax(Globals.MAX_NM_THRESHOLD - Globals.MIN_NM_THRESHOLD);
		
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
		
		_sbNightThreshold.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser)
				{
					Integer tval = progress + Globals.MIN_NM_THRESHOLD;
					if(_bLowNightmodeValues)
					{
						Float tfval = (float) tval / 10;
						_txtCurrentTheshold.setText(tfval.toString());
					}
					else
						_txtCurrentTheshold.setText(tval.toString());
					
					BrightnessController bc = BrightnessController.get();
					bc.setNightThreshold(tval);
					bc.updateRunningBrightness();
				}
			}
		});
		
		_btnTestNight.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					_bInTest = true;
					setAllowChangeThemeOnFly(false);
					BrightnessController.get().setForceNight(true);
					BrightnessController.get().updateRunningBrightness();
					return true;
				case MotionEvent.ACTION_UP:
					BrightnessController.get().setForceNight(false);
					BrightnessController.get().updateRunningBrightness();
					setAllowChangeThemeOnFly(true);
					_bInTest = false;
					return true;
				default:
					break;
				}
				return false;
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		AppSettings as = new AppSettings(this);
		
		_sbMin.setProgress(as.getBrightnessMinRange() - Globals.MIN_BRIGHTNESS_INT);
		_sbMax.setProgress(as.getBrightnessMaxRange() - Globals.MIN_BRIGHTNESS_INT);
		
		_sbNightBrightness.setProgress(as.getNMBrightness() - Globals.MIN_NM_BRIGHTNESS);
		_sbNightThreshold.setProgress(as.getNMThreshold() - Globals.MIN_NM_THRESHOLD);
		
		_bLowNightmodeValues = as.getLowNightmodeValues();
		
		Integer tval = as.getNMThreshold();
		if(_bLowNightmodeValues)
		{
			Float tfval = (float) tval / 10;
			_txtCurrentTheshold.setText(tfval.toString());
		}
		else
			_txtCurrentTheshold.setText(tval.toString());
		
		Float fval = BrightnessController.get().getRunningReading();
		if(fval != null)
			_txtRunningReading.setText(_df.format(fval));
		
		if(BrightnessController.get().isForceNight())
			_btnTestNight.setEnabled(false);
		
		BrightnessController.get().addBrightnessStatusObserver(_oBrightnessStatus);
		BrightnessController.get().addRunningReadingObserver(_oRunningLightSensorValue);
		
		updateControls();
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		BrightnessController.get().removeRunningReadingObserver(_oRunningLightSensorValue);
		BrightnessController.get().removeBrightnessStatusObserver(_oBrightnessStatus);
		
		AppSettings as = new AppSettings(this);
		
		as.setBrightnessMinRange(_sbMin.getProgress() + Globals.MIN_BRIGHTNESS_INT);
		as.setBrightnessMaxRange(_sbMax.getProgress() + Globals.MIN_BRIGHTNESS_INT);
		as.setNMBrightness(_sbNightBrightness.getProgress() + Globals.MIN_NM_BRIGHTNESS);
		as.setNMThreshold(_sbNightThreshold.getProgress() + Globals.MIN_NM_THRESHOLD);
		
	}
	
	private void updateControls()
	{
		BrightnessStatus bs = BrightnessController.get().getBrightnessStatus();
		
		_btnTestNight.setEnabled(_bInTest || bs == BrightnessStatus.Auto);
		_txtNotAutoWarn.setVisibility(bs != BrightnessStatus.Auto?View.VISIBLE:View.GONE);
	}
}
