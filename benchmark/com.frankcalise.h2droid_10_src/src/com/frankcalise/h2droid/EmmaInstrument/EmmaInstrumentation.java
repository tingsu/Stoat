package com.frankcalise.h2droid.EmmaInstrument;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

public class EmmaInstrumentation extends Instrumentation implements
		FinishListener {
	public static String TAG = "EmmaInstrumentation:";
	private static final String DEFAULT_COVERAGE_FILE_PATH = "/mnt/sdcard/coverage.ec";

	private final Bundle mResults = new Bundle();

	private Intent mIntent;
	private static final boolean LOGD = true;

	private boolean mCoverage = true;

	private String mCoverageFilePath;

	
	/**
	 * Constructor
	 */
	public EmmaInstrumentation() {

	}

	@Override
	public void onCreate(Bundle arguments) {
		Log.d(TAG, "onCreate(" + arguments + ")");
		super.onCreate(arguments);

		if (arguments != null) {
			mCoverage = getBooleanArgument(arguments, "coverage");
			mCoverageFilePath = arguments.getString("coverageFile");
		}

		mIntent = new Intent(getTargetContext(), InstrumentedActivity.class);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		
		start();
	}

	@Override
	public void onStart() {
		if (LOGD)
			Log.d(TAG, "onStart()");
		super.onStart();

		Looper.prepare();
		InstrumentedActivity activity = (InstrumentedActivity) startActivitySync(mIntent);
		activity.setFinishListener(this);
	}

	private boolean getBooleanArgument(Bundle arguments, String tag) {
		String tagString = arguments.getString(tag);
		return tagString != null && Boolean.parseBoolean(tagString);
	}

	
	private void generateCoverageReport() {
		if (LOGD)
			Log.d(TAG, "generateCoverageReport()");

		java.io.File coverageFile = new java.io.File(getCoverageFilePath());

		// We may use this if we want to avoid refecltion and we include
		// emma.jar
		// RT.dumpCoverageData(coverageFile, false, false);

		// Use reflection to call emma dump coverage method, to avoid
		// always statically compiling against emma jar
		try {
			Class<?> emmaRTClass = Class.forName("com.vladium.emma.rt.RT");
			Method dumpCoverageMethod = emmaRTClass.getMethod(
					"dumpCoverageData", coverageFile.getClass(), boolean.class,
					boolean.class);
			dumpCoverageMethod.invoke(null, coverageFile, true, false);
		} catch (ClassNotFoundException e) {
			reportEmmaError("Emma.jar not in the class path?", e);
		} catch (SecurityException e) {
			reportEmmaError(e);
		} catch (NoSuchMethodException e) {
			reportEmmaError(e);
		} catch (IllegalArgumentException e) {
			reportEmmaError(e);
		} catch (IllegalAccessException e) {
			reportEmmaError(e);
		} catch (InvocationTargetException e) {
			reportEmmaError(e);
		}
	}

	private String getCoverageFilePath() {
		if (mCoverageFilePath == null) {
			return DEFAULT_COVERAGE_FILE_PATH;
		} else {
			return mCoverageFilePath;
		}
	}
	
	private boolean setCoverageFilePath(String filePath){
		if(filePath != null && filePath.length() > 0) {
			mCoverageFilePath = filePath;
			return true;
		}
		return false;
	}

	private void reportEmmaError(Exception e) {
		reportEmmaError("", e);
	}

	private void reportEmmaError(String hint, Exception e) {
		String msg = "Failed to generate emma coverage. " + hint;
		Log.e(TAG, msg, e);
		mResults.putString(Instrumentation.REPORT_KEY_STREAMRESULT, "\nError: "
				+ msg);
	}

	@Override
	public void onActivityFinished() {
		if (LOGD)
			Log.d(TAG, "onActivityFinished()");
		if (mCoverage) {
			generateCoverageReport();
		}
		finish(Activity.RESULT_OK, mResults);
	}
	
	@Override
	public void dumpIntermediateCoverage(String filePath){
		// TODO Auto-generated method stub
		if(LOGD){
			Log.d(TAG,"Intermidate Dump Called with file name :"+ filePath);
		}
		if(mCoverage){
			if(!setCoverageFilePath(filePath)){
				if(LOGD){
					Log.d(TAG,"Unable to set the given file path:"+filePath+" as dump target.");
				}
			}
			generateCoverageReport();
			setCoverageFilePath(DEFAULT_COVERAGE_FILE_PATH);
		}
	}

}
