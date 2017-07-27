package org.nerdcircus.android.klaxon.tests.pageparser;

import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.os.Bundle;
import junit.framework.TestCase;

import org.nerdcircus.android.klaxon.Alert;
import org.nerdcircus.android.klaxon.pageparser.Go2Mobile;

public class Go2MobileTest extends TestCase {

    // standard page. the magic is handled inside of the Android Framework,
    // so this test is pretty straightforward.
    public void testSimple(){
        String message = "test@example.com:subject:alert body";
        Alert expected = new Alert();
        expected.setFrom("1234");
        expected.setDisplayFrom("test@example.com");
        expected.setSubject("subject");
        expected.setBody("alert body");
        Alert observed = (new Go2Mobile()).parse("1234", "", message);
        assertEquals(expected.getFrom(), observed.getFrom());
        assertEquals(expected.getSubject(), observed.getSubject());
        assertEquals(expected.getBody(), observed.getBody());
    }

    public void testSubjectSnippet(){
        String message = "test@example.com::alert body with no alert subject. it has a large body so we only pull the beginning.";
        Alert expected = new Alert();
        expected.setFrom("1234");
        expected.setDisplayFrom("test@example.com");
        expected.setSubject("alert body with no alert subject. it has a large body so we only pull the beginning.".substring(0,40));
        Alert observed = (new Go2Mobile()).parse("1234", "", message);
        assertEquals(expected.getFrom(), observed.getFrom());
        assertEquals(expected.getSubject(), observed.getSubject());
    }

    //*** Go2Mobile-specific tests. ***//

    public void testColonSeparator(){
        String message = "someone@example.com:alert subject:alert body";
        Alert expected = new Alert();
        expected.setFrom("1234");
        expected.setDisplayFrom("someone@example.com");
        expected.setSubject("alert subject");
        expected.setBody("alert body");
        Alert observed = (new Go2Mobile()).parse("1234", "", message);
        assertEquals(expected.getFrom(), observed.getFrom());
        assertEquals(expected.getDisplayFrom(), observed.getDisplayFrom());
        assertEquals(expected.getSubject(), observed.getSubject());
        assertEquals(expected.getBody(), observed.getBody());
    }

    public void testTooManyColons(){
        String message = "test@example.com:some subject:alert:body with :colons";
        Alert expected = new Alert();
        expected.setFrom("1234");
        expected.setDisplayFrom("test@example.com");
        expected.setSubject("some subject");
        expected.setBody("alert:body with :colons");
        Alert observed = (new Go2Mobile()).parse("1234", "", message);
        assertEquals(expected.getFrom(), observed.getFrom());
        assertEquals(expected.getSubject(), observed.getSubject());
        assertEquals(expected.getBody(), observed.getBody());
    }

}
