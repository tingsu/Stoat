/* 
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nerdcircus.android.klaxon;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.nerdcircus.android.klaxon.Pager;

public class Notifier extends BroadcastReceiver
{
    public static String TAG = "KlaxonNotifier";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        NotificationManager nm = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        AlarmManager am = (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);

        if(intent.getAction().equals(Pager.PAGE_RECEIVED)){
            Log.d(TAG, "new page received. notifying.");

            //get subject line of page, for notification.
            Cursor cursor = context.getContentResolver().query(intent.getData(),
                    new String[] {Pager.Pages._ID, Pager.Pages.SUBJECT},
                    null, null, null);
            cursor.moveToFirst();
            String page_subj = cursor.getString(cursor.getColumnIndex(Pager.Pages.SUBJECT));

            Notification n = getNotification(context, page_subj);
            n.sound = null; //no noise initially. wait for the delayed ANNOY action below.
            nm.notify(R.string.notify_page, n);

            Intent i = new Intent(Pager.ANNOY_ACTION);
            //we cant use data here, because it makes the silencing fail.
            i.putExtra("notification_text", page_subj);
            PendingIntent annoyintent = PendingIntent.getBroadcast(
                                          context,
                                          0,
                                          i,
                                          PendingIntent.FLAG_CANCEL_CURRENT
                                        );
            
            //TODO: is this the right way to get a PreferenceManager?
            SharedPreferences prefs = context.getSharedPreferences("org.nerdcircus.android.klaxon_preferences", 0);

            Log.d(TAG, "notifcation interval: " + prefs.getString("notification_interval", "unknown"));
            long repeat_interval_ms = new Integer(prefs.getString("notification_interval", "20000")).longValue();
            Log.d(TAG, "notifcation interval: " + repeat_interval_ms);

            // 500 ms delay, to prevent the regular text message noise from stomping on us.
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+500, repeat_interval_ms, annoyintent);
        }
        else if(intent.getAction().equals(Pager.SILENCE_ACTION)){
            Log.d(TAG, "cancelling the notification...");
            Intent i = new Intent(Pager.ANNOY_ACTION);
            PendingIntent annoyintent = PendingIntent.getBroadcast(
                                          context,
                                          0,
                                          i,
                                          PendingIntent.FLAG_CANCEL_CURRENT
                                        );
            am.cancel(annoyintent);
            nm.cancel(R.string.notify_page);
        }
        else if(intent.getAction().equals(Pager.ANNOY_ACTION)){
            Log.e(TAG, "got annoy intent. annoying.");
            //just be annoying.
            Notification n = getNotification(context, intent.getStringExtra("notification_text"));
            nm.notify(R.string.notify_page, n);
        }
        else if(intent.getAction().equals("org.nerdcircus.android.klaxon.REPLY_SENT")){
            //a reply was sent. update state in the db.
            if(Activity.RESULT_OK == getResultCode()){
                Log.d(TAG, "reply successful. updating ack status..");
                //result was sent. update state.
                updateAckStatus(context, intent.getData(), intent.getIntExtra(Pager.EXTRA_NEW_ACK_STATUS, 0));
                return;
            }
            else {
                Log.e(TAG, "reply failed!!! doing nothing.");
                return;
            }
        }
        else {
            Log.e(TAG, "Uncaught Action:" + intent.getAction());
        }
        
    }

    /** return our notification object.
     */
    Notification getNotification(Context context, String subject) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Uri alertsound = Uri.parse(prefs.getString("alert_sound", ""));

        Intent listIntent = new Intent(Intent.ACTION_VIEW);
        listIntent.setType("vnd.android.cursor.dir/pages");
        Intent cancelIntent = new Intent(Pager.SILENCE_ACTION);


        Notification n = new Notification(
            R.drawable.bar_icon,
            "This will be overridden below.", //TODO: make this be "n pages waiting"
            System.currentTimeMillis()
        );
        n.ledARGB=R.color.red;
        n.ledOnMS=1000;
        n.ledOffMS=100;
        n.sound = alertsound;
        n.flags = Notification.FLAG_AUTO_CANCEL | 
                  Notification.FLAG_SHOW_LIGHTS;
        n.contentIntent = PendingIntent.getActivity(context, 0, listIntent, 0);
        n.deleteIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, 0);

        n.tickerText = subject;
        n.contentView = new RemoteViews(context.getPackageName(), R.layout.notification);

        n.contentView.setTextViewText(R.id.text, subject);

        //vibrate!
        if (prefs.getBoolean("vibrate", true)){
            n.vibrate = new long[] {0, 800, 500, 800};
        }

        // default is RING. this will override.
        if (prefs.getBoolean("use_alarm_stream", false)){
            n.audioStreamType = AudioManager.STREAM_ALARM;
        }

        return n;
    }

    private void updateAckStatus(Context c, Uri data, int ack_status){
        Log.d(TAG, "updating acks status for "+data.toString()+" to "+ ack_status);
        ContentValues cv = new ContentValues();
        cv.put(Pager.Pages.ACK_STATUS, new Integer(ack_status));
        int rows = c.getContentResolver().update(data, cv, null, null);
        Log.d(TAG, "Updated rows: "+rows);
        Toast.makeText(c, R.string.reply_ok, Toast.LENGTH_LONG);
    }

}
