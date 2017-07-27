package com.frankcalise.h2droid;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Entry {
	private int id = 0;
	private String date;
	private double nonMetricAmount;
	private double metricAmount;
	
	public static final double ouncePerMililiter = 0.0338140227;
	
	// String Date getter with passed in format
	public String getDateWithFormat(String _format) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDate = null;
		
		try {
			Date date = sdf.parse(this.date);
			sdf = new SimpleDateFormat(_format);
			formattedDate = sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return formattedDate;
	}
	
	public int getId() { return id; }
	public String getDate() { return getDateWithFormat("yyyy-MM-dd HH:mm:ss"); }
	public double getNonMetricAmount() { return nonMetricAmount; }
	public double getMetricAmount() { return metricAmount; }
	
	/** Constructor for Entry
	 * 
	 * @param _date The date of the entry
	 * @param _amount The amount of water consumed at this date
	 * @param _isNonMetric True/false if the amount was in non-Metric units
	 * 
	 * Depending on _isNonMetric, units will be converted to get 
	 * the other amount for second double field
	 */
	public Entry(String _date, double _amount, boolean _isNonMetric) {
		// If no date is passed, generate date/time for now
		if (_date == null) {
			Date now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = sdf.format(now);
		} else {
			date = _date;
		}
		
		if (_isNonMetric) {
			nonMetricAmount = _amount;
			metricAmount = convertToMetric(_amount);
		} else {
			metricAmount = _amount;
			nonMetricAmount = convertToNonMetric(_amount);
		}
	}
	
	/** Constructor for Entry, use now as date
	 * 
	 * @param _amount The amount of water consumed 
	 * @param _isNonMetric
	 */
	public Entry(double _amount, boolean _isNonMetric) {
		this(null, _amount, _isNonMetric);
	}
	
	public Entry(int _id, String _date, double _amount, boolean _isNonMetric) {
		this(_date, _amount, _isNonMetric);
		id = _id;
	}
	
	/** Received amount in non-Metric units, convert to Metric */
	private double convertToMetric(double _amount) {
		return _amount / ouncePerMililiter;
	}
	
	/** Received amount in Metric units, convert to non-Metric */
	private double convertToNonMetric(double _amount) {
		return _amount * ouncePerMililiter;
	}
	
	/** Entry toString */
	@Override
	public String toString() {
		return ("(" + id + ") " + date + "- metric: " + metricAmount + " ml - nonmetric: "
			   + nonMetricAmount + " fl oz"); 
	}
}
