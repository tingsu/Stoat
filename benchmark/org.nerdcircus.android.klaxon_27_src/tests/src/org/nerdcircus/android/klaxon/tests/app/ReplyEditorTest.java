package org.nerdcircus.android.klaxon.tests.app;

import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.content.ContentValues;
import android.util.Log;

import org.nerdcircus.android.klaxon.ReplyEditor;
import org.nerdcircus.android.klaxon.Pager.Replies;

public class ReplyEditorTest extends ActivityInstrumentationTestCase2 {

    public ReplyEditorTest(){
        super("org.nerdcircus.android.klaxon", ReplyEditor.class);
    }

    public void setUp(){
        Log.d("KlaxonTest", "setting up..");
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(Replies.CONTENT_URI + "/1"));
        setActivityIntent(i);

    }

    public void tearDown(){
        Log.d("KlaxonTest", "tearing down.");
    }
        
    public void testLaunch(){
        getActivity();
        assertTrue(true);
    }

}
