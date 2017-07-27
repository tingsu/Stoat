package biz.gyrus.yaab;

public class Globals {

	public static final String TAG = "YAAB";
	public static final int NOTIFICATION_ID = 42;
	
	public static final int MIN_BRIGHTNESS_INT = 20;
	public static final int MAX_BRIGHTNESS_INT = 255;
	public static final float MIN_READING_DAY_F = 50f;
	public static final float DEFAULT_DIMAMOUNT_F = 0.45f;

	public static final int ADJUSTMENT_RANGE_INT = 100;
	
	public static final long MEASURING_FRAME = 5000;	// 5 seconds for now
	public static final long TIMER_PERIOD = 500;
	
	public static final long SMOOTH_TIMER_PERIOD = 12;
	public static final float SMOOTH_TIMER_MIN_STEP = 0.0001f;
	
	// following values used to configure sliders properly, not as executive values
	public static int MIN_NM_THRESHOLD = 1;
	public static int MAX_NM_THRESHOLD = 80;
	public static int MIN_NM_BRIGHTNESS = 0;
	public static int MAX_NM_BRIGHTNESS = 60;
	
	public static final long DEFAULT_NIGHTFALL_DELAY = 5000;		// 5 seconds

}
