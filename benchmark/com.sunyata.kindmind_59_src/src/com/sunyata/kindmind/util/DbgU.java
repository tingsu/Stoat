package com.sunyata.kindmind.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.sunyata.kindmind.BuildConfig;
import com.sunyata.kindmind.List.ListTypeM;

public class DbgU {

	private static final int STACK_TRACE_LINE = 3;
	private static final String APP_TAG = "kindmind";
	public static final int NO_VALUE_SET = -2;
	//-Used for indicating that a value has not been set. -2 was chosen because the db uses -1.
	//There is nothing similar for non-int/non-long values
	
	public static String getMethodName(String inPrefix){
		return "[" + inPrefix + "]" + getMethodName();
	}
	public static String getMethodName(int inListType){
		if(inListType != ListTypeM.NOT_SET){
			return Thread.currentThread().getStackTrace()[3].getMethodName()
					+ "[" + ListTypeM.getListTypeString(inListType) + "]";
		}else{
			return Thread.currentThread().getStackTrace()[3].getMethodName() + "[N/A]";
		}
	}
	//Used for filtering with logcat, the class name has been moved into getMethodName
	public static String getAppTag(){
		return APP_TAG;
	}
	public static String getMethodName(){
		//Extracting the class ("component") name
		String tmpClassWithPackage = Thread.currentThread().getStackTrace()[STACK_TRACE_LINE].getClassName();
		String[] tmpSplitString = tmpClassWithPackage.split("\\.");
		//-Regular expression, so "." means "all"
		//String tmpOrganization = tmpSplitString[tmpSplitString.length-3];
		//String tmpProject = tmpSplitString[tmpSplitString.length-2];
		String tmpComponent = tmpSplitString[tmpSplitString.length-1];
		
		//Extracting the method name
		String tmpMethodName = Thread.currentThread().getStackTrace()[3].getMethodName();
		
		return tmpComponent + "." + tmpMethodName;
	}
	
	public static boolean isReleaseVersion(Context inContext){
		//Checking if this is a beta or alpha version
		PackageInfo tmpPackageInfo = null;
		try {
			tmpPackageInfo = inContext.getPackageManager().getPackageInfo(inContext.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName());
		}
		
		//Checking if this is a debug build
		if(BuildConfig.DEBUG){
			return false;
		}
		
		//Checking if this is a testing version
		String tVerName = tmpPackageInfo.versionName;
		if(tVerName.contains("test") || tVerName.contains("Test") || tVerName.contains("TEST")){
			return false;
		}
		
		return true;
	}
}