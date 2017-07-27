package biz.gyrus.yaab;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

public class ActivatorView extends View {
	
	private static class OrientationObservable extends Observable
	{
		private int _iCurrentOrientation = -1;
		
		public void initOrientation(int orientation) { _iCurrentOrientation = orientation; }
		
		public void setOrientation(int orientation) {
			if(_iCurrentOrientation != orientation)
				setChanged();
			
			_iCurrentOrientation = orientation;
			
			notifyObservers();
		}
		
	}
	
	private OrientationObservable _oo = new OrientationObservable();

	public ActivatorView(Context context) {
		super(context);
		
		_oo.initOrientation(getResources().getConfiguration().orientation);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(0, 0);
	}
	
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		
		_oo.setOrientation(newConfig.orientation);
		super.onConfigurationChanged(newConfig);
	}

	public void addOrientationObserver(Observer o) { _oo.addObserver(o); }
	public void delOrientationObserver(Observer o) { _oo.deleteObserver(o); }
}
