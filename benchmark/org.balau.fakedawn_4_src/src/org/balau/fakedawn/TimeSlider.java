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

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author francesco
 *
 */
public class TimeSlider extends IntervalSlider {

	private Listener m_listener;
	private int m_color = 0xFFFFFFFF;
	private Paint m_paint;

	private DawnTime m_startTime;
	private int m_spanMinutes;

	private void construct()
	{
		m_startTime = new DawnTime(0);
		m_spanMinutes = 30;

		m_listener = new Listener();
		setOnClickListener(m_listener);
		setOnCursorsMovedListener(m_listener);

		m_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
		m_paint.setStrokeWidth(0);
	}

	public TimeSlider(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		construct();
	}

	public TimeSlider(Context context, AttributeSet attrs) {
		super(context, attrs);
		construct();
	}

	public TimeSlider(Context context) {
		super(context);
		construct();
	}

	private void updateView()
	{
		m_listener.onCursorsMoved(this, 0.0F, 0.0F);
	}

	public void setRectColor(int color)
	{
		m_color = color;
		updateView();
	}

	public DawnTime getStartTime()
	{
		return new DawnTime(m_startTime.getMinutes());
	}

	public int getSpanTime()
	{
		return m_spanMinutes;
	}

	private void reposCursors(int leftMinutes, int rightMinutes)
	{
		setRightPos(1.0F);
		setLeftPos(0.0F);

		DawnTime leftTime = new DawnTime(leftMinutes);
		setLeftTime(leftTime.getHour(), leftTime.getMinute());

		DawnTime rightTime = new DawnTime(rightMinutes);
		setRightTime(rightTime.getHour(), rightTime.getMinute());
	}

	public int setStartTime(int minutes)
	{
		int rightMinutes = getRightTime().getMinutes();
		int leftMinutes = getLeftTime().getMinutes();

		int minMinutes = leftMinutes;
		if(minutes > minMinutes)
			minutes = minMinutes;

		m_startTime = new DawnTime(minutes);

		int minSpan = rightMinutes - minutes;
		if(m_spanMinutes < minSpan)
			m_spanMinutes = minSpan;

		reposCursors(leftMinutes, rightMinutes);

		updateView();

		return minutes;
	}

	public int setStartTime(DawnTime start)
	{
		return setStartTime(start.getMinutes());
	}

	public int setStartTime(int hour, int minute)
	{
		return setStartTime(new DawnTime(hour, minute));
	}

	public int setSpanTime(int minutes)
	{
		int rightMinutes = getRightTime().getMinutes();
		int leftMinutes = getLeftTime().getMinutes();
		int minSpan = rightMinutes - leftMinutes;
		if(minutes < minSpan)
			minutes = minSpan;
		m_spanMinutes = minutes;

		int minStart = leftMinutes;
		if(m_startTime.getMinutes() > minStart)
			m_startTime = new DawnTime(minStart);

		reposCursors(leftMinutes, rightMinutes);

		updateView();

		return minutes;
	}

	public DawnTime getLeftTime()
	{
		return new DawnTime(
				m_startTime.getMinutes() +
				(int)Math.round(m_spanMinutes*getLeftPos()));
	}

	public DawnTime getRightTime()
	{
		return new DawnTime(
				m_startTime.getMinutes() +
				(int)Math.round(m_spanMinutes*getRightPos()));
	}

	public void setLeftTime(int leftMinutes)
	{
		DawnTime rightTime = getRightTime();
		if(rightTime.getMinutes() < leftMinutes)
			rightTime = new DawnTime(leftMinutes);
		if(leftMinutes < m_startTime.getMinutes())
			m_startTime = new DawnTime(leftMinutes);
		if(rightTime.getMinutes() > m_startTime.getMinutes() + m_spanMinutes)
			m_spanMinutes = rightTime.getMinutes() - m_startTime.getMinutes();
		setRightPos(
				((float)(rightTime.getMinutes()-m_startTime.getMinutes())) /
				((float)m_spanMinutes));
		setLeftPos(
				((float)(leftMinutes-m_startTime.getMinutes())) /
				((float)m_spanMinutes));
		updateView();
	}

	public void setLeftTime(DawnTime leftTime)
	{
		setLeftTime(leftTime.getMinutes());
	}

