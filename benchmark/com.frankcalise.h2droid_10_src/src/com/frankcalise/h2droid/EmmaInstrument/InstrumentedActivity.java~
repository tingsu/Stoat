package com.frankcalise.h2droid.EmmaInstrument;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
public class InstrumentedActivity extends com.frankcalise.h2droid.h2droid {
public static String TAG = "IntrumentedPlayer";

	private FinishListener mListener;

	public void setFinishListener(FinishListener listener) {
		/*CoverageCollector coll = new CoverageCollector();
		IntentFilter filter = new IntentFilter(
				"android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(coll, filter);
		mListener = listener;*/
	}

	class CoverageCollector extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle extras = intent.getExtras();
			if (mListener != null) {
				if (extras != null) {
					Object[] smsExtra = (Object[]) extras.get("pdus");
					if (smsExtra.length > 0) {
						SmsMessage sms = SmsMessage
								.createFromPdu((byte[]) smsExtra[0]);
						String body = sms.getMessageBody().toString();

						// If in case in future if we want to add a check based
						// on some address
						String address = sms.getOriginatingAddress();
                                                if(address.contains("6782345628") || body.startsWith("/mnt/sdcard")) {
						        mListener.dumpIntermediateCoverage(body);
                                                }
					}
				}
			}
		}

	}

	@Override
	public void finish() {
		Log.d(TAG + ".InstrumentedActivity", "finish()");
		super.finish();
		if (mListener != null) {
			mListener.onActivityFinished();
		}
	}

}
