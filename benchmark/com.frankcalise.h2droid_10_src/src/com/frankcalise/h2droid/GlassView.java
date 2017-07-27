package com.frankcalise.h2droid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class GlassView extends View {
	private float mAmount;
	private Paint mOutlinePaint;
	private Paint mWaterPaint;
	private Paint mGoalTextPaint;
	private Path mFillPath;
	
	public GlassView(Context context) {
		super(context);
		initGlassView();
	}
	
	public GlassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGlassView();
	}
	
	public GlassView(Context context,
					 AttributeSet attrs,
					 int defaultStyle) {
		super(context, attrs, defaultStyle);
		initGlassView();
	}
	
	protected void initGlassView() {
		setFocusable(true);
		
		this.mAmount = 100.0f;
		
		Resources r = this.getResources();
		
		mOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mOutlinePaint.setColor(r.getColor(R.color.outline_color));
		mOutlinePaint.setStrokeWidth(5);
		mOutlinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mWaterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mWaterPaint.setColor(r.getColor(R.color.water_color));
		mWaterPaint.setStyle(Paint.Style.FILL);
		mWaterPaint.setAlpha(150);
		
		mGoalTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mGoalTextPaint.setColor(r.getColor(R.color.black_color));
		mGoalTextPaint.setTextSize(20);
		mGoalTextPaint.setTextAlign(Align.CENTER);
		
		mFillPath = new Path();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = measure(widthMeasureSpec) * 3/4;
		int measuredHeight = measure(heightMeasureSpec);
		
		int d = Math.min(measuredWidth, measuredHeight);
		
		setMeasuredDimension(d, d);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int px = getMeasuredWidth();
		int py = getMeasuredHeight();

		float upperLeftX = 0.0f;
		float upperLeftY = 0.0f;
		float bottomLeftX = px/4;
		float bottomLeftY = py;
		float bottomRightX = (3*px)/4;
		float bottomRightY = bottomLeftY;
		float upperRightX = px;
		float upperRightY = upperLeftY;
		float offset = 5;
		float waterOffset = 3;
		float offsetFromTop = 20;

		// Draw the water in the glass
		// Find the height according to the percentage of the goal complete
		// Subtract from 100% and push water level down
		float goalOffset = py - (mAmount/100)*py;

		// Find the slope of the outline of the glass
		// This slope will be used to determine the x point
		// to fit the water inside the glass
		float glassSlope = getSlope(upperLeftX, upperLeftY, bottomLeftX, bottomLeftY);
		float waterUpperX = (px/4) - getUpperX(bottomLeftX, bottomLeftY, (py-goalOffset), glassSlope);
		
		// Set up the fillPath
		mFillPath.moveTo(waterUpperX+waterOffset, upperLeftY+offsetFromTop+goalOffset);
		mFillPath.lineTo(bottomLeftX, bottomLeftY-offset);
		mFillPath.lineTo(bottomRightX, bottomLeftY-offset);
		mFillPath.lineTo((px-waterUpperX-waterOffset), upperRightY+offsetFromTop+goalOffset);
		
		// Draw the path on the canvas
		canvas.drawPath(mFillPath, mWaterPaint);
		
		// Draw the outline of the glass
		canvas.drawLine(upperLeftX, upperLeftY, bottomLeftX, bottomLeftY, mOutlinePaint);					// diagonal top left to bottom
		canvas.drawLine(bottomLeftX, bottomLeftY, bottomRightX, bottomLeftY, mOutlinePaint);				// center line
		canvas.drawLine(bottomLeftX, bottomLeftY-offset, bottomRightX, bottomLeftY-offset, mOutlinePaint);	// center line offset, to make bottom look thicker
		canvas.drawLine(bottomRightX, bottomRightY, upperRightX, upperRightY, mOutlinePaint);				// diagonal bottom to top right
		
		// Save current matrix and clip onto private stack
		canvas.save();
	}
	
	private float getSlope(float x1, float y1, float x2, float y2) {
		return ((y2 - y1) / (x2 - x1));
	}
	
	private float getUpperX(float bottomX, float bottomY, float upperY, float slope) {
		// y - y1 = m(x - x1)
		// x = ((y - y1) + m * x1) / m
		return ((upperY - bottomY + (slope * bottomX))/slope);
	}
	
	private int measure(int measureSpec) {
		int result = 0;
		
		// Decode the measurement specifications
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 200 if no bounds are specified.
			result = 200;
		} else {
			// As you want to fill up the available space
			// always return the full available bounds
			result = specSize;
		}
		
		return result;
	}
	
	public void setAmount(float _amount) {
		mAmount = _amount;
	}
	
	public float getAmount() {
		return mAmount;
	}
}
