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

public class GlobalState {
	private Double diameter;
	private Double duration;
	private Double viscosity;
	private Double capillaryLength;
	private Double pressure;
	private Double toWindowLength;
	private Double concentration;
	private Double molecularWeight;
	private int concentrationSpinPosition;
	private int pressureSpinPosition;

	public void setDiameter(Double diameter) {
		this.diameter = diameter;
	}

	public Double getDiameter() {
		return diameter;
	}

	public void setDuration(Double duration) {
		this.duration = duration;
	}

	public Double getDuration() {
		return duration;
	}

	public void setViscosity(Double viscosity) {
		this.viscosity = viscosity;
	}

	public Double getViscosity() {
		return viscosity;
	}

	public void setCapillaryLength(Double capillaryLength) {
		this.capillaryLength = capillaryLength;
	}

	public Double getCapillaryLength() {
		return capillaryLength;
	}

	public void setPressure(Double pressure) {
		this.pressure = pressure;
	}

	public Double getPressure() {
		return pressure;
	}

	public void setToWindowLength(Double toWindowLength) {
		this.toWindowLength = toWindowLength;
	}

	public Double getToWindowLength() {
		return toWindowLength;
	}

	public void setConcentration(Double concentration) {
		this.concentration = concentration;
	}

	public Double getConcentration() {
		return concentration;
	}

	public void setMolecularWeight(Double molecularWeight) {
		this.molecularWeight = molecularWeight;
	}

	public Double getMolecularWeight() {
		return molecularWeight;
	}

	public void setConcentrationSpinPosition(int concentrationSpinPosition) {
		this.concentrationSpinPosition = concentrationSpinPosition;
	}

	public int getConcentrationSpinPosition() {
		return concentrationSpinPosition;
	}

	public void setPressureSpinPosition(int pressureSpinPosition) {
		this.pressureSpinPosition = pressureSpinPosition;
	}

	public int getPressureSpinPosition() {
		return pressureSpinPosition;
	}
}
