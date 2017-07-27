package campyre.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import campyre.java.Campfire;
import campyre.java.CampfireException;
import campyre.java.Room;

public class Utils {
	public static final int ABOUT = 0;
	
	public static void alert(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
    
    public static void alert(Context context, CampfireException exception) {
    	String message = exception == null ? "Unhandled error." : exception.getMessage();
    	Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    public static Dialog aboutDialog(Context context) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	LayoutInflater inflater = LayoutInflater.from(context);
    	
    	ScrollView aboutView = (ScrollView) inflater.inflate(R.layout.about, null);
    	
    	Spanned about1 = Html.fromHtml(
    			"Made by <a href=\"" + 
    			context.getResources().getString(R.string.home_link) + 
    			"\">Eric Mill</a>."
    			);
    	TextView aboutView1 = (TextView) aboutView.findViewById(R.id.about_main_1);
    	aboutView1.setText(about1);
    	aboutView1.setMovementMethod(LinkMovementMethod.getInstance());
    	
    	TextView about3 = (TextView) aboutView.findViewById(R.id.about_links);
    	about3.setText(R.string.about_links);
    	Linkify.addLinks(about3, Linkify.WEB_URLS);
    	
    	String versionString = context.getResources().getString(R.string.version_string);
    	((TextView) aboutView.findViewById(R.id.about_version)).setText("Version " + versionString);
    	
    	builder.setView(aboutView);
    	builder.setPositiveButton(R.string.about_button, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		});
        return builder.create();
    }
    
