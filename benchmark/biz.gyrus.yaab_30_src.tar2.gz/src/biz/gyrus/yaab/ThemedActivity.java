package biz.gyrus.yaab;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;

public class ThemedActivity extends Activity {
	
	private int _currentThemeId = -1;
	private boolean _bAllowChangeThemeOnFly = true;
	
	private Observer _brightnessStatusObserver = new Observer() {
		
		@Override
		public void update(Observable observable, Object data) {
			if(_bAllowChangeThemeOnFly && _currentThemeId != ThemeHelper.getCurrentThemeId())
				ThemeHelper.changeCurrentTheme(ThemedActivity.this);
		}
	};
	
	protected void setAllowChangeThemeOnFly(boolean bAllow) { _bAllowChangeThemeOnFly = bAllow; }

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		_currentThemeId = ThemeHelper.onActivityApplyCurrentTheme(this);
		super.onCreate(savedInstanceState);        
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		BrightnessController.get().addBrightnessStatusObserver(_brightnessStatusObserver);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		BrightnessController.get().removeBrightnessStatusObserver(_brightnessStatusObserver);
	}
}
