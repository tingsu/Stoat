package org.nerdcircus.android.klaxon.tests.app;

import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.os.Bundle;

import org.nerdcircus.android.klaxon.Preferences;

public class PreferencesTest extends ActivityInstrumentationTestCase2 {

    public PreferencesTest(){
        super("org.nerdcircus.android.klaxon", Preferences.class);
    }
    public void testLaunch(){
        getActivity();
        assertTrue(true);
    }

}
