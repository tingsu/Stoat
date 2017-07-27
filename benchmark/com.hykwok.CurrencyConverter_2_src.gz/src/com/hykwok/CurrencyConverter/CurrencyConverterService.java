/*
	Copyright 2010, 2012 Kwok Ho Yin

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

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class CurrencyConverterService extends Service {
	// This variable is used for debug log (LogCat)
	private static final String TAG = "CC:Service";
	
	// Intent string for broadcasting
	public static final String ACTIVITY_TO_SERVICE_BROADCAST = "com.hykwok.action.CC_A_TO_S_BROADCAST";
	public static final String SERVICE_TO_ACTIVITY_BROADCAST = "com.hykwok.action.CC_S_TO_A_BROADCAST";
	
	// Intent key for broadcasting
	private static final String BROADCAST_KEY_ROAMING_OPT = "roaming";
	private static final String BROADCAST_KEY_LASTUPDATETIME = "lastupdatetime";

	// EU Bank Currency Rate data source URL
	private static final String EU_BANK_XML_URL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
	
	// broadcast receiver
	private Broadcast_Receiver my_intent_receiver;	
	
	// task delay time (in ms)
	private long task_delay = 60 * 1000;
	
	private long ref_time = 0;
	private boolean ref_roaming = false;
	
	private CurrencyInternetConnection	cc_connection;
	private CurrencyRateParser_ECB		cc_parser_ECB;
	
	private Thread parser_thread;
	private boolean parser_thread_alive = true;

	@Override
	public IBinder onBind(Intent i) {
		Log.d(TAG, "onBind >>>>>");
    	
    	Log.d(TAG, "onBind <<<<<");
		return null;
	}
	
	@Override
	public boolean onUnbind(Intent i) {
		Log.d(TAG, "onUnbind >>>>>");
    	
    	Log.d(TAG, "onUnbind <<<<<");
		return false;		
	}
	
	@Override
	public void onRebind(Intent i) {
		Log.d(TAG, "onRebind >>>>>");
    	
    	Log.d(TAG, "onRebind <<<<<");
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate >>>>>");
		super.onCreate();
		
		cc_connection = new CurrencyInternetConnection(this);
		cc_parser_ECB = new CurrencyRateParser_ECB();
		
		// register broadcast receiver
		IntentFilter filter = new IntentFilter(ACTIVITY_TO_SERVICE_BROADCAST);
		my_intent_receiver = new Broadcast_Receiver();
		registerReceiver(my_intent_receiver, filter);
		
    	Log.d(TAG, "onCreate <<<<<");
	}
	
	@Override
	public void onStart(Intent i, int startId) {
		Log.d(TAG, "onStart >>>>>");    	
		super.onStart(i, startId);
		
		// default values
		ref_time = 0;
		ref_roaming = false;
		
		try {
			ref_time = i.getExtras().getLong(BROADCAST_KEY_LASTUPDATETIME);
			ref_roaming = i.getExtras().getBoolean(BROADCAST_KEY_ROAMING_OPT, false);
		} catch (Exception e) {
			Log.e(TAG, "onStart: " + e.toString());
		}
		
		// start a new thread to handle database update
		parser_thread_alive = true;
		parser_thread = new Thread(mTask);
		parser_thread.start();
		
    	Log.d(TAG, "onStart <<<<<");
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy >>>>>");
		super.onDestroy();
		
		parser_thread_alive = false;
		parser_thread.interrupt();
				
		// remove broadcast receiver
		unregisterReceiver(my_intent_receiver);		
		
    	Log.d(TAG, "onDestroy <<<<<");
	}
	
	// background thread to get data from internet
	private Runnable mTask = new Runnable() {
		long	timediff;
		
		private void delay() {
			try {
				Thread.sleep(task_delay);
			} catch (InterruptedException e) {
				Log.d(TAG, "Parser thread receive interrupt");
			}
		}
		
		public void run() {
			do {
				timediff = getDiffTime(ref_time);
				
				// 86400000 = 24 * 60 * 60 * 1000ms = 1 day
				if(timediff >= 86400000) {
					// update
					try {
						boolean result = cc_connection.TestConnection(EU_BANK_XML_URL);
						
						if(result) {
							if(cc_parser_ECB.StartParser(EU_BANK_XML_URL)) {
								// update last update time
								ref_time = System.currentTimeMillis();
								
								// send data to activity to update database
								sendSettingToActivity();
							}
						}
					} catch (Exception e) {
						Log.e(TAG, "mTask: " + e.toString());
					}
				} else {
					task_delay = 86400000 - timediff;
					Log.d(TAG, "Task: Increase delay time: " + Double.toString((double)task_delay / 3600000) + " hour(s)");				
				}
				
				// call this task again
				delay();
				
			} while(parser_thread_alive);
		}
	};
	
	// receive data from other activities
	public class Broadcast_Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// receive intent from activity
			Log.d(TAG, "receive data from activity >>>>>");
			
			try {
				ref_time = intent.getExtras().getLong(BROADCAST_KEY_LASTUPDATETIME);
				ref_roaming = intent.getExtras().getBoolean(BROADCAST_KEY_ROAMING_OPT, false);
				
				cc_connection.EnableNetworkRoaming(ref_roaming);
			} catch (Exception e) {
				Log.e(TAG, "Broadcast_Receiver:" + e.toString());
			}
		}
	}
	
	// send data to activity
	void sendSettingToActivity() {
		Intent	intent = new Intent(SERVICE_TO_ACTIVITY_BROADCAST);
		
		intent.putExtra(BROADCAST_KEY_LASTUPDATETIME, ref_time);
		
		List<CurrencyRate> onlinedata = cc_parser_ECB.getRates();
		
		for(int i=0; i<onlinedata.size(); i++) {
			try {
				intent.putExtra(onlinedata.get(i).m_name, onlinedata.get(i).m_rate);
			} catch (Exception e) {
				Log.e(TAG, "sendSettingToActivity:" + e.toString());
			}
		}
		
		Log.d(TAG, "send data to activity >>>>>");
		sendBroadcast(intent);
		
		// change delay time since EU back only updates currency rate once per day
		task_delay = 86400000;
	}
	
	// return difference in hour 
	private long getDiffTime(long reftime) {
		long currenttime = System.currentTimeMillis();
		
		return (currenttime - reftime);
	}
}
