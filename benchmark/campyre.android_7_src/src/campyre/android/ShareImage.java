package campyre.android;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;

import campyre.java.Campfire;
import campyre.java.CampfireException;
import campyre.java.Room;

public class ShareImage extends Activity {
	private static final int RESULT_ROOM_ID = 0;
	
	private Campfire campfire;
	private Room room;
	private UploadTask uploadTask;
	private ProgressDialog dialog = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		ShareImageHolder holder = (ShareImageHolder) getLastNonConfigurationInstance();
        if (holder != null) {
        	campfire = holder.campfire;
	    	room = holder.room;
	    	uploadTask = holder.uploadTask;
	    	if (uploadTask != null) {
	    		uploadTask.context = this;
	    		loadingDialog();
	    	}
        }
		
		verifyLogin();
	}
	
	public void onLogin() {
		if (room == null)
			startActivityForResult(new Intent(this, RoomList.class).putExtra("for_result", true), RESULT_ROOM_ID);
	}
	
	public void onLoadRoom() {
		if (uploadTask == null)
			new UploadTask(this).execute();
	}
	
	public void onUpload(CampfireException exception) {
		if (exception == null)
			Utils.alert(this, "Uploaded image to Campfire.");
		else
			Utils.alert(this, exception);
		finish();
	}
	
	public void uploadImage() throws FileNotFoundException, CampfireException {
		Uri uri = (Uri) getIntent().getExtras().get("android.intent.extra.STREAM");
		
		InputStream stream = getContentResolver().openInputStream(uri);
		String mimeType = getContentResolver().getType(uri);
		if (mimeType == null)
			throw new CampfireException("Couldn't figure out what kind of data you're sharing.");
		String filename = filenameFor(mimeType);
		
		room.uploadImage(stream, filename, mimeType);
	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
    	ShareImageHolder holder = new ShareImageHolder();
    	holder.campfire = this.campfire;
    	holder.room = this.room;
    	holder.uploadTask = this.uploadTask;
    	return holder;
    }
	
	public void verifyLogin() {
    	campfire = Utils.getCampfire(this);
        if (campfire != null)
        	onLogin();
        else
        	startActivityForResult(new Intent(this, Login.class), Login.RESULT_LOGIN);
    }
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case Login.RESULT_LOGIN:
    		if (resultCode == RESULT_OK) {
    			Utils.alert(this, "You have been logged in successfully.");
    			campfire = Utils.getCampfire(this);
    			onLogin();
    		} else
    			finish();
    		break;
    	case RESULT_ROOM_ID:
			if (resultCode == RESULT_OK) {
				room = new Room(campfire, data.getExtras().getString("room_id"));
				onLoadRoom();
			} else
				finish();
			break;
    	}
    }
	
	public void loadingDialog() {
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);
        dialog.show();
    }
	
	public static String filenameFor(String mimeType) {
		// default to whatever was in the 2nd half of the mime type
		if (mimeType.equals("image/jpeg"))
			return "from_phone.jpg";
		else
			return "from_phone." + mimeType.split("/")[1];
	}
	
	private class UploadTask extends AsyncTask<Void,Void,CampfireException> {
		public ShareImage context;
    	
    	public UploadTask(ShareImage context) {
    		super();
    		this.context = context;
    		this.context.uploadTask = this;
    	}
    	 
       	@Override
    	protected void onPreExecute() {
            context.loadingDialog();
    	}
    	
    	@Override
    	protected CampfireException doInBackground(Void... nothing) {
    		try {
				context.uploadImage();
			} catch (FileNotFoundException e) {
				return new CampfireException(e, "Couldn't get a handle on the image you selected.");
			} catch (CampfireException e) {
				return e;
			}
			return null;
    	}
    	
    	@Override
    	protected void onPostExecute(CampfireException exception) {
    		if (context.dialog != null && context.dialog.isShowing())
    			context.dialog.dismiss();
    		context.uploadTask = null;
    		
    		context.onUpload(exception);
    	}
	}
	
	static class ShareImageHolder {
		Campfire campfire;
		Room room;
		UploadTask uploadTask;
	}
}