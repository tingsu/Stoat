package campyre.android;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import campyre.java.CampfireException;

public class LoadImageTask extends AsyncTask<String,Void,BitmapDrawable> {
	public LoadsImage context;
	public Object tag = null;
	
	public Exception exception;
	
	public LoadImageTask(LoadsImage context, Object tag) {
		super();
		this.context = context;
		this.tag = tag;
	}
	
	public void onScreenLoad(LoadsImage context) {
		this.context = context;
	}
	
	@Override
	public BitmapDrawable doInBackground(String... url) {
		try {
			return Utils.imageFromUrl(context.getContext(), url[0]);
		} catch (CampfireException e) {
			this.exception = e;
			return null;
		}
	}
	
	@Override
	public void onPostExecute(BitmapDrawable image) {
		context.onLoadImage(image, tag);
	}
	
	public interface LoadsImage {
		public void onLoadImage(BitmapDrawable image, Object tag);
		public Context getContext();
	}
}