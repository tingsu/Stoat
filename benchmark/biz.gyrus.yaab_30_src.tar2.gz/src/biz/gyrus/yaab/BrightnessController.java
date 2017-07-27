package biz.gyrus.yaab;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

public class BrightnessController {
	private BrightnessController(){}
	private static BrightnessController _instance = new BrightnessController();
	
	public static BrightnessController get() { return _instance; }
	
	public enum ServiceStatus { Error, Stopped, Running }
	public enum BrightnessStatus { Off, Auto, ForceNight, AutoNight }
	
	private static class BrightnessStatusObservable extends Observable
	{
		private BrightnessStatus _bsCurrent = BrightnessStatus.Off;
		
		public BrightnessStatus getStatus() { return _bsCurrent; }
		
		public void setStatus(BrightnessStatus status)
		{
			if(_bsCurrent != status)
				setChanged();
			
			_bsCurrent = status;
			
			notifyObservers();
		}
	}
	
	private static class ServiceStatusObservable extends Observable
	{
		private ServiceStatus _ssCurrent = ServiceStatus.Stopped;

		public ServiceStatus getStatus() { return _ssCurrent; }
		
		public void setStatus(ServiceStatus ss)
		{
			if(ss != _ssCurrent)
				setChanged();
			
			_ssCurrent = ss;
			notifyObservers();
		}
	}
	
	private static class RunningBrightnessObservable extends Observable
	{
		private int _iRunningBrightness = 0;
		
		public int getRunningBrightness() { return _iRunningBrightness; }
		
		public void setRunningBrightness(int brightness)
		{
			if(_iRunningBrightness != brightness)
				setChanged();
			
			_iRunningBrightness = brightness;
			notifyObservers();
		}
	}
	
	private static class RunningLSReadingObservable extends Observable
	{
		private Float _runningReading = null;
		
		public Float getRunningLSReading() { return _runningReading; }
		public void setRunningLSReading(float reading)
		{
			if(_runningReading == null || _runningReading != reading)	// floats are always !=, aren't they? what is this for?
				setChanged();
			
			_runningReading = reading;
			notifyObservers();
		}
	}
	
	private Boolean _bIsSensorPresent = null;
	private int _iManualAdjustmentValue = 0;
	private ServiceStatusObservable _oServStatus = new ServiceStatusObservable();
	private BrightnessStatusObservable _oBrightnessStatus = new BrightnessStatusObservable();
	private RunningBrightnessObservable _oRunningBrightness = new RunningBrightnessObservable();
	private RunningLSReadingObservable _oRunningReading = new RunningLSReadingObservable();
	private int _iMinRange = Globals.MIN_BRIGHTNESS_INT;
	private int _iMaxRange = Globals.MAX_BRIGHTNESS_INT;
	private boolean _bAutoNight = false;
	private float _fDimAmount = Globals.DEFAULT_DIMAMOUNT_F;
	private float _fNightThreshold = Globals.MIN_READING_DAY_F;
	private Boolean _bSmoothApplyBrightness = null;
	private boolean _bBlockEffects = false;
	private boolean _bLowNightmodeValues = false;
	private boolean _bNightFallDelay = false;
	
	public boolean isLightSensorPresent(Context ctx)
	{
		if(_bIsSensorPresent == null)
		{
			SensorManager sm = (SensorManager)ctx.getSystemService(Context.SENSOR_SERVICE);
			Sensor ls = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
			List<Sensor> list = null;
			
			if(ls == null)
			{
				list = sm.getSensorList(Sensor.TYPE_LIGHT);
			}
			
			_bIsSensorPresent = (ls != null || list.size() > 0);
		}
		return _bIsSensorPresent;
	}
	
	public void updateRunningBrightness()
	{
		LightMonitorService lms = LightMonitorService.getInstance();
		if(lms != null)
		{
			lms.applyRunningReading();
		}
	}
	
