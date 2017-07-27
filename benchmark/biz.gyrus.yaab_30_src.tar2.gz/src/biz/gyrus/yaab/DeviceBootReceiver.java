package biz.gyrus.yaab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent arg1) {

		Log.i(Globals.TAG, "Device boot broadcast received.");
		
		if(!BrightnessController.get().isLightSensorPresent(ctx))
		{
			Log.w(Globals.TAG, "Light sensor not found, exit.");
			return;
		}
		
		AppSettings s = new AppSettings(ctx);
		if(s.getAutostart())
		{
			Log.i(Globals.TAG, "Autostart configured, starting service.");
			
			Intent srvIntent = new Intent(ctx, LightMonitorService.class);
			ctx.startService(srvIntent);
		}
	}

}
