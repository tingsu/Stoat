package com.sunyata.kindmind.Setup;

import java.util.Calendar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

/*
 * Overview: TimePickerFragmentC displays pop-up window where hour and minute can be chosen. Hours and minutes
 *  are sent back to DetailsFragmentC using an interface for callbacks specified here. 
 * Usage in app: DetailsFragmentC
 * Notes: We don't have a corresponding activity for this fragment, instead this fragment is
 *  displayed using DialogFragment.show()
 * Improvements: In the BNRG book there is a suggestion of using DatePicker dialog which would simplify,
 *  but this class was at the time of writing that book not functioning.
 */

/*
 * Overview: 
 * 
 * Details: 
 * 
 * Usage: 
 * 
 * Uses app internal: 
 * 
 * Uses Android lib: 
 * 
 * In:
 * 
 * Out:
 * 
 * Does: 
 * 
 * Shows user: 
 * 
 * Notes: 
 * 
 * Improvements: 
 * 
 * Documentation: 
 * 
 */


public class TimePickerFragmentC extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

	//-------------------Fields and constructor
	
	static OnTimeSetListenerI mOnTimeSetListener;
	
	static TimePickerFragmentC newInstance(OnTimeSetListenerI inOnTimeSetListener){
		mOnTimeSetListener = inOnTimeSetListener;
		return new TimePickerFragmentC();
	}
	
	
	//-------------------Overridden DialogFragment methods
	
	@Override
	public Dialog onCreateDialog(Bundle inSavedInstanceState){
		Calendar tmpCalendar = Calendar.getInstance();
		int tmpHour = tmpCalendar.get(Calendar.HOUR_OF_DAY);
		int tmpMinute = tmpCalendar.get(Calendar.MINUTE);
		return new TimePickerDialog(
				getActivity(), this, tmpHour, tmpMinute, DateFormat.is24HourFormat(getActivity()));
	}
	
	@Override
	public void onTimeSet(TimePicker inView, int inHourOfDay, int inMinute) {
		mOnTimeSetListener.fireOnTimeSetEvent(inHourOfDay, inMinute);
	}
	
	
	//-------------------Interface for callback
	
	interface OnTimeSetListenerI{
		void fireOnTimeSetEvent(int inHourOfDay, int inMinute);
	}
}
