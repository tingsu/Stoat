package com.sunyata.kindmind.util;

import java.io.File;
import java.math.BigDecimal;

import com.sunyata.kindmind.R;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.sunyata.kindmind.WidgetAndNotifications.WidgetProviderC;

public class OtherU {

	public static final String LIST_TYPE = "LIST_TYPE";
	public static final int MAX_NR_OF_PATTERN_ROWS = 2000;



	
	public static int longToIntCutOff(long inLong) {
		int retIntVal = (int) (inLong & 0x0000FFFF);
		return retIntVal;
	}
	

	
	/**
	 * \brief sendAsEmail sends an email with title, text and optionally an attachment
	 * 
	 * Used in: Helper method for onOptionsItemSelected above
	 * 
	 * Uses app internal: Utils.copyFile
	 * Notes: File must be stored on the external storage to be accessible by email applications (not enough to
	 *  use the internal cache dir for example)
	 */
	public static void sendAsEmail(Context inContext, String inTitle, String inTextContent, File inFileWithPath){
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, inTitle);
		i.putExtra(Intent.EXTRA_TEXT, inTextContent);
		File tmpExtCacheDir = inContext.getExternalCacheDir();
		if(inFileWithPath != null && tmpExtCacheDir != null){
			String tmpFileName = inFileWithPath.toString().substring(inFileWithPath.toString().lastIndexOf("/") + 1);
			FileU.copyFile(inFileWithPath, new File(tmpExtCacheDir + "/" + tmpFileName));
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(tmpExtCacheDir.toString() + "/" + tmpFileName)));
		}
		inContext.startActivity(i);
	}
	

	
	public static void updateWidgets(Context inContext) {
		AppWidgetManager tmpAppWidgetManager = AppWidgetManager.getInstance(inContext);
		ComponentName tmpComponentName = new ComponentName(inContext, WidgetProviderC.class);
		int[] tmpWidgetIds = tmpAppWidgetManager.getAppWidgetIds(tmpComponentName);
		tmpAppWidgetManager.notifyAppWidgetViewDataChanged(tmpWidgetIds, R.id.widget_listview);
	}
	
	
	public static String formatNumber(double inValue) {
		BigDecimal tmpBigDecimal = new BigDecimal(inValue);
		tmpBigDecimal = tmpBigDecimal.setScale(2, BigDecimal.ROUND_UP);
		return "" + tmpBigDecimal;
	}

	public static void waitForConditionHelper(int inStepTime, int inNumberOfSteps, int inCurrentStepNumber) {
		try {
			Thread.sleep(inStepTime);
		} catch (InterruptedException e) {}
		if(inCurrentStepNumber > inNumberOfSteps){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName()
					+ "Waited a long time for condition");
		}
	}


}