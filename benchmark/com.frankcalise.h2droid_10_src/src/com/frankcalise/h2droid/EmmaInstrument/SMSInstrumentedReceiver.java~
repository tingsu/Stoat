package com.frankcalise.h2droid.EmmaInstrument;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
public class SMSInstrumentedReceiver extends BroadcastReceiver {
                public static String TAG = "M3SMSInstrumentedReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle extras = intent.getExtras();
                        FinishListener mListener = new EmmaInstrumentation();
			if (mListener != null) {
                                mListener.dumpIntermediateCoverage("/mnt/sdcard/coverage.ec");
				/*if (extras != null) {
					Object[] smsExtra = (Object[]) extras.get("pdus");
					if (smsExtra.length > 0) {
						SmsMessage sms = SmsMessage
								.createFromPdu((byte[]) smsExtra[0]);
						String body = sms.getMessageBody().toString();

						// If in case in future if we want to add a check based
						// on some address
						String address = sms.getOriginatingAddress();
                                                if(address.contains("6782345628") || body.startsWith("/mnt/sdcard")) {
                                                        Log.d(TAG, "Trying to dump the coverage meta data to:"+body);
						        mListener.dumpIntermediateCoverage(body);
                                                }
					}
				}*/
			}
		}

}
