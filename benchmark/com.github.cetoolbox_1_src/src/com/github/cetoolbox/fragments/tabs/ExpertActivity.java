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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.Locale;
import com.github.cetoolbox.CEToolboxActivity;
import com.github.cetoolbox.CapillaryElectrophoresis;
import com.github.cetoolbox.GlobalState;
import com.github.cetoolbox.R;

public class ExpertActivity extends Activity implements
		AdapterView.OnItemSelectedListener, View.OnClickListener {

	public static final String PREFS_NAME = "capillary.electrophoresis.toolbox.PREFERENCE_FILE_KEY";

	Button calculate;
	Button reset;
	EditText capillaryLengthValue;
	EditText diameterValue;
	EditText pressureValue;
	EditText durationValue;
	EditText viscosityValue;
	EditText toWindowLengthValue;
	EditText concentrationValue;
	EditText molecularWeightValue;
	EditText voltageValue;
	TextView tvConcentrationUnits;
	Spinner concentrationSpin;
	int concentrationSpinPosition;
	Spinner pressureSpin;
	int pressureSpinPosition;

	CapillaryElectrophoresis capillary;

	Double capillaryLength;
	Double toWindowLength;
	Double diameter;
	Double pressure;
	String pressureUnit;
	Double duration;
	Double viscosity;
	Double concentration;
	String concentrationUnit;
	Double molecularWeight;
	Double voltage;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String languageToLoad = "en";
		Locale locale = new Locale(languageToLoad);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
		this.setContentView(R.layout.expert);

		capillaryLengthValue = (EditText) findViewById(R.id.capillaryLengthValue);
		diameterValue = (EditText) findViewById(R.id.diameterValue);
		toWindowLengthValue = (EditText) findViewById(R.id.toWindowLengthValue);
		pressureValue = (EditText) findViewById(R.id.pressureValue);
		durationValue = (EditText) findViewById(R.id.durationValue);
		viscosityValue = (EditText) findViewById(R.id.viscosityValue);
		concentrationValue = (EditText) findViewById(R.id.concentrationValue);
		molecularWeightValue = (EditText) findViewById(R.id.molecularWeightValue);
		voltageValue = (EditText) findViewById(R.id.voltageValue);
		concentrationSpin = (Spinner) findViewById(R.id.concentrationSpin);
		concentrationSpin.setOnItemSelectedListener(this);
		ArrayAdapter<CharSequence> concentrationUnitsAdapter = ArrayAdapter
				.createFromResource(this, R.array.concentrationUnitArray,
						android.R.layout.simple_spinner_item);
		concentrationUnitsAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		concentrationSpin.setAdapter(concentrationUnitsAdapter);

		pressureSpin = (Spinner) findViewById(R.id.pressureSpin);
		pressureSpin.setOnItemSelectedListener(this);
		ArrayAdapter<CharSequence> pressureUnitsAdapter = ArrayAdapter
				.createFromResource(this, R.array.pressureUnitArray,
						android.R.layout.simple_spinner_item);
		pressureUnitsAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		pressureSpin.setAdapter(pressureUnitsAdapter);

		calculate = (Button) findViewById(R.id.button1);
		calculate.setOnClickListener(this);
		reset = (Button) findViewById(R.id.button2);
		reset.setOnClickListener(this);

		/* Restore preferences */
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		capillaryLength = Double.longBitsToDouble(settings.getLong(
				"capillaryLength", Double.doubleToLongBits(100.0)));
		toWindowLength = Double.longBitsToDouble(settings.getLong(
				"toWindowLength", Double.doubleToLongBits(100.0)));
		diameter = Double.longBitsToDouble(settings.getLong("diameter",
				Double.doubleToLongBits(50.0)));
		pressure = Double.longBitsToDouble(settings.getLong("pressure",
				Double.doubleToLongBits(30.0)));
		pressureSpinPosition = settings.getInt("pressureSpinPosition", 0);
		duration = Double.longBitsToDouble(settings.getLong("duration",
				Double.doubleToLongBits(10.0)));
		viscosity = Double.longBitsToDouble(settings.getLong("viscosity",
				Double.doubleToLongBits(1.0)));
		concentration = Double.longBitsToDouble(settings.getLong(
				"concentration", Double.doubleToLongBits(1.0)));
		concentrationSpinPosition = settings.getInt("concentratinSpinPosition",
				0);
		molecularWeight = Double.longBitsToDouble(settings.getLong(
				"molecularWeight", Double.doubleToLongBits(1000.0)));
		voltage = Double.longBitsToDouble(settings.getLong("voltage",
				Double.doubleToLongBits(30000.0)));

		if (CEToolboxActivity.fragmentData == null) {
			CEToolboxActivity.fragmentData = new GlobalState();
			setGlobalStateValues();
		} else {
			getGlobalStateValues();
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		capillaryLength = savedInstanceState.getDouble("capillaryLength");
		toWindowLength = savedInstanceState.getDouble("toWindowLength");
		diameter = savedInstanceState.getDouble("diameter");
		pressure = savedInstanceState.getDouble("pressure");
		duration = savedInstanceState.getDouble("duration");
		viscosity = savedInstanceState.getDouble("viscosity");
		concentration = savedInstanceState.getDouble("concentration");
		molecularWeight = savedInstanceState.getDouble("molecularWeight");
		voltage = savedInstanceState.getDouble("voltage");

		/* Set GlobalState values */
		CEToolboxActivity.fragmentData.setCapillaryLength(capillaryLength);
		CEToolboxActivity.fragmentData.setToWindowLength(toWindowLength);
		CEToolboxActivity.fragmentData.setDiameter(diameter);
		CEToolboxActivity.fragmentData.setPressure(pressure);
		CEToolboxActivity.fragmentData.setPressureSpinPosition(pressureSpin
				.getSelectedItemPosition());
		CEToolboxActivity.fragmentData.setDuration(duration);
		CEToolboxActivity.fragmentData.setViscosity(viscosity);
		CEToolboxActivity.fragmentData.setConcentration(concentration);
		CEToolboxActivity.fragmentData
				.setConcentrationSpinPosition(concentrationSpin
						.getSelectedItemPosition());
		CEToolboxActivity.fragmentData.setMolecularWeight(molecularWeight);
	}

	@Override
	public void onResume() {
		super.onResume();

		getGlobalStateValues();

		/* Initialize content */
		editTextInitialize();
	}

	private void getGlobalStateValues() {
		/* Get GlobalState values */
		capillaryLength = CEToolboxActivity.fragmentData.getCapillaryLength();
		toWindowLength = CEToolboxActivity.fragmentData.getToWindowLength();
		diameter = CEToolboxActivity.fragmentData.getDiameter();
		pressure = CEToolboxActivity.fragmentData.getPressure();
		pressureSpinPosition = CEToolboxActivity.fragmentData
				.getPressureSpinPosition();
		duration = CEToolboxActivity.fragmentData.getDuration();
		viscosity = CEToolboxActivity.fragmentData.getViscosity();
		concentration = CEToolboxActivity.fragmentData.getConcentration();
		concentrationSpinPosition = CEToolboxActivity.fragmentData
				.getConcentrationSpinPosition();
		molecularWeight = CEToolboxActivity.fragmentData.getMolecularWeight();

	}

	private void setGlobalStateValues() {
		/* Set GlobalState values */
		CEToolboxActivity.fragmentData.setCapillaryLength(capillaryLength);
		CEToolboxActivity.fragmentData.setToWindowLength(toWindowLength);
		CEToolboxActivity.fragmentData.setDiameter(diameter);
		CEToolboxActivity.fragmentData.setPressure(pressure);
		CEToolboxActivity.fragmentData
				.setPressureSpinPosition(pressureSpinPosition);
		CEToolboxActivity.fragmentData.setDuration(duration);
		CEToolboxActivity.fragmentData.setViscosity(viscosity);
		CEToolboxActivity.fragmentData.setConcentration(concentration);
		CEToolboxActivity.fragmentData
				.setConcentrationSpinPosition(concentrationSpinPosition);
		CEToolboxActivity.fragmentData.setMolecularWeight(molecularWeight);
	}

	private void editTextInitialize() {
		capillaryLengthValue.setText(capillaryLength.toString());
		diameterValue.setText(diameter.toString());
		toWindowLengthValue.setText(toWindowLength.toString());
		pressureValue.setText(pressure.toString());
		pressureSpin.setSelection(pressureSpinPosition);
		durationValue.setText(duration.toString());
		viscosityValue.setText(viscosity.toString());
		concentrationValue.setText(concentration.toString());
		concentrationSpin.setSelection(concentrationSpinPosition);
		molecularWeightValue.setText(molecularWeight.toString());
		voltageValue.setText(voltage.toString());
	}

	/*
	 * Validate the content of the EditText widget
	 */
	private String parseEditTextContent() {
		String errorMessage = "";

		/* Verify that no field are empty */
		if (capillaryLengthValue.getText().length() == 0) {
			errorMessage = "The capillary length field is empty.";
		} else if (toWindowLengthValue.getText().length() == 0) {
			errorMessage = "The length to window field is empty.";
		} else if (diameterValue.getText().length() == 0) {
			errorMessage = "The diameter field is empty.";
		} else if (pressureValue.getText().length() == 0) {
			errorMessage = "The pressure field is empty.";
		} else if (durationValue.getText().length() == 0) {
			errorMessage = "The duration field is empty.";
		} else if (viscosityValue.getText().length() == 0) {
			errorMessage = "The viscosity field is empty.";
		} else if (concentrationValue.getText().length() == 0) {
			errorMessage = "The concentration field is empty.";
		} else if (molecularWeightValue.getText().length() == 0) {
			errorMessage = "The molecular weight field is empty.";
		} else if (voltageValue.getText().length() == 0) {
			errorMessage = "The voltage field is empty.";
		}

		if (errorMessage.length() == 0) {
			if (Double.valueOf(capillaryLengthValue.getText().toString()) == 0) {
				errorMessage = "The capillary length can not be null.";
			} else if (Double.valueOf(toWindowLengthValue.getText().toString()) == 0) {
				errorMessage = "The length to window can not be null.";
			} else if (Double.valueOf(diameterValue.getText().toString()) == 0) {
				errorMessage = "The diameter can not be null.";
			} else if (Double.valueOf(pressureValue.getText().toString()) == 0) {
				errorMessage = "The pressure can not be null.";
			} else if (Double.valueOf(durationValue.getText().toString()) == 0) {
				errorMessage = "The duration can not be null.";
			} else if (Double.valueOf(viscosityValue.getText().toString()) == 0) {
				errorMessage = "The viscoty can not be null.";
			} else if (Double.valueOf(concentrationValue.getText().toString()) == 0) {
				errorMessage = "The concentration can not be null.";
			} else if (Double
					.valueOf(molecularWeightValue.getText().toString()) == 0) {
				errorMessage = "The molecular weight can not be null.";
			} else if (Double.valueOf(voltageValue.getText().toString()) == 0) {
				errorMessage = "The voltage can not be null.";
			}
		}

		return errorMessage;
	}

	@Override
	public void onClick(View view) {
		if (view == calculate) {
			boolean isFull = false;
			boolean validatedValues = false;
			Double pressureMBar = 0.0;
			String errorMessage;

			errorMessage = parseEditTextContent();
			if (errorMessage.length() == 0) {
				validatedValues = true;
			}
			if (validatedValues) {
				/* Parameter validation */
				diameter = Double.valueOf(diameterValue.getText().toString());
				duration = Double.valueOf(durationValue.getText().toString());
				viscosity = Double.valueOf(viscosityValue.getText().toString());
				capillaryLength = Double.valueOf(capillaryLengthValue.getText()
						.toString());
				pressure = Double.valueOf(pressureValue.getText().toString());
				if (pressureUnit.compareTo("psi") == 0) {
					pressureMBar = pressure * 6894.8 / 100;
				} else {
					pressureMBar = pressure;
				}
				toWindowLength = Double.valueOf(toWindowLengthValue.getText()
						.toString());
				concentration = Double.valueOf(concentrationValue.getText()
						.toString());
				molecularWeight = Double.valueOf(molecularWeightValue.getText()
						.toString());
				voltage = Double.valueOf(voltageValue.getText().toString());

				/* Check the values for incoherence */
				if (toWindowLength > capillaryLength) {
					validatedValues = false;
					errorMessage = "The length to window can not be greater than the capillary length";
				}
			}
			if (validatedValues) {
				/* If all is fine, save the data and compute */
				SharedPreferences preferences = getSharedPreferences(
						PREFS_NAME, 0);
				SharedPreferences.Editor editor = preferences.edit();

				editor.putLong("capillaryLength",
						Double.doubleToLongBits(capillaryLength));
				editor.putLong("toWindowLength",
						Double.doubleToLongBits(toWindowLength));
				editor.putLong("diameter", Double.doubleToLongBits(diameter));
				editor.putLong("pressure", Double.doubleToLongBits(pressure));
				editor.putInt("pressureSpinPosition", pressureSpinPosition);
				editor.putLong("duration", Double.doubleToLongBits(duration));
				editor.putLong("viscosity", Double.doubleToLongBits(viscosity));
				editor.putLong("concentration",
						Double.doubleToLongBits(concentration));
				editor.putInt("concentrationSpinPosition",
						concentrationSpinPosition);
				editor.putLong("molecularWeight",
						Double.doubleToLongBits(molecularWeight));
				editor.putLong("voltage", Double.doubleToLongBits(voltage));
				editor.commit();

				capillary = new CapillaryElectrophoresis(pressureMBar,
						diameter, duration, viscosity, capillaryLength,
						toWindowLength, concentration, molecularWeight);

				DecimalFormat doubleDecimalFormat = new DecimalFormat("#.##");
				DecimalFormat singleDecimalFormat = new DecimalFormat("#.#");
				Double deliveredVolume = capillary.getDeliveredVolume(); /* nl */
				Double capillaryVolume = capillary.getCapillaryVolume(); /* nl */
				if (deliveredVolume > capillaryVolume) {
					deliveredVolume = capillaryVolume;
					isFull = true;
				}
				Double capillaryToWindowVolume = capillary.getToWindowVolume(); /* nl */
				Double injectionPlugLength = capillary.getInjectionPlugLength(); /* mm */
				Double timeToReplaceVolume = capillary.getTimeToReplaceVolume(); /* s */
				/* Compute injected quantity of analyte */
				Double analyteMass; /* ng */
				Double analyteMol; /* mmol */
				if (concentrationUnit.compareTo("g/L") == 0) {
					analyteMass = deliveredVolume * concentration;
					analyteMol = analyteMass / molecularWeight * 1000;
				} else {
					analyteMol = deliveredVolume * concentration;
					analyteMass = analyteMol * molecularWeight / 1000;
				}

				Double plugLength = deliveredVolume / capillaryVolume * 100;
				Double plugLengthToWindow = deliveredVolume
						/ capillaryToWindowVolume * 100;

				LayoutInflater li = LayoutInflater.from(this);
				View expertDetailsView = li.inflate(R.layout.expertresults,
						null);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				builder.setView(expertDetailsView);

				TextView title = new TextView(this);
				title.setText("Injection Details");
				title.setTextSize(20);
				title.setBackgroundColor(Color.DKGRAY);
				title.setTextColor(Color.WHITE);
				title.setPadding(10, 10, 10, 10);
				title.setGravity(Gravity.CENTER);
				builder.setCustomTitle(title);

				TextView tvHydrodynamicInjection = (TextView) expertDetailsView
						.findViewById(R.id.hydrodynamicInjectionValue);
				tvHydrodynamicInjection.setText(doubleDecimalFormat
						.format(deliveredVolume) + " nl");

				TextView tvCapillaryVolume = (TextView) expertDetailsView
						.findViewById(R.id.capillaryVolumeValue);
				tvCapillaryVolume.setText(doubleDecimalFormat
						.format(capillaryVolume) + " nl");

				TextView tvCapillaryToWindowVolume = (TextView) expertDetailsView
						.findViewById(R.id.capillaryToWindowVolumeValue);
				tvCapillaryToWindowVolume.setText(doubleDecimalFormat
						.format(capillaryToWindowVolume) + " nl");

				TextView tvInjectionPlugLength = (TextView) expertDetailsView
						.findViewById(R.id.injectionPlugLengthValue);
				tvInjectionPlugLength.setText(singleDecimalFormat
						.format(injectionPlugLength) + " mm");

				TextView tvPlugLength = (TextView) expertDetailsView
						.findViewById(R.id.plugLengthValue);
				tvPlugLength.setText(doubleDecimalFormat.format(plugLength));

				TextView tvPlugLengthToWindow = (TextView) expertDetailsView
						.findViewById(R.id.plugLengthToWindowValue);
				tvPlugLengthToWindow.setText(doubleDecimalFormat
						.format(plugLengthToWindow));

				TextView tvTimeToReplaceVolume = (TextView) expertDetailsView
						.findViewById(R.id.timeToReplaceVolumeValue);
				tvTimeToReplaceVolume.setText(doubleDecimalFormat
						.format(timeToReplaceVolume)
						+ " s\n"
						+ doubleDecimalFormat.format(timeToReplaceVolume / 60)
						+ " min");

				TextView tvInjectedAnalyte = (TextView) expertDetailsView
						.findViewById(R.id.injectedAnalyteValue);
				tvInjectedAnalyte.setText(doubleDecimalFormat
						.format(analyteMass)
						+ " ng\n"
						+ doubleDecimalFormat.format(analyteMol) + " pmol");

				TextView tvInjectionPressure = (TextView) expertDetailsView
						.findViewById(R.id.injectionPressureValue);
				tvInjectionPressure.setText(doubleDecimalFormat
						.format(pressureMBar * duration * 100 / 6894.8)
						+ " psi.s");

				TextView tvFlowRate = (TextView) expertDetailsView
						.findViewById(R.id.flowRateValue);
				tvFlowRate.setText(singleDecimalFormat.format(capillaryVolume
						* 60 / timeToReplaceVolume)
						+ " nL/min");

				TextView tvFieldStrength = (TextView) expertDetailsView
						.findViewById(R.id.fieldStrengthValue);
				tvFieldStrength.setText(doubleDecimalFormat.format(voltage
						/ capillaryLength)
						+ " V/cm");

				if (isFull) {
					TextView tvMessage = (TextView) expertDetailsView
							.findViewById(R.id.expertMessage);
					tvMessage.setTextColor(Color.RED);
					tvMessage.setTypeface(null, Typeface.BOLD);
					tvMessage.setText("Warning: the capillary is full !");
				}
				builder.setNeutralButton("Close",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dlg, int sumthin) {
								// do nothing – it will close on its own
							}
						});

				builder.show();
			} else {

				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				builder.setTitle("Error");
				builder.setMessage(errorMessage);
				builder.setIcon(R.drawable.ic_dialog_error);
				builder.setNeutralButton("Close",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dlg, int sumthin) {
								// do nothing – it will close on its own
							}
						});

				builder.show();
			}
		} else if (view == reset) {
			/* Reset the values to default program settings */
			capillaryLength = 100.0;
			toWindowLength = 100.0;
			diameter = 50.0;
			pressure = 30.0;
			pressureSpinPosition = 0;
			duration = 10.0;
			viscosity = 1.0;
			concentration = 1.0;
			concentrationSpinPosition = 0;
			molecularWeight = 1000.0;
			voltage = 30000.0;

			editTextInitialize();
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

		if (parent == concentrationSpin) {
			concentrationUnit = (String) concentrationSpin
					.getItemAtPosition(position);
			concentrationSpinPosition = position;
		} else if (parent == pressureSpin) {
			pressureUnit = (String) pressureSpin.getItemAtPosition(position);
			pressureSpinPosition = position;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		/* concentrationSpin.setText(""); */
	}

	@Override
	public void onPause() {
		CEToolboxActivity.fragmentData = new GlobalState();
		try {
			CEToolboxActivity.fragmentData.setCapillaryLength(Double
					.valueOf(capillaryLengthValue.getText().toString()));
		} catch (Exception e) {
			CEToolboxActivity.fragmentData.setCapillaryLength(capillaryLength);
		}
		try {
			CEToolboxActivity.fragmentData.setToWindowLength(Double
					.valueOf(toWindowLengthValue.getText().toString()));
		} catch (Exception e) {
			CEToolboxActivity.fragmentData.setToWindowLength(toWindowLength);
		}
		try {
			CEToolboxActivity.fragmentData.setDiameter(Double
					.valueOf(diameterValue.getText().toString()));
		} catch (Exception e) {
			CEToolboxActivity.fragmentData.setDiameter(diameter);
		}
		try {
			CEToolboxActivity.fragmentData.setPressure(Double
					.valueOf(pressureValue.getText().toString()));
		} catch (Exception e) {
			CEToolboxActivity.fragmentData.setPressure(pressure);
		}
		CEToolboxActivity.fragmentData
				.setPressureSpinPosition(pressureSpinPosition);
		try {
			CEToolboxActivity.fragmentData.setDuration(Double
					.valueOf(durationValue.getText().toString()));
		} catch (Exception e) {
			CEToolboxActivity.fragmentData.setDuration(duration);
		}
		try {
			CEToolboxActivity.fragmentData.setViscosity(Double
					.valueOf(viscosityValue.getText().toString()));
		} catch (Exception e) {
			CEToolboxActivity.fragmentData.setViscosity(viscosity);
		}
		try {
			CEToolboxActivity.fragmentData.setConcentration(Double
					.valueOf(concentrationValue.getText().toString()));
		} catch (Exception e) {
			CEToolboxActivity.fragmentData.setConcentration(concentration);
		}
		CEToolboxActivity.fragmentData
				.setConcentrationSpinPosition(concentrationSpinPosition);
		try {
			CEToolboxActivity.fragmentData.setMolecularWeight(Double
					.valueOf(molecularWeightValue.getText().toString()));
		} catch (Exception e) {
			CEToolboxActivity.fragmentData.setMolecularWeight(molecularWeight);
		}

		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		try {
			state.putDouble("capillaryLength",
					Double.valueOf(capillaryLengthValue.getText().toString()));
		} catch (Exception e) {
			state.putDouble("capillaryLength", capillaryLength);
		}
		try {
			state.putDouble("toWindowLength",
					Double.valueOf(toWindowLengthValue.getText().toString()));
		} catch (Exception e) {
			state.putDouble("toWindowLength", toWindowLength);
		}
		try {
			state.putDouble("diameter",
					Double.valueOf(diameterValue.getText().toString()));
		} catch (Exception e) {
			state.putDouble("diameter", diameter);
		}
		try {
			state.putDouble("pressure",
					Double.valueOf(pressureValue.getText().toString()));
		} catch (Exception e) {
			state.putDouble("pressure", pressure);
		}
		state.putInt("pressureSpinPosition", pressureSpinPosition);
		try {
			state.putDouble("duration",
					Double.valueOf(durationValue.getText().toString()));
		} catch (Exception e) {
			state.putDouble("duration", duration);
		}
		try {
			state.putDouble("viscosity",
					Double.valueOf(viscosityValue.getText().toString()));
		} catch (Exception e) {
			state.putDouble("viscosity", viscosity);
		}
		try {
			state.putDouble("concentration",
					Double.valueOf(concentrationValue.getText().toString()));
		} catch (Exception e) {
			state.putDouble("concentration", concentration);
		}
		state.putInt("concentrationSpinPosition", concentrationSpinPosition);
		try {
			state.putDouble("molecularWeight",
					Double.valueOf(molecularWeightValue.getText().toString()));
		} catch (Exception e) {
			state.putDouble("molecularWeight", molecularWeight);
		}
		try {
			state.putDouble("voltage",
					Double.valueOf(voltageValue.getText().toString()));
		} catch (Exception e) {
			state.putDouble("voltage", voltage);
		}

		super.onSaveInstanceState(state);
	}
}