	public int getManualAdjustment() { return _iManualAdjustmentValue; }
	public void setManualAdjustment(int val)
	{
		_iManualAdjustmentValue = val;
	}
	public float getBrightnessFromReading(float reading)
	{
		return (float)(14*Math.log(reading) - 38 + _iManualAdjustmentValue)/100f;
	}
	
	
	public void addServiceStatusObserver(Observer o)
	{
		_oServStatus.addObserver(o);
	}
	public void removeServiceStatusObserver(Observer o)
	{
		_oServStatus.deleteObserver(o);
	}
	public void updateServiceStatus(ServiceStatus ss)
	{
		_oServStatus.setStatus(ss);
	}
	public ServiceStatus getServiceStatus() { return _oServStatus.getStatus(); }
	
	
	public void addRunningBrightnessObserver(Observer o)
	{
		_oRunningBrightness.addObserver(o);
	}
	public void removeRunningBrightnessObserver(Observer o)
	{
		_oRunningBrightness.deleteObserver(o);
	}
	public void setRunningBrightness(int brightness)
	{
		_oRunningBrightness.setRunningBrightness(brightness);
	}
	public int getRunningBrightness() { return _oRunningBrightness.getRunningBrightness(); }
	
	public int getBrightnessMin() { return _iMinRange; }
	public int getBrightnessMax() { return _iMaxRange; }
	public void setBrightnessMin(int min) { _iMinRange = min; }
	public void setBrightnessMax(int max) { _iMaxRange = max; }
	
	public float getRunningDimAmount() { return _fDimAmount; }
	public void setRunningDimAmount(float dimAmount) { _fDimAmount = dimAmount; }
	public boolean isForceNight() { return _oBrightnessStatus.getStatus() == BrightnessStatus.ForceNight; }
	public void setForceNight(boolean forceNight) { _oBrightnessStatus.setStatus(forceNight ? BrightnessStatus.ForceNight : BrightnessStatus.Auto); }
	
	public boolean getAutoNight() { return _bAutoNight; }
	public void setAutoNight(boolean bAutoNight) { _bAutoNight = bAutoNight; }
	
	public float getDimAmount(int sliderSetting)
	{
		return -0.01f * sliderSetting + 0.8f;
	}
	public int getSliderBrightness(float dimAmount)
	{
		return - (int)((dimAmount - 0.8f) * 100);
	}
	
	public float getNightThreshold() 
	{
		if(_bLowNightmodeValues)
			return _fNightThreshold / 10;
		return _fNightThreshold; 
	}
	public void setNightThreshold(float val) { _fNightThreshold = val; }
	
	public void addRunningReadingObserver(Observer o) { _oRunningReading.addObserver(o); }
	public void removeRunningReadingObserver(Observer o) { _oRunningReading.deleteObserver(o); }
	public Float getRunningReading() { return _oRunningReading.getRunningLSReading(); }
	public void setRunningReading(float val) { _oRunningReading.setRunningLSReading(val); }
	
	public void addBrightnessStatusObserver(Observer o) { _oBrightnessStatus.addObserver(o); }
	public void removeBrightnessStatusObserver(Observer o) { _oBrightnessStatus.deleteObserver(o); }
	public BrightnessStatus getBrightnessStatus() { return _oBrightnessStatus.getStatus(); }
	public void setBrightnessStatus(BrightnessStatus status) { _oBrightnessStatus.setStatus(status); }
	
	public boolean getSmoothApplyBrightness() { return _bSmoothApplyBrightness && !_bBlockEffects; }
	public void setSmoothApplyBrightness(boolean bSmooth) { _bSmoothApplyBrightness = bSmooth; }
	
	public boolean getLowNightmodeValues() { return _bLowNightmodeValues; }
	public void setLowNightmodeValues(boolean bLow) { _bLowNightmodeValues = bLow; }
	
	public boolean getNightFallDelay() { return _bNightFallDelay; }
	public void setNightFallDelay(boolean bNightFallDelay) { _bNightFallDelay = bNightFallDelay; }
	
	public float cutLayoutParamsBrightness(float fromReading)
	{
		if(Log.isLoggable(Globals.TAG, Log.DEBUG))
			Log.d(Globals.TAG, String.format("cutLayoutParamsBrightness. input = %f", fromReading));
		
		float brightness = fromReading;
		
		float fMinBrightness = ((float)_iMinRange)/Globals.MAX_BRIGHTNESS_INT;
		float fMaxBrightness = ((float)_iMaxRange)/Globals.MAX_BRIGHTNESS_INT;
		
		if (brightness > fMaxBrightness)
			brightness = fMaxBrightness;
		
		if (brightness < fMinBrightness)
			brightness = fMinBrightness;

		if(Log.isLoggable(Globals.TAG, Log.DEBUG))
			Log.d(Globals.TAG, String.format("cutLayoutParamsBrightness. output = %f", brightness));
		
		return brightness;
	}
	
	public void blockEffects(boolean block)	{ _bBlockEffects = block; }
}
