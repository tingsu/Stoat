package org.nerdcircus.android.klaxon.tests.app;

import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.os.Bundle;

import org.nerdcircus.android.klaxon.KlaxonList;

public class KlaxonTest extends ActivityInstrumentationTestCase2 {

    public KlaxonTest(){
        super("org.nerdcircus.android.klaxon", KlaxonList.class);
    }
    public void testLaunch(){
        launchActivity("org.nerdcircus.android.klaxon", KlaxonList.class, new Bundle());
        assertTrue(true);
    }

}
