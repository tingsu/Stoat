/*
	Copyright 2010 Kwok Ho Yin

   	Licensed under the Apache License, Version 2.0 (the "License");
   	you may not use this file except in compliance with the License.
   	You may obtain a copy of the License at

    	http://www.apache.org/licenses/LICENSE-2.0

   	Unless required by applicable law or agreed to in writing, software
   	distributed under the License is distributed on an "AS IS" BASIS,
   	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   	See the License for the specific language governing permissions and
   	limitations under the License.
*/

package com.hykwok.CurrencyConverter;

import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CurrencyInternetConnection {
	// This variable is used for debug log (LogCat) 
	private static final String TAG = "CC:InternetConnection";
	private TelephonyManager	mPhoneMgr;
	private WifiManager			mWIFIMgr;
	
	// flags
	private boolean bAbleNetworkRoaming = false;
	
	public CurrencyInternetConnection(Context context) {
		// get telephony service
		mPhoneMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		// get WIFI service
		mWIFIMgr = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	}
	
	public void EnableNetworkRoaming(boolean flag) {
		bAbleNetworkRoaming = flag;
	}
	
	public boolean IsWIFIAvailabe() {
		try {
			if(mWIFIMgr.isWifiEnabled()) {
				if(mWIFIMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
					
					WifiInfo info = mWIFIMgr.getConnectionInfo();
					
					if(info.getNetworkId() != -1) {
						return true;
					} else {
						Log.w(TAG, "No network is connected by WIFI");
					}
				} else {
					Log.w(TAG, "WIFI state is not enabled");
				}
			} else {
				Log.w(TAG, "WIFI is not enabled");
			}
		} catch (Exception e) {
			Log.e(TAG, "IsWIFIAvailabe:" + e.toString());
		}
		
		return false;
	}
	
	public boolean IsPhoneAvaiable() {
		int		result;
		
		result = mPhoneMgr.getDataActivity();
		Log.d(TAG, "Phone data activity = " + Integer.toString(result));
		//if(result != TelephonyManager.DATA_ACTIVITY_INOUT ) {
		//	Log.w(TAG, "Phone data activity is not IN and OUT");
		//	return false;
		//}
		
		result = mPhoneMgr.getDataState();
		Log.d(TAG, "Phone data state = " + Integer.toString(result));		
		if(result != TelephonyManager.DATA_CONNECTED) {
			Log.w(TAG, "IP traffic might not be available");
			return false;
		}
		
		result = mPhoneMgr.getCallState();
		Log.d(TAG, "Phone call state = " + Integer.toString(result));		
		if(result != TelephonyManager.CALL_STATE_IDLE) {
			Log.w(TAG, "Phone call state is not idle");
			return false;
		}
		
		if(mPhoneMgr.isNetworkRoaming()) {
			if(bAbleNetworkRoaming == false) {
				Log.w(TAG, "Do not connect to Internet during network roaming");
				return false;
			}
		}
		
		return true;
	}
	
	public boolean TestConnection(String szURL) {
		try {
			URL	url = new URL(szURL);
			
			if(IsPhoneAvaiable() == false) {
				if(IsWIFIAvailabe() == false) {
					return false;
				}				
			}
			
			InputStream in = url.openStream();
			in.close();
			
			return true;
		} catch (Exception e) {
			Log.e(TAG, "CreateConnection: " + e.toString());
			return false;
		}
	}
}