	public void setLeftTime(int hour, int minute)
	{
		setLeftTime(new DawnTime(hour, minute));
	}

	public void setRightTime(int rightMinutes)
	{
		DawnTime leftTime = getLeftTime();
		if(leftTime.getMinutes() > rightMinutes)
			leftTime = new DawnTime(rightMinutes);
		if(leftTime.getMinutes() < m_startTime.getMinutes())
			m_startTime = new DawnTime(leftTime.getMinutes());
		if(rightMinutes > m_startTime.getMinutes() + m_spanMinutes)
			m_spanMinutes = rightMinutes - m_startTime.getMinutes();
		setLeftPos(
				((float)(leftTime.getMinutes()-m_startTime.getMinutes())) /
				((float)m_spanMinutes));
		setRightPos(
				((float)(rightMinutes-m_startTime.getMinutes())) /
				((float)m_spanMinutes));
		updateView();
	}

	public void setRightTime(DawnTime rightTime)
	{
		setRightTime(rightTime.getMinutes());
	}

	public void setRightTime(int hour, int minute)
	{
		setRightTime(new DawnTime(hour, minute));
	}

	private class Listener implements OnClickListener, OnCursorsMovedListener {

		public void onClick(View v) {

		}

		public void onCursorsMoved(IntervalSlider i, float leftMovement,
				float rightMovement) {

			setLeftText(getLeftTime().toString());
			setRightText(getRightTime().toString());

			int colors[] = new int[] {0xFF000000, 0xFF000000, m_color, m_color};
			Shader s = new SweepGradient(0, 0, colors, null);
			s = new LinearGradient(
					getLeftPos()*getWidth()-0.1F, 0,
					getRightPos()*getWidth()+0.1F, 0, 
					0xFF000000, m_color,
					Shader.TileMode.CLAMP);
			m_paint.setShader(s);
			setRectPaint(m_paint);

			if((leftMovement != 0.0F || rightMovement != 0.0F) && 
					(m_timesChangedListener != null))
			{
				m_timesChangedListener.onTimesChanged((TimeSlider) i);
			}
		}

	}

	public interface OnTimesChangedListener {
		void onTimesChanged(TimeSlider s);
	}

	private OnTimesChangedListener m_timesChangedListener = null;

	public void setOnTimesChangedListener(OnTimesChangedListener l)
	{
		m_timesChangedListener = l;
	}

	/* (non-Javadoc)
	 * @see com.balau.helloandroid.IntervalSlider#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		updateView();
	}

	public static class DawnTime
	{
		public DawnTime(int hour, int minute)
		{
			m_hour = hour;
			m_minute = minute;
		}

		public DawnTime(int minutes)
		{
			m_hour = minutes/60;
			m_minute = minutes - m_hour*60;
		}

		private int m_hour;
		private int m_minute;

		public int getMinute()
		{
			return m_minute;
		}

		public int getMinutes()
		{
			return m_minute + 60*m_hour;
		}

		public int getHour()
		{
			return m_hour;
		}

		public int getHourOfDay()
		{
			return m_hour % 24;
		}

		@Override
		public String toString()
		{
			return String.format("%02d:%02d", getHourOfDay(), getMinute());
		}
	}

	private static class SavedState extends BaseSavedState {
		int startTimeMinutes;
		int spanTime;
		int color;

		public SavedState(Parcel source) {
			super(source);

			startTimeMinutes = source.readInt();
			spanTime = source.readInt();
			color = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);

			dest.writeInt(startTimeMinutes);
			dest.writeInt(spanTime);
			dest.writeInt(color);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	/* (non-Javadoc)
	 * @see android.view.View#onRestoreInstanceState(android.os.Parcelable)
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState s = (SavedState) state;
		super.onRestoreInstanceState(s.getSuperState());
		m_spanMinutes = s.spanTime;
		m_startTime = new DawnTime(s.startTimeMinutes);
		m_color = s.color;
		updateView();
	}

	/* (non-Javadoc)
	 * @see android.view.View#onSaveInstanceState()
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable p = super.onSaveInstanceState(); 
		SavedState s = new SavedState(p);
		s.spanTime = m_spanMinutes;
		s.startTimeMinutes = m_startTime.getMinutes();
		s.color = m_color;
		return s;
	}
}
