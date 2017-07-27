package apps.babycaretimer.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import apps.babycaretimer.R;
import apps.babycaretimer.common.Constants;
import apps.babycaretimer.log.Log;

/**
 * A custom ListPreference class that handles custom LED pattern selection/creation.
 * 
 * @author Camille Sévigny
 */
public class LEDPatternListPreference extends ListPreference {
	
	//================================================================================
    // Properties
    //================================================================================

    private boolean _debug = false;
    private Context _context = null;
    private SharedPreferences _preferences = null;

	//================================================================================
	// Constructors
	//================================================================================
    
	/**
	 * ListPreference constructor.
	 * 
	 * @param context - The application context.
	 */
	public LEDPatternListPreference(Context context) {
		super(context);
	    _debug = Log.getDebug();
	    if (_debug) Log.v("LEDPatternListPreference.LEDPatternListPreference()");
		_context = context;
		_preferences = PreferenceManager.getDefaultSharedPreferences(_context);
	}

	/**
	 * ListPreference constructor.
	 * 
	 * @param context - The application context.
	 * @param attrs
	 */
	public LEDPatternListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	    _debug = Log.getDebug();
	    if (_debug) Log.v("LEDPatternListPreference.LEDPatternListPreference()");
		_context = context;
		_preferences = PreferenceManager.getDefaultSharedPreferences(_context);
	}

	//================================================================================
	// Protected Methods
	//================================================================================
	
	/**
	 * Override the onDialogClosed method.
	 * 
	 * @param result - A boolean result that indicates how the dialog window was closed.
	 */
	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (_debug) Log.v("LEDPatternListPreference.onDialogClosed()");
		if (result) {
			if (_preferences.getString(Constants.ALARM_NOTIFICATION_LED_PATTERN_KEY, Constants.ALARM_NOTIFICATION_LED_PATTERN_DEFAULT).equals(Constants.ALARM_NOTIFICATION_LED_PATTERN_CUSTOM_VALUE_KEY)) {
				  showDialog();
			}
		}
	}
	
	//================================================================================
	// Private Methods
	//================================================================================
	
	/**
	 * Display the dialog window that allows the user to enter the vibrate pattern they wish to have.
	 */
	private void showDialog() {
		if (_debug) Log.v("LEDPatternListPreference.showDialog()");
	    LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.ledpatterndialog, null);
	    String ledCustomPattern = _preferences.getString(Constants.ALARM_NOTIFICATION_LED_PATTERN_CUSTOM_KEY, Constants.ALARM_NOTIFICATION_LED_PATTERN_DEFAULT);
	    String[] ledCustomPatternArray = ledCustomPattern.split(",");
		final EditText customOnLEDPatternEditText = (EditText) view.findViewById(R.id.ledPatternOnEditText);
		final EditText customOffLEDPatternEditText = (EditText) view.findViewById(R.id.ledPatternOffEditText);
		customOnLEDPatternEditText.setText(ledCustomPatternArray[0]);
		customOffLEDPatternEditText.setText(ledCustomPatternArray[1]);
		AlertDialog.Builder ledPatternAlertBuilder = new AlertDialog.Builder(_context);
		ledPatternAlertBuilder.setIcon(R.drawable.ic_dialog_info);
		ledPatternAlertBuilder.setTitle(R.string.preference_led_pattern_title);
		ledPatternAlertBuilder.setView(view);
		ledPatternAlertBuilder.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String customLEDPattern = customOnLEDPatternEditText.getText() + "," + customOffLEDPatternEditText.getText();
				if (validateLEDPattern(customLEDPattern)) {
					SharedPreferences.Editor editor = _preferences.edit();
		        	editor.putString(Constants.ALARM_NOTIFICATION_LED_PATTERN_CUSTOM_KEY, customLEDPattern);
		            editor.commit();
					Toast.makeText(_context, _context.getString(R.string.preference_led_pattern_set), Toast.LENGTH_LONG).show();
		        } else {
		        	Toast.makeText(_context, _context.getString(R.string.preference_led_pattern_error), Toast.LENGTH_LONG).show();
		        }
			}
		});
		ledPatternAlertBuilder.show();
	}
	
	/**
	 * Parse an led pattern and verify if it's valid or not.
	 * 
	 * @param ledPattern - The led pattern to verify.
	 * 
	 * @return boolean - Returns True if the led pattern is valid.
	 */
	private boolean validateLEDPattern(String ledPattern){
		if (_debug) Log.v("LEDPatternListPreference.parseLEDPattern()");
		String[] ledPatternArray = ledPattern.split(",");
		int arraySize = ledPatternArray.length;
	    for (int i = 0; i < arraySize; i++) {
	    	long ledLength = 0;
	    	try {
	    		ledLength = Long.parseLong(ledPatternArray[i].trim());
	    	} catch (Exception ex) {
	    		return false;
	    	}
	    	if(ledLength < 0){
	    		return false;
	    	}
	    }
		return true;
	}
	
}