/**
 * 
 */
package org.balau.fakedawn.test;

import org.balau.fakedawn.License;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author francesco
 *
 */
public class LicenseTest extends ActivityInstrumentationTestCase2<License> {
	
	protected License mActivity;
	
	public LicenseTest() {
		super("org.balau.fakedawn.License", License.class);
	}

	/* (non-Javadoc)
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		mActivity = getActivity();
	}

	/* (non-Javadoc)
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testViews() {
		View v;
		v = mActivity.findViewById(org.balau.fakedawn.R.id.buttonClose);
		assertNotNull(v);
		assertEquals(v.getClass(), Button.class);
		v = mActivity.findViewById(org.balau.fakedawn.R.id.textViewLicense);
		assertNotNull(v);
		assertEquals(v.getClass(), TextView.class);
	}
}