    public static Intent feedbackIntent(Context context) {
    	return new Intent(Intent.ACTION_SENDTO, 
    			Uri.fromParts("mailto", context.getResources().getString(R.string.contact_email), null))
    		.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.contact_subject));
    }
    
    public static Intent shortcutIntent(Context context, Room room) {
    	return new Intent()
    		.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.drawable.icon))
			.putExtra(Intent.EXTRA_SHORTCUT_INTENT, roomIntent(room))
			.putExtra(Intent.EXTRA_SHORTCUT_NAME, room.name);
    }
    
    public static Intent roomIntent(Room room) {
    	return new Intent(Intent.ACTION_MAIN)
    		.putExtra("room_id", room.id)
    		.putExtra("room_name", room.name)
    		.setClassName("campyre.android", "campyre.android.RoomTabs");
    }
    
    public static void installShortcut(Context context, Room room) {
    	context.sendBroadcast(shortcutIntent(context, room)
				.setAction("com.android.launcher.action.INSTALL_SHORTCUT"));
    }
    
    public static Campfire getCampfire(Context context) {
    	SharedPreferences prefs = context.getSharedPreferences("campfire", 0);
    	String user_id = prefs.getString("user_id", null);
        
        if (user_id != null) {
        	String subdomain = prefs.getString("subdomain", null);
            String token = prefs.getString("token", null);
        	return new Campfire(subdomain, token, user_id);
        } else
        	return null;
	}
    
    public static String getCampfireValue(Context context, String key) {
    	return context.getSharedPreferences("campfire", 0).getString(key, null);
    }
	
	public static void saveCampfire(Context context, Campfire campfire) {
		SharedPreferences prefs = context.getSharedPreferences("campfire", 0);
		Editor editor = prefs.edit();
	
		editor.putString("subdomain", campfire.subdomain);
		editor.putString("token", campfire.token);
		editor.putString("user_id", campfire.user_id);
		
		editor.commit();
	}
	
	public static void logoutCampfire(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("campfire", 0);
		Editor editor = prefs.edit();
	
		editor.putString("user_id", null);		
		editor.commit();
	}
	
	public static String getStringPreference(Context context, String key) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
	}
	
	public static String getStringPreference(Context context, String key, String value) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(key, value);
	}

	public static boolean setStringPreference(Context context, String key, String value) {
		return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).commit();
	}
	
	public static boolean getBooleanPreference(Context context, String key, boolean defaultValue) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
	}
	
	public static boolean setBooleanPreference(Context context, String key, boolean value) {
		return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).commit();
	}
	
	// this should probably be moved into the Settings class itself
	public static int getIntPreferenceFromString(Context context, String key, int defaultValue) {
		int value;
		
		String stringValue = getStringPreference(context, key, null);
		if (stringValue == null)
			value = defaultValue;
		else {
			try {
				value = Integer.parseInt(stringValue); 
			} catch (NumberFormatException e) {
				value = defaultValue;
			}
		}
		
		return value;
	}
	
	public static String truncate(String original, int length) {
		if (original.length() > length)
			return original.substring(0, length-1) + "...";
		else
			return original;
	}
	
	public static InputStream openConnection(String urlString) throws CampfireException {
		InputStream inputStream = null;
		
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setRequestMethod("GET");
			httpConn.connect();
	
			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
				inputStream = httpConn.getInputStream();
			else
				throw new CampfireException("Problem downloading image.");
		}
		catch (MalformedURLException e) {
			return null;
		}
		catch (IOException e) {
			throw new CampfireException(e, "Problem downloading image.");
		}
		
		return inputStream;
	}
	
	public static BitmapDrawable imageFromUrl(Context context, String url) throws CampfireException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		Bitmap bitmap = null;
		
		InputStream in = openConnection(url);
		if (in != null) {
			bitmap = BitmapFactory.decodeStream(in, null, options);
			try {
				in.close();
			} catch(IOException e) {
				throw new CampfireException(e, "Error after downloading image.");
			}
		}
		
		if (bitmap == null)
			return null;
		else
			return new BitmapDrawable(context.getResources(), bitmap);
	}
	
	public static void showLoading(Activity activity) {
		activity.findViewById(R.id.empty_message).setVisibility(View.GONE);
		activity.findViewById(R.id.refresh).setVisibility(View.GONE);
		activity.findViewById(R.id.loading).setVisibility(View.VISIBLE);
	}

	public static void setLoading(Activity activity, int message) {
		((TextView) activity.findViewById(R.id.loading_message)).setText(message);
	}

	public static void showRefresh(Activity activity, int message) {
		activity.findViewById(R.id.loading).setVisibility(View.GONE);
		TextView messageView = (TextView) activity.findViewById(R.id.empty_message);
		messageView.setText(message);
		messageView.setVisibility(View.VISIBLE);
		activity.findViewById(R.id.refresh).setVisibility(View.VISIBLE);
	}

	public static void showBack(Activity activity, int message) {
		activity.findViewById(R.id.loading).setVisibility(View.GONE);
		TextView messageView = (TextView) activity.findViewById(R.id.empty_message);
		messageView.setText(message);
		messageView.setVisibility(View.VISIBLE);
		activity.findViewById(R.id.back).setVisibility(View.VISIBLE);	
	}

	public static void showEmpty(Activity activity, int message) {
		activity.findViewById(R.id.loading).setVisibility(View.GONE);
		activity.findViewById(R.id.back).setVisibility(View.GONE);
		TextView messageView = (TextView) activity.findViewById(R.id.empty_message);
		messageView.setText(message);
		messageView.setVisibility(View.VISIBLE);
	}

	public static void setTitle(Activity activity, String title) {
		((TextView) activity.findViewById(R.id.title_text)).setText(title);
	}

	public static void setTitle(Activity activity, int title) {
		((TextView) activity.findViewById(R.id.title_text)).setText(title);
	}

	public static void setTitleIcon(Activity activity, int icon) {
		((ImageView) activity.findViewById(R.id.title_icon)).setImageResource(icon);
	}

	public static void setTitle(Activity activity, int title, int icon) {
		setTitle(activity, title);
		setTitleIcon(activity, icon);
	}

	public static void setTitle(Activity activity, String title, int icon) {
		setTitle(activity, title);
		setTitleIcon(activity, icon);
	}

	public static void setTitleSize(Activity activity, float size) {
		((TextView) activity.findViewById(R.id.title_text)).setTextSize(size);
	}

}