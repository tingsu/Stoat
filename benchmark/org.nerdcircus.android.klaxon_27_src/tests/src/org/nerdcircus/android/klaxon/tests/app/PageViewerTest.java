package org.nerdcircus.android.klaxon.tests.app;

import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.content.ContentValues;
import android.util.Log;

import org.nerdcircus.android.klaxon.PageViewer;
import org.nerdcircus.android.klaxon.PagerProvider;
import org.nerdcircus.android.klaxon.Pager.Pages;

public class PageViewerTest extends ActivityInstrumentationTestCase2 {
    private Uri testpageUri = null;

    public PageViewerTest(){
        super("org.nerdcircus.android.klaxon", PageViewer.class);
    }

    public void setUp(){
        Log.d("KlaxonTest", "setting up..");
        //insert a dummy page, so we have one to view..
        ContentValues cv = new ContentValues();
        cv.put(Pages.SERVICE_CENTER, "00001");
        cv.put(Pages.SENDER, "someone@example.com");
        cv.put(Pages.SUBJECT, "something urgent");
        cv.put(Pages.ACK_STATUS, 0);
        cv.put(Pages.FROM_ADDR, "someone@example.com");
        cv.put(Pages.BODY, "the body of a message that is very important!");
        cv.put(Pages.TRANSPORT, "nonexistant");
        Log.d("KlaxonTest", "trying to insert some data...");
        this.testpageUri = getInstrumentation().getTargetContext().getContentResolver().insert(Pages.CONTENT_URI, cv);
        Log.d("KlaxonTest", "inserted: " + this.testpageUri.toString() );
        Intent i = new Intent(Intent.ACTION_VIEW, this.testpageUri);
        setActivityIntent(i);

    }

    public void tearDown(){
        Log.d("KlaxonTest", "tearing down.");
        if(this.testpageUri != null){
            getActivity().getContentResolver().delete(this.testpageUri, null, null);
        }
    }
        
    public void testLaunchWithViewAction(){
        Log.d("XXXX", "attempting to launch with: " + this.testpageUri.toString());
        getActivity();
        assertTrue(true);
    }

}
