package com.nanoconverter.zlab;

import java.math.BigDecimal;
import com.nanoconverter.zlab.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

public class Widget_1x1 extends AppWidgetProvider {
	
	public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";

    static String id_bank;
    static String id_from;
    static String id_to;
    static String id_from_position;
    static String id_to_position;
    static String design;
    static String update;
    static String source;
    static String theme; 
    static String cur;
    static String[] separated;
    static RemoteViews[] remoteViews;
    static int[] widget_id_store;
    static int N;
    static int ViewID;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
	 int[] appWidgetIds) {

	super.onUpdate(context, appWidgetManager, appWidgetIds);

	N = appWidgetIds.length;
	remoteViews = new RemoteViews[N];
	widget_id_store = new int[N];
	for (int i=0; i<N; i++) {
	         int appWidgetId = appWidgetIds[i];
	         widget_id_store[i] = appWidgetId;
	         updateAppWidget(context, appWidgetManager, appWidgetId);
	     }
	}

	public static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
	 final int appWidgetId){
		for (int i=0; i<N; i++) {
	         if(widget_id_store[i]==appWidgetId){ViewID=i;}}

	// ÏÀÐÀÌÅÒÐÛ
		SharedPreferences DuelPrefs = context.getSharedPreferences("DuelPrefs",0);

		id_from		= DuelPrefs.getString("CUR_FROM"+appWidgetId, "USD");
		id_from_position	= DuelPrefs.getString("CUR_FROM_ID"+appWidgetId, "0");
		id_to		= DuelPrefs.getString("CUR_TO"+appWidgetId, "USD");
		id_to_position	= DuelPrefs.getString("CUR_TO_ID"+appWidgetId, "0");
		id_bank		= DuelPrefs.getString("BANK_IS"+appWidgetId, "Bank");
		design 		= DuelPrefs.getString("DESIGN"+appWidgetId, "square");
		update 		= DuelPrefs.getString("UPDATE"+appWidgetId, "show");
		source 		= DuelPrefs.getString("SOURCE"+appWidgetId, "show");
		cur 		= DuelPrefs.getString("CUR"+appWidgetId, "show");
		theme		= DuelPrefs.getString("THEME"+appWidgetId, "black");

		final SharedPreferences shared_from_app = context.getSharedPreferences("nanostore_shared", 0);
		separated = shared_from_app.getString("rates_from_"+id_bank, "null").split(",");

	    String datestored = shared_from_app.getString("LastUpdateMs"+id_bank, "0");
	    Long lastupdatetimelong = Long.parseLong(datestored);
		Long currenttimelong = Long.parseLong(String.valueOf(System.currentTimeMillis()));
		// Íà÷èíàåì
		
		

		if (design.equals("square")){
			if (theme.equals("black")){
			remoteViews[ViewID] = new RemoteViews(context.getPackageName(), R.layout.widget_1x1);
			remoteViews[ViewID].setImageViewResource(R.id.imageBackground, R.drawable.shape_square);} else
			if (theme.equals("white")){
				remoteViews[ViewID] = new RemoteViews(context.getPackageName(), R.layout.widget_1x1_white);
				remoteViews[ViewID].setImageViewResource(R.id.imageBackground, R.drawable.shape_square_white);
			}
		} else if (design.equals("rounded")){
			if (theme.equals("black")){
			remoteViews[ViewID] = new RemoteViews(context.getPackageName(), R.layout.widget_1x1);
			remoteViews[ViewID].setImageViewResource(R.id.imageBackground, R.drawable.shape_square_rounded);} else
				if (theme.equals("white")){
					remoteViews[ViewID] = new RemoteViews(context.getPackageName(), R.layout.widget_1x1_white);
					remoteViews[ViewID].setImageViewResource(R.id.imageBackground, R.drawable.shape_square_rounded_white);
				}
		} else if (design.equals("circle")){
			if (theme.equals("black")){
			remoteViews[ViewID] = new RemoteViews(context.getPackageName(), R.layout.widget_1x1_circle);
			remoteViews[ViewID].setImageViewResource(R.id.imageBackground, R.drawable.shape_circle);} else
				if (theme.equals("white")){
					remoteViews[ViewID] = new RemoteViews(context.getPackageName(), R.layout.widget_1x1_circle_white);
					remoteViews[ViewID].setImageViewResource(R.id.imageBackground, R.drawable.shape_circle_white);
				}
		}

		if (update.equals("show")){
			remoteViews[ViewID].setViewVisibility(R.id.update_button, View.VISIBLE);
		} else if (update.equals("false")){
			remoteViews[ViewID].setViewVisibility(R.id.update_button, View.INVISIBLE);
		}

		if (source.equals("show")){
			remoteViews[ViewID].setViewVisibility(R.id.bank, View.VISIBLE);
			remoteViews[ViewID].setTextViewText(R.id.bank, id_bank);
		} else if (source.equals("false")){
			remoteViews[ViewID].setViewVisibility(R.id.bank, View.INVISIBLE);
		}

		if (cur.equals("show")){
			remoteViews[ViewID].setTextViewText(R.id.cur_id, id_from+"/"+id_to);
		} else if (cur.equals("from")){
			remoteViews[ViewID].setTextViewText(R.id.cur_id, id_from);
		} else if (cur.equals("to")){
			remoteViews[ViewID].setTextViewText(R.id.cur_id, id_to);
		} else if (cur.equals("false")){
			remoteViews[ViewID].setViewVisibility(R.id.cur_id, View.INVISIBLE);
		}
    	remoteViews[ViewID].setTextViewText(R.id.rate, "...");

/*
    	Log.e("currenttimelong", String.valueOf(currenttimelong));
    	Log.e("lastupdatetimelong", String.valueOf(lastupdatetimelong));
    	Log.e("dif", String.valueOf(currenttimelong/60000-lastupdatetimelong/60000));
*/

    	if (separated.length==1 || (currenttimelong/60000-lastupdatetimelong/60000)>1){
			new Thread() {
				public void run() {
					int ViewIDS=ViewID;
					String bankIDS=id_bank;
					String fromIDS=id_from_position;
					String toIDS=id_to_position;

                	/*Log.e("thread", "start!");*/
            		try {
            			Thread.sleep(2000);
    					if (bankIDS == "CBR"){com.nanoconverter.zlab.NanoConverter.mContext.runLongProcessCBR();} else 
	    			   	if (bankIDS == "NBU"){com.nanoconverter.zlab.NanoConverter.mContext.runLongProcessNBU();} else
	    			   	if (bankIDS == "NBRB"){com.nanoconverter.zlab.NanoConverter.mContext.runLongProcessNBRB();} else
	    			   	if (bankIDS == "BNM"){com.nanoconverter.zlab.NanoConverter.mContext.runLongProcessBNM();} else
	    			   	if (bankIDS == "AZ"){com.nanoconverter.zlab.NanoConverter.mContext.runLongProcessAZ();} else
	    			   	if (bankIDS == "ECB"){com.nanoconverter.zlab.NanoConverter.mContext.runLongProcessECB();} else
	    			   	if (bankIDS == "FOREX") {com.nanoconverter.zlab.NanoConverter.mContext.runLongProcessFOREX();}
    					Thread.sleep(2000);
            			} catch (Exception ioe) {/*Log.e("Error", "update = fail");*/}

	    				separated = shared_from_app.getString("rates_from_"+bankIDS, "null").split(",");
	    				BigDecimal x = null;
				        try {
				        		x = new BigDecimal(Double.parseDouble(separated[Integer.parseInt(fromIDS)]) / Double.parseDouble(separated[Integer.parseInt(toIDS)]));
				        	   	x = x.setScale(2, BigDecimal.ROUND_HALF_UP);
				        	} catch (Exception ioe) {/*Log.e("Error", "null decimal");*/}

				        if (x!=null){remoteViews[ViewIDS].setTextViewText(R.id.rate, String.valueOf(x));}

	                	appWidgetManager.updateAppWidget(appWidgetId, remoteViews[ViewIDS]);

	                	/*Log.e("thread", "done!");*/
	            }
	        }.start();
	        try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
    	} else {

		BigDecimal x = null;
        try {
        		x = new BigDecimal(Double.parseDouble(separated[Integer.parseInt(id_from_position)]) / Double.parseDouble(separated[Integer.parseInt(id_to_position)]));
        	   	x = x.setScale(2, BigDecimal.ROUND_HALF_UP);
        	} catch (Exception ioe)
        	{/*Log.e("Error", "still no data");*/}
        if (x!=null){remoteViews[ViewID].setTextViewText(R.id.rate, String.valueOf(x));}
    	}

		Intent nanointent = new Intent(context, com.nanoconverter.zlab.NanoConverter.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, nanointent, 0);
		remoteViews[ViewID].setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
		
		Intent active = new Intent(context, Widget_1x1.class);
		active.setAction(ACTION_WIDGET_RECEIVER);
		PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
		remoteViews[ViewID].setOnClickPendingIntent(R.id.update_button, actionPendingIntent);

		appWidgetManager.updateAppWidget(appWidgetId, remoteViews[ViewID]);
}
	@Override
    public void onReceive(Context context, Intent intent) {
         final String action = intent.getAction();
         if (ACTION_WIDGET_RECEIVER.equals(action)) {
               AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
               ComponentName thisAppWidget = new ComponentName(context.getPackageName(), Widget_1x1.class.getName());
               int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
               onUpdate(context, appWidgetManager, appWidgetIds);
         }
         super.onReceive(context, intent);
   }
}

