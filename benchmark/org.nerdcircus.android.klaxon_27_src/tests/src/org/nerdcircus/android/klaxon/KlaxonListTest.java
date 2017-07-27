package org.nerdcircus.android.klaxon;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class org.nerdcircus.android.klaxon.KlaxonListTest \
 * org.nerdcircus.android.klaxon.tests/android.test.InstrumentationTestRunner
 */
public class KlaxonListTest extends ActivityInstrumentationTestCase2<KlaxonList> {

    public KlaxonListTest() {
        super("org.nerdcircus.android.klaxon", KlaxonList.class);
    }

}
