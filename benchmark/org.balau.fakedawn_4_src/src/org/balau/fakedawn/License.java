/**
 *   Copyright 2012 Francesco Balducci
 *
 *   This file is part of FakeDawn.
 *
 *   FakeDawn is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   FakeDawn is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with FakeDawn.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.balau.fakedawn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author francesco
 *
 */
public class License extends Activity implements OnClickListener {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.license);

		Button closeButton = (Button)findViewById(R.id.buttonClose);
		closeButton.setOnClickListener(this);

		TextView licenseText = (TextView) findViewById(R.id.textViewLicense);
		licenseText.setText(readRawTextFile(R.raw.license));

	}

	private String readRawTextFile(int resId)
	{
		// Inspiration:
		// http://stackoverflow.com/questions/4087674/android-read-text-raw-resource-file
		
		InputStream stream = getResources().openRawResource(resId);
		InputStreamReader reader = new InputStreamReader(stream);
		BufferedReader lineReader = new BufferedReader(reader);
		String line;
		StringBuilder text = new StringBuilder();

		try {
			while (( line = lineReader.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		finish();
	}

}
