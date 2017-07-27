/*******************************************************************************
 * Copyright (C) 2012-2013 CNRS and University of Strasbourg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.github.cetoolbox;

import com.github.cetoolbox.fragments.tabs.AboutActivity;
import com.github.cetoolbox.fragments.tabs.ExpertActivity;
import com.github.cetoolbox.fragments.tabs.SimpleActivity;
import com.github.cetoolbox.GlobalState;
import android.os.Bundle;
import android.app.TabActivity;
import android.widget.TabHost;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import com.github.cetoolbox.R;

public class CEToolboxActivity extends TabActivity {

	static public GlobalState fragmentData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		fragmentData = null;

		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Reusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab
		Resources res = getResources();
		try {
			intent = new Intent(this.getBaseContext(), SimpleActivity.class);
			/*
			 * intent.setClassName("proteomics.mobile.workbench",
			 * "CEToolboxSimpleActivity");
			 */
			/* .setClass(this, CEToolboxSimpleActivity.class); */
			spec = tabHost.newTabSpec("simple");
			spec.setContent(intent);
			spec.setIndicator("Simple");
			tabHost.addTab(spec);

			intent = new Intent(this.getBaseContext(), ExpertActivity.class);
			spec = tabHost.newTabSpec("expert");
			spec.setContent(intent);
			spec.setIndicator("Expert");
			tabHost.addTab(spec);

			intent = new Intent(this.getBaseContext(), AboutActivity.class);
			spec = tabHost.newTabSpec("about");
			spec.setContent(intent);
			spec.setIndicator("About");
			tabHost.addTab(spec);

			tabHost.setCurrentTab(0);
		} catch (ActivityNotFoundException e) {
			/* e.printStackTrace(); */
		}

	}
}
