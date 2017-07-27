/*
 * YAAB concept proof project, (C) Gyrus Solutions, 2011
 * http://www.gyrus.biz
 * 
 */

package biz.gyrus.yaab;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import biz.gyrus.yaab.BrightnessController.BrightnessStatus;
import biz.gyrus.yaab.BrightnessController.ServiceStatus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;

public class LightMonitorService extends Service {

	public static final float HIST_DELTA_THRESHOLD = 0.02f;
	
	private static class Reading
	{
		public Reading(float f, long t) { fVal = f; lTime = t; }
		
		public float fVal = 0f;
		public long lTime = 0L;
	}

	private boolean _bActive = false;
	private boolean _bAllowNightFall = false;
	private Handler _h = new Handler();
	private SensorManager _sensorManager = null;
	private Sensor _lightSensor = null;

	private float _currentRunningReading = 150f;
	private List<Reading> _readings = new LinkedList<Reading>();

	private ActivatorView _av = null;
	private WindowManager.LayoutParams _avLayoutParams = null;
	private float _fScreenBrightness = 0f;

	private static LightMonitorService _instance = null;
	
	private PendingIntent _piSelf = null;

	private BrightnessAnimatorRunnable _ba = null;
	
	public static LightMonitorService getInstance() {
		return _instance;
	}
	
	private class BrightnessAnimatorRunnable implements Runnable
	{
		private float _to = 0;
		private float _from = 0;
		private float _step = 0;
		private float _current = _from;
		private boolean _bSmoothTimerActive = false;
		private WindowManager _wm = null;
		private int _iStepIdx = 0;
		private long _iCalculatedSmoothTimerPeriod = Globals.SMOOTH_TIMER_PERIOD;
		
		public void start(float from, float to)
		{
			_wm = (WindowManager) getSystemService(WINDOW_SERVICE); 

			_from = from;
			_to = to;
			_step = (_to - _from)/(Globals.TIMER_PERIOD/Globals.SMOOTH_TIMER_PERIOD + 1);
			_current = from;
			_iStepIdx = 0;
			_iCalculatedSmoothTimerPeriod = Globals.SMOOTH_TIMER_PERIOD;
			
			if(Math.abs(_step) < Globals.SMOOTH_TIMER_MIN_STEP)
				_iCalculatedSmoothTimerPeriod = (int)(Globals.SMOOTH_TIMER_MIN_STEP * Globals.SMOOTH_TIMER_PERIOD / _step);
			
//			if(Log.isLoggable(Globals.TAG, Log.DEBUG))
//				Log.d(Globals.TAG, String.format("animateBrightness, _iCalculatedSmoothTimerPeriod = %d", _iCalculatedSmoothTimerPeriod));
			
			if(Math.abs(_step) < Globals.SMOOTH_TIMER_MIN_STEP)
				smoothTimerApplyVal(_to);
			else
			{
				_bSmoothTimerActive = true;
			
//				if(Log.isLoggable(Globals.TAG, Log.DEBUG))
//					Log.d(Globals.TAG, String.format("animateBrightness, _from = %f, _to = %f, _step = %f", _from, _to, _step));
		
				_h.postDelayed(this, _iCalculatedSmoothTimerPeriod);
			}
		}

		private void smoothTimerApplyVal(float val)
		{
			if(_avLayoutParams != null)
			{
				_avLayoutParams.flags &= ~(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
				_avLayoutParams.screenBrightness = val;
				if(_wm != null)
					_wm.updateViewLayout(_av, _avLayoutParams);
			}
			
//			if(Log.isLoggable(Globals.TAG, Log.DEBUG))
//				Log.d(Globals.TAG, String.format("smoothTimer tick, newVal = %f", val));
			
		}
		
		@Override
		public void run() {
			
			if(_bSmoothTimerActive)
			{
				_current += _step;
				
				float newVal = _current;
				if(Math.abs(_to - newVal) < Math.abs(_step) || _iStepIdx > Globals.TIMER_PERIOD / _iCalculatedSmoothTimerPeriod + 1)
				{
					newVal = _to;
					_bSmoothTimerActive = false;
				}
				
				smoothTimerApplyVal(newVal);
				
				_iStepIdx++;
				
				if(_bSmoothTimerActive)
					_h.postDelayed(this, _iCalculatedSmoothTimerPeriod);
				else
					_wm = null;
			}
		}
		
		public boolean isActive() { return _bSmoothTimerActive; }
		
		public void cancel()
		{
			_bSmoothTimerActive = false;
			
			smoothTimerApplyVal(_to);
			
			_wm = null; 
		}
		
	}
	
	private Observer _oBrightnessStatus = new Observer() {
		
		@Override
		public void update(Observable observable, Object data) {
			
			showNotificationIcon(true);
		}
	};

