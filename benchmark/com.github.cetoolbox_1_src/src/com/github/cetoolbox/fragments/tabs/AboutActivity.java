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
package com.github.cetoolbox.fragments.tabs;

import com.github.cetoolbox.R;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import java.io.InputStream;
import java.io.IOException;

public class AboutActivity extends Activity {

	/** Called when the activity is first created. */
	WebView aboutView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		aboutView = (WebView) findViewById(R.id.aboutWebView);
		try {
			InputStream webPage = getAssets().open("about.html");
			byte[] buffer = new byte[webPage.available()];
			webPage.read(buffer);
			webPage.close();
			aboutView.loadData(new String(buffer), "text/html", "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

