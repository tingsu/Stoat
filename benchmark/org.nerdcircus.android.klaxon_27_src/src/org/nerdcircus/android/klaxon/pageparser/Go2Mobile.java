package org.nerdcircus.android.klaxon.pageparser;

import android.content.ContentValues;
import android.telephony.SmsMessage;
import android.util.Log;

import org.nerdcircus.android.klaxon.Alert;
import org.nerdcircus.android.klaxon.Pager.Pages;

public class Go2Mobile extends Standard {
    /* pageparser for go2mobile.com
     * basically just Standard, but with ':' as a linebreak
     *
     * Messages for this parser should look like this:
     * "sender@example.com:subject:body"
     */

    public static String TAG = "PageParser-Go2Mobile";

    protected ContentValues doCleanup(ContentValues cv){
        cv = parseColonLineEndings(cv);
        Log.d(TAG, "after go2mobile parsing:");
        Log.d(TAG, "From: " + cv.getAsString(Pages.FROM_ADDR));
        Log.d(TAG, "subj: " + cv.getAsString(Pages.SUBJECT));
        Log.d(TAG, "body: " + cv.getAsString(Pages.BODY));
        cv = super.doCleanup(cv);
        Log.d(TAG, "after super.doCleanup():");
        Log.d(TAG, "From: " + cv.getAsString(Pages.FROM_ADDR));
        Log.d(TAG, "subj: " + cv.getAsString(Pages.SUBJECT));
        Log.d(TAG, "body: " + cv.getAsString(Pages.BODY));
        return cv;
    }

    /*
     * Message Cleanup Functions
     * functions below are intended to "clean up" messages that may not parse correctly.
     * they should be called in the doCleanup() function above
     */

    private ContentValues parseColonLineEndings(ContentValues cv){
        String body = cv.getAsString(Pages.BODY);
        String[] fields = body.split(":", 3);
        if(fields.length != 3){
            Log.d(TAG, "wrong number of colon-splits!");
            return cv; //wrong number of splits. 
        }
        cv.put(Pages.FROM_ADDR, fields[0]);
        cv.put(Pages.SUBJECT, fields[1]);
        cv.put(Pages.BODY, fields[2]);
        return cv;
    }
}
