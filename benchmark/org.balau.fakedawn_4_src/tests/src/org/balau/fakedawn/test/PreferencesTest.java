/**
 * 
 */
package org.balau.fakedawn.test;

import org.balau.fakedawn.Preferences;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;

/**
 * @author francesco
 *
 */
public class PreferencesTest extends ActivityUnitTestCase<Preferences> {

	private Preferences mPreferences;
	
	public PreferencesTest() {
		super(Preferences.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		/*
		Intent preferencesIntent =		new Intent(getInstrumentation()
                .getTargetContext(), Preferences.class);
        startActivity(preferencesIntent, null, null);
		mPreferences = getActivity();
		*/
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	@MediumTest
	public void testButtonSoundClick() {
		/*
		final Button buttonSound = (Button) mPreferences.findViewById(
				org.balau.fakedawn.R.id.buttonSound);
		buttonSound.performClick();
		final Intent pickSound = getStartedActivityIntent();
		assertNotNull(pickSound);
		*/
		
	}
}
