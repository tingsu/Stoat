package biz.gyrus.yaab;

import android.app.Activity;
import android.content.Intent;
import biz.gyrus.yaab.BrightnessController.BrightnessStatus;

public class ThemeHelper {
	
	public static int getCurrentThemeId()
	{
		int themeId = R.style.AppTheme;
		
		BrightnessStatus bs = BrightnessController.get().getBrightnessStatus();
		if(bs == BrightnessStatus.AutoNight || bs == BrightnessStatus.ForceNight)
			themeId = R.style.AppThemeNight;
		
		return themeId;
	}
	
	public static int onActivityApplyCurrentTheme(Activity ctx)
	{
		int themeId = getCurrentThemeId();
		
		if(themeId != -1 && themeId != android.R.style.Theme)
		{
			ctx.setTheme(themeId);
			return themeId;
		}
		
		return -1;
	}
	
	public static void changeCurrentTheme(Activity ctx)
	{
		ctx.finish();
		ctx.startActivity(new Intent(ctx, ctx.getClass()));
	}

}
