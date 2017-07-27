package apps.babycaretimer.log;

/**
 * This class logs messages to the Android log file.
 * 
 * @author Camille Sévigny
 */
public class Log {
	
	//================================================================================
    // Properties
    //================================================================================
	
	private static final String _logTag = "BabyCareTimer";
	private static final boolean _debug = true;
	private static final boolean _showAndroidRateAppLink = true;
	private static final boolean _showAmazonRateAppLink = false;
	
	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 *  Get logTag property.
	 *  
	 *  @return String - Returns the tag of these log entries.
	 */
	public static String getLogTag(){
		return _logTag;
	}

	/**
	 *  Get debug property.
	 *  
	 *  @return boolean - Returns true if the log class is set to log entries.
	 */
	public static boolean getDebug(){
		return _debug;
	}

	/**
	 *  Get showAndroidRateAppLink property.
	 *  
	 *  @return boolean - Returns true if we want to show the Android Market link.
	 */
	public static boolean getShowAndroidRateAppLink(){
		return _showAndroidRateAppLink;
	}

	/**
	 *  Get showAmazonRateAppLink property.
	 *  
	 *  @return boolean - Returns true if we want to show the Amazon Appstore link.
	 */
	public static boolean getShowAmazonRateAppLink(){
		return _showAmazonRateAppLink;
	}
	
	/**
	 *  Add an entry to the Android LogCat log under the V (Verbose) type.
	 *  
	 *  @param msg - Entry to be made to the log file.
	 */
	public static void v(String msg) {
		if(_debug){
			android.util.Log.v(getLogTag(), msg);
		}
	}
	
	/**
	 *  Add an entry to the Android LogCat log under the D (Debug) type.
	 *  
	 *  @param msg - Entry to be made to the log file.
	 */
	public static void d(String msg) {
		if(_debug){
			android.util.Log.d(getLogTag(), msg);
		}
	}	
	
	/**
	 *  Add an entry to the Android LogCat log under the I (Info) type.
	 *  
	 *  @param msg - Entry to be made to the log file.
	 */
	public static void i(String msg) {
		if(_debug){
			android.util.Log.i(getLogTag(), msg);
		}
	}
	
	/**
	 *  Add an entry to the Android LogCat log under the W (Warning) type.
	 *  
	 *  @param msg - Entry to be made to the log file.
	 */
	public static void w(String msg) {
		if(_debug){
			android.util.Log.w(getLogTag(), msg);
		}
	}
	
	/**
	 *  Add an entry to the Android LogCat log under the E (Error) type.
	 *  
	 *  @param msg - Entry to be made to the log file.
	 */
	public static void e(String msg) {
		if(_debug){
			android.util.Log.e(getLogTag(), msg);
		}
	}
	
}