	private Observer _oOrientation = new Observer() {
		
		@Override
		public void update(Observable observable, Object data) {
			
			BrightnessController bc = BrightnessController.get();
			
			boolean bAutoNight = bc.getAutoNight() && _currentRunningReading < bc.getNightThreshold();
			boolean bUseDim = bc.isForceNight() || bAutoNight;
			
			if(bUseDim)
			{
				WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
				
				_avLayoutParams.flags &= ~(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
				wm.updateViewLayout(_av, _avLayoutParams);

				_avLayoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
				wm.updateViewLayout(_av, _avLayoutParams);
			}
		}
	};

	private BroadcastReceiver _brScrOFF = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.i(Globals.TAG, "ScreenOFF broadcast received, unregistering listeners.");

				if (_sensorManager != null)
					_sensorManager.unregisterListener(_listener);

				cancelTimer();

				AppSettings as = new AppSettings(LightMonitorService.this);
				if(!as.getPersistAlwaysNotification())
					showNotificationIcon(false);
				startAlertKeepalive(false);
			}
		}
	};

	private BroadcastReceiver _brScrON = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				Log.i(Globals.TAG, "ScreenON broadcast received, registering listeners.");

				_sensorManager.registerListener(_listener, _lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

				AppSettings as = new AppSettings(LightMonitorService.this);
				showNotificationIcon(as.getPersistNotification());
				startAlertKeepalive(as.getAlertKeepalive());
			}

		}
	};

	private Runnable _nightFallDelayHandler = new Runnable() {
		
		@Override
		public void run() {
			_bAllowNightFall = true;
			_h.removeCallbacks(_nightFallDelayHandler);
			if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "NightFallHandler: delay time passed, night allowed");
			applyRunningReading();
		}
	};
	
	private Runnable _timerHandler = new Runnable() {

		@Override
		public void run() {
			long now = System.currentTimeMillis();
			if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, String.format("Timer hit, millis: %d.", now));

			synchronized (_readings) {
				// cleaning obsolete values which are out of measuring frame
				while(_readings.size() > 0 && _readings.get(0).lTime < now - Globals.MEASURING_FRAME)
					_readings.remove(0);
				
				if (_readings.size() > 0) {
					
					float sum = 0;
					long measurePoint = now;
					
					for (int i = _readings.size() - 1; i >= 0; i--) 
					{
						Reading r = _readings.get(i);
						
						sum += r.fVal * (measurePoint - r.lTime);
						measurePoint = r.lTime;
					}
					
					sum += _currentRunningReading * (measurePoint - (now - Globals.MEASURING_FRAME));
					
					float currentReading = sum / Globals.MEASURING_FRAME;
	
					if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "AVG reading: " + currentReading);
	
					float currentReadingBrightness = BrightnessController.get().getBrightnessFromReading(currentReading);
					float currentRunningBrightness = BrightnessController.get().getBrightnessFromReading(_currentRunningReading);
	
					if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "ReadingBrightness: " + currentReadingBrightness);
					if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "RunningBrightness: " + currentRunningBrightness);
	
					if (Math.abs(currentReadingBrightness - currentRunningBrightness) > HIST_DELTA_THRESHOLD) {
						if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, String.format("Threshold defeated! newReading = %f", currentReading));
						_currentRunningReading = currentReading;
						BrightnessController.get().setRunningReading(_currentRunningReading);
						applyRunningReading();
					}
				} else {
					_bActive = false;
				}
			}
			
			if(_bActive)
				_h.postDelayed(_timerHandler, Globals.TIMER_PERIOD);
		}
	};

	private SensorEventListener _listener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			//Log.d(Globals.TAG, "Accuracy changed called!");
		}

		@Override
		public void onSensorChanged(SensorEvent event) {

			if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
				float currentReading = event.values[0] + 0.1f;		// +0.1 is a fix for sensors which can general pure zero as measuring value. avoid Math.log(0) 
				//Log.d(Globals.TAG, String.format("Float brightness: %f", currentReading));
				
				synchronized (_readings) {
					_readings.add(new Reading(currentReading, System.currentTimeMillis()));
				}

				kickTimer();
			}

		}
	};
	
	public synchronized void applyRunningReading() {
		setBrightness(BrightnessController.get().getBrightnessFromReading(_currentRunningReading));
	}

	@Override
	public void onCreate() {
		_instance = this;
		Log.i(Globals.TAG, "Service onCreate() called");

		super.onCreate();
		
		BrightnessController bc = BrightnessController.get();

		if (bc.isLightSensorPresent(this)) {
			AppSettings as = new AppSettings(this);

			_sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			_lightSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			if(_lightSensor == null)
			{
				Log.w(Globals.TAG, "No default light sensor found!");
				List<Sensor> sens = _sensorManager.getSensorList(Sensor.TYPE_LIGHT);
				if(sens.size() > 0)
				{
					Log.i(Globals.TAG, "Non-default light sensors found, trying just the first");
					_lightSensor = sens.get(0);
				}
			}

			_piSelf = PendingIntent.getService(this, 0, new Intent(this, this.getClass()), PendingIntent.FLAG_UPDATE_CURRENT);

			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			if (pm.isScreenOn()) {
				_sensorManager.registerListener(_listener, _lightSensor, SensorManager.SENSOR_DELAY_FASTEST);

				showNotificationIcon(as.getPersistNotification());

				startAlertKeepalive(as.getAlertKeepalive());
				Log.i(Globals.TAG, "Listener registered");
			} else
				Log.i(Globals.TAG, "Screen is off, skip listener registration.");

			bc.setManualAdjustment(as.getAdjshift());
			bc.setBrightnessMin(as.getBrightnessMinRange());
			bc.setBrightnessMax(as.getBrightnessMaxRange());
			bc.setAutoNight(as.getAllowAutoNight());
			bc.setRunningDimAmount(bc.getDimAmount(as.getNMBrightness()));
			bc.setNightThreshold(as.getNMThreshold());
			bc.setSmoothApplyBrightness(as.getSmoothApplyBrightness());
			bc.setLowNightmodeValues(as.getLowNightmodeValues());
			bc.setNightFallDelay(as.getNightfallDelay());

			_av = new ActivatorView(this);
			_avLayoutParams = new WindowManager.LayoutParams(0, 0, 0, 0,
					WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
							| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
					PixelFormat.OPAQUE);
			_avLayoutParams.screenBrightness = _fScreenBrightness = 20f;

			WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
			wm.addView(_av, _avLayoutParams);

			registerReceiver(_brScrOFF, new IntentFilter(Intent.ACTION_SCREEN_OFF));
			registerReceiver(_brScrON, new IntentFilter(Intent.ACTION_SCREEN_ON));
			
			_av.addOrientationObserver(_oOrientation);

			bc.updateServiceStatus(ServiceStatus.Running);
			if(as.getManualNight())
				bc.setForceNight(true);
			else
				bc.setBrightnessStatus(BrightnessStatus.Auto);

		}

		Log.i(Globals.TAG, "Service onCreate() finished");
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(Globals.TAG, "Service onStartCommand() called");

		if (BrightnessController.get().isLightSensorPresent(this)) {
			int brightnessMode = 0;

			try {
				brightnessMode = Settings.System.getInt(getContentResolver(),
						Settings.System.SCREEN_BRIGHTNESS_MODE);
			} catch (SettingNotFoundException e) {
				e.printStackTrace();
			}

			if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
				Settings.System.putInt(getContentResolver(),
						Settings.System.SCREEN_BRIGHTNESS_MODE,
						Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			}
		} else {
			Log.w(Globals.TAG,
					"Started on a device without light sensor. How could this happen at all?!!");
			stopSelf();
		}

		return super.onStartCommand(intent, flags, startId);
	};

	@Override
	public void onDestroy() {

		Log.i(Globals.TAG, "Service onDestroy() called");
		
		_av.delOrientationObserver(_oOrientation);
		
		BrightnessController.get().updateServiceStatus(ServiceStatus.Stopped);
		BrightnessController.get().setBrightnessStatus(BrightnessStatus.Off);

		unregisterReceiver(_brScrOFF);
		unregisterReceiver(_brScrON);

		if (_sensorManager != null && _listener != null)
			_sensorManager.unregisterListener(_listener);

		cancelTimer();
		finishBrightnessAnimation();
		
		synchronized (_h) {
			if (_av != null) {
				WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
				wm.removeView(_av);
				_av = null;
				_avLayoutParams = null;
			}
		}

		showNotificationIcon(false);

		super.onDestroy();
		_instance = null;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	// must be called when incoming sensor change arrives
	private void kickTimer() {
		//Log.i(Globals.TAG, "kickTimer in action");
		if (_bActive)
			return;

		_bActive = true;

		_h.postDelayed(_timerHandler, Globals.TIMER_PERIOD);
		Log.i(Globals.TAG, "Timer hit initiated!");
	}

	private void cancelTimer() {
		Log.i(Globals.TAG, "cancelTimer called");
		_bActive = false;
		_h.removeCallbacks(_timerHandler);
	}

	private void setBrightness(float brightness) {
		if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, String.format("setBrightness called, brightness = %f", brightness));
		
		finishBrightnessAnimation();
		
		BrightnessController bc = BrightnessController.get();
		
		boolean bNightReading = _currentRunningReading < bc.getNightThreshold();
		if(bc.getNightFallDelay())
		{
			if(bNightReading)
			{
				if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "Caught night reading, posting nightFall handler");
				_h.postDelayed(_nightFallDelayHandler, Globals.DEFAULT_NIGHTFALL_DELAY);
			}
			else
			{
				if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "Caught day reading, removing nightFall handler");
				_h.removeCallbacks(_nightFallDelayHandler);
				_bAllowNightFall = false;
			}
		}
		else
			_bAllowNightFall = true;
		
		boolean bAutoNight = bc.getAutoNight() && bNightReading && _bAllowNightFall;
		boolean bUseDim = bc.isForceNight() || bAutoNight;
		
		if(!bc.isForceNight())
			if((bc.getBrightnessStatus() == BrightnessStatus.AutoNight) ^ bAutoNight)
				bc.setBrightnessStatus(bAutoNight?BrightnessStatus.AutoNight:BrightnessStatus.Auto);
		
		int iBrightness = (int) (brightness * Globals.MAX_BRIGHTNESS_INT);
		if (iBrightness < bc.getBrightnessMin())
			iBrightness = bc.getBrightnessMin();
		if (iBrightness > bc.getBrightnessMax())
			iBrightness = bc.getBrightnessMax();

		Settings.System.putInt(LightMonitorService.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, iBrightness);
		if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, String.format("putInt with %d called.", iBrightness));
		
		synchronized (_h) {
			if(_av != null && _avLayoutParams != null)		// these will be null when the service is stopping
			{
				WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
				
				if(bUseDim)
				{
					_avLayoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
					_avLayoutParams.dimAmount = bc.getRunningDimAmount();
					_avLayoutParams.screenBrightness = (float)Globals.MIN_BRIGHTNESS_INT/Globals.MAX_BRIGHTNESS_INT;
					wm.updateViewLayout(_av, _avLayoutParams);
				}
				else
				{
					float fNewScreenBrightness = bc.cutLayoutParamsBrightness(brightness);
					if(bc.getSmoothApplyBrightness())
					{
						animateBrightness(
								_fScreenBrightness, 
								fNewScreenBrightness);
						
					}
					else
					{
						_avLayoutParams.flags &= ~(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
						_avLayoutParams.screenBrightness = fNewScreenBrightness;
						if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, String.format("setBrightness applying, brightness = %f", brightness));
						wm.updateViewLayout(_av, _avLayoutParams);
					}
					_fScreenBrightness = fNewScreenBrightness;
				}
				
			}
		}
		
		bc.setRunningBrightness(iBrightness);
		
		if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "setBrightness done.");
	}

	public void showNotificationIcon(boolean bShow) {
		if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, String.format("showNotificationIcon entering, bShow = %b", bShow));
		
		if (bShow) {
			Intent ni = new Intent(this, MainActivity.class);
			PendingIntent pi = PendingIntent.getActivity(this, 0, ni, PendingIntent.FLAG_CANCEL_CURRENT);

			int iStatusRedId = -1;
			switch(BrightnessController.get().getBrightnessStatus())
			{
			case Auto:
				iStatusRedId = R.string.brightness_status_auto;
				break;
			case AutoNight:
				iStatusRedId = R.string.brightness_status_autonight;
				break;
			case ForceNight:
				iStatusRedId = R.string.brightness_status_manualnight;
				break;
			default:
				iStatusRedId = R.string.brightness_status_off;
				break;
			}

			NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
			nb.setContentIntent(pi)
					.setAutoCancel(false)
					.setSmallIcon((Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)?R.drawable.ic_notif_white:R.drawable.ic_launcher)
					.setWhen(System.currentTimeMillis())
					.setContentTitle(getResources().getString(R.string.app_name))
					.setContentText(getResources().getString(iStatusRedId));

			startForeground(Globals.NOTIFICATION_ID, nb.getNotification());
			BrightnessController.get().addBrightnessStatusObserver(_oBrightnessStatus);
		} else
		{
			BrightnessController.get().removeBrightnessStatusObserver(_oBrightnessStatus);
			stopForeground(true);
		}
		
		if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "showNotificationIcon leave");
	}
	
	public void startAlertKeepalive(boolean bStart)
	{
		if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, String.format("startAlertKeepalive entering, bStart = %b",  bStart));
		
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
		if(bStart)
		{
			am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, 5 * 1000 /* each five seconds */, _piSelf);
		}
		else
		{
			am.cancel(_piSelf);
		}
		
		if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "startAlertKeepalive leave");
	}
	
	private synchronized void animateBrightness(float from, float to)
	{
		if(_ba != null && _ba.isActive())
			finishBrightnessAnimation();
		
		_ba = new BrightnessAnimatorRunnable();
		_ba.start(from, to);
	}
	
	private synchronized void finishBrightnessAnimation()
	{
		if(_ba != null)
			_ba.cancel();
	}
}
