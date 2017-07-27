package org.nerdcircus.android.klaxon.tests.app;

import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.os.Bundle;

import org.nerdcircus.android.klaxon.ReplyList;

public class ReplyListTest extends ActivityInstrumentationTestCase2 {

    public ReplyListTest(){
        super("org.nerdcircus.android.klaxon", ReplyList.class);
    }
    public void testLaunch(){
        launchActivity("org.nerdcircus.android.klaxon", ReplyList.class, new Bundle());
        assertTrue(true);
    }

}
