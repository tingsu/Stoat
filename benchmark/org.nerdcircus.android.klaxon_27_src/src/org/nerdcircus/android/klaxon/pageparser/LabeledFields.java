package org.nerdcircus.android.klaxon.pageparser;

import android.content.ContentValues;
import android.telephony.SmsMessage;
import android.util.Log;

import org.nerdcircus.android.klaxon.Alert;
import org.nerdcircus.android.klaxon.Pager.Pages;

public class LabeledFields extends Standard {
    /* pageparser for gateways which include labels like "subj:", etc.
     */

    public static String TAG = "PageParser-Labeled";

    public Alert parse(SmsMessage[] msgs){
        Alert a = super.parse(msgs);
        //XXX: this should probably just use the Alert
        return new Alert(doCleanup(a.asContentValues()));
    }

    public Alert parse(String from, String subj, String message_text){
        Alert a = super.parse(from, subj, message_text);
        return new Alert(doCleanup(a.asContentValues()));
    }


    protected ContentValues doCleanup(ContentValues cv){
        cv = super.doCleanup(cv);
        cv = cleanupLabeledFields(cv);
        return cv;
    }

    /*
     * Message Cleanup Functions
     * functions below are intended to "clean up" messages that may not parse correctly.
     * they should be called in the doCleanup() function above
     */

    private ContentValues cleanupLabeledFields(ContentValues cv){
        String body = cv.getAsString(Pages.BODY);
        String[] labels = { "frm:", "subj:", "msg:" };
        String val = getLabeledValue("frm:", body);
        if(val != null){
            cv.put(Pages.FROM_ADDR, val);
        }
        val = getLabeledValue("subj:", body);
        if(val != null){
            cv.put(Pages.SUBJECT, val);
        }
        //Body can contain newlines. so always get the rest.
        int start = body.toLowerCase().indexOf("msg:");
        if(start > -1){
            //increment to exclude the label
            start += 4;
            cv.put(Pages.BODY, body.substring(start));
        }
        return cv;
    }

    private String getLabeledValue(String label, String body){
        int start = body.toLowerCase().indexOf(label);
        if(start == -1){
            return null; // not found
        }
        //increment to exclude the label
        start += label.length();

        int end = body.toLowerCase().indexOf("\n", start);
        if(end == -1){
            return body.substring(start);
        } 
        else {
            return body.substring(start,end);
        }
    }
}
