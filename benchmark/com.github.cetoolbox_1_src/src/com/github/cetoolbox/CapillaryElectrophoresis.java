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

/**
 * This class contains several functions for volume computation
 * 
 * @author Jerome Pansanel
 */

public class CapillaryElectrophoresis {

	/* The pressure drop across the capillary (mbar) */
	private double pressure;

	/* The capillary inside diameter (micrometer) */
	private double diameter;

	/* The time the pressure is applied (second) */
	private double duration;

	/* The buffer viscosity (cp) */
	private double viscosity;

	/* The length of the capillary (centimeter) */
	private double totalLength;

	/* The window length (centimeter) */
	private double toWindowLength;

	/* The voltage applied to the capillary (kilovolt) */
	private double voltage;

	/* The temperature (C) */
	private double temperature;

	/* Analyte concentration (mol/l) */
	private double concentration;

	/* Analyte molecular weight (g/mol) */
	private double molecularWeight;

	public CapillaryElectrophoresis(double pressure, double diameter,
			double duration, double viscosity, double totalLength,
			double toWindowLength, double concentration, double molecularWeight) {
		this.pressure = pressure;
		this.diameter = diameter;
		this.duration = duration;
		this.viscosity = viscosity;
		this.totalLength = totalLength;
		this.toWindowLength = toWindowLength;
		this.concentration = concentration;
		this.molecularWeight = molecularWeight;
	}

	public void setPressure(double pressure) {
		this.pressure = pressure;
	}

	public void setDiameter(double diameter) {
		this.diameter = diameter;
	}

	public void setPressureTime(double duration) {
		this.duration = duration;
	}

	public void setViscosity(double viscosity) {
		this.viscosity = viscosity;
	}

	public void setTotalLength(double totalLength) {
		this.totalLength = totalLength;
	}

	public void setToWindowLength(double toWindowLength) {
		this.toWindowLength = toWindowLength;
	}

	public void setConcentration(double concentration) {
		this.concentration = concentration;
	}

	public void setMolecularWeight(double molecularWeight) {
		this.molecularWeight = molecularWeight;
	}

	public double getDeliveredVolume() {
		double deliveredVolume;
		deliveredVolume = (pressure * Math.pow(diameter, 4) * Math.PI * duration)
				/ (128 * viscosity * totalLength * Math.pow(10, 5));
		return deliveredVolume;
	}

	public double getCapillaryVolume() {
		double capillaryVolume;
		capillaryVolume = (totalLength * Math.PI * Math.pow(diameter / 2, 2)) / 100;
		return capillaryVolume;
	}

	public double getToWindowVolume() {
		double toWindowVolume;
		toWindowVolume = (toWindowLength * Math.PI * Math.pow(diameter / 2, 2)) / 100;
		return toWindowVolume;
	}

	public double getInjectionPlugLength() {
		double injectionPlugLength;
		injectionPlugLength = (pressure * Math.pow(diameter, 2) * duration)
				/ (32 * viscosity * totalLength * Math.pow(10, 2));
		return injectionPlugLength;
	}

	public double getTimeToReplaceVolume() {
		double timeToReplaceVolume;
		timeToReplaceVolume = (32 * viscosity * Math.pow(totalLength, 2))
				/ (Math.pow(diameter, 2) * Math.pow(10, -3) * pressure);
		return timeToReplaceVolume;
	}
}
