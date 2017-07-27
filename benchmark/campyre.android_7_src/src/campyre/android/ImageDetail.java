package campyre.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import campyre.android.LoadImageTask.LoadsImage;

public class ImageDetail extends Activity implements LoadsImage {
	private static String TIMESTAMP_FORMAT = "MMM d, h:mm a";
	
	String roomName;
	String person, url;
	Date timestamp;
	
	LoadImageTask loadImageTask;
	BitmapDrawable image;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image);
		
		Bundle extras = getIntent().getExtras();
		roomName = extras.getString("room_name");
		person = extras.getString("person");
		url = extras.getString("url");
		timestamp = (Date) extras.getSerializable("timestamp");
		
		setupControls();
		
		ImageDetailHolder holder = (ImageDetailHolder) getLastNonConfigurationInstance();
        if (holder != null) {
	    	loadImageTask = holder.loadImageTask;
	    	image = holder.image;
        }
        
        if (loadImageTask != null)
    		loadImageTask.onScreenLoad(this);
        
        loadImage();
	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
    	return new ImageDetailHolder(loadImageTask, image);
    }
	
	public void setupControls() {
		setTitle(roomName);
		
		String formatted = new SimpleDateFormat(TIMESTAMP_FORMAT).format(timestamp);
		
		((TextView) findViewById(R.id.person)).setText(person + ", at " + formatted + ":");
	}
	
	public void loadImage() {
		if (loadImageTask == null) {
			if (image == null)
				loadImageTask = (LoadImageTask) new LoadImageTask(this, null).execute(url);
			else
				onLoadImage(image, null);
		}
	}
	
	@Override
	public void onLoadImage(BitmapDrawable image, Object tag) {
		this.loadImageTask = null;
		this.image = image;
		
		if (image != null) {
			ImageView view = (ImageView) findViewById(R.id.image);
			view.setImageDrawable(image);
			
			findViewById(R.id.loading).setVisibility(View.GONE);
			view.setVisibility(View.VISIBLE);
			
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				}
			});
		} else {
			findViewById(R.id.loading_spinner).setVisibility(View.GONE);
			((TextView) findViewById(R.id.loading_message)).setText(R.string.image_failed);
		}
	}
	
	@Override
	public Context getContext() {
		return this;
	}
	
	static class ImageDetailHolder {
		LoadImageTask loadImageTask;
		BitmapDrawable image;
		
		public ImageDetailHolder(LoadImageTask loadImageTask, BitmapDrawable image) {
			this.loadImageTask = loadImageTask;
			this.image = image;
		}
	}
}