package campyre.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import campyre.android.MessageAdapter.RoomContext;
import campyre.java.Campfire;
import campyre.java.CampfireException;
import campyre.java.Message;
import campyre.java.Room;
import campyre.java.User;

public class TranscriptView extends ListActivity implements RoomContext, LoadImageTask.LoadsImage {
	private Campfire campfire;
	private Room room;
	private ArrayList<Message> messages;
	
	private LoadTranscriptTask loadTranscriptTask;
	private HashMap<String,LoadImageTask> loadImageTasks = new HashMap<String,LoadImageTask>();
	private HashMap<String,BitmapDrawable> cachedImages = new HashMap<String,BitmapDrawable>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transcript);
		
		Bundle extras = getIntent().getExtras();
		
		campfire = Utils.getCampfire(this);
		room = new Room(campfire, extras.getString("room_id"));
		
		TranscriptViewHolder holder = (TranscriptViewHolder) getLastNonConfigurationInstance();
		if (holder != null) {
			messages = holder.messages;
			loadTranscriptTask = holder.loadTranscriptTask;
			cachedImages = holder.cachedImages;
			loadImageTasks = holder.loadImageTasks;
		}
		
		loadTranscripts();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return new TranscriptViewHolder(messages, loadTranscriptTask, loadImageTasks, cachedImages);
	}
	
	public void loadTranscripts() {
		if (messages != null) 
			displayTranscript();
		else {
			if (loadTranscriptTask == null)
				loadTranscriptTask = (LoadTranscriptTask) new LoadTranscriptTask(this).execute();
			else
				loadTranscriptTask.onScreenLoad(this);
		}
	}
	
	public void onLoadTranscripts(ArrayList<Message> messages) {
		this.messages = messages;
		displayTranscript();
	}
	
	public void onLoadTranscripts(CampfireException exception) {
		Utils.alert(this, exception);
	}
	
	public void displayTranscript() {
		setListAdapter(new MessageAdapter(this, messages));
	}
	
	@Override
    public Campfire getCampfire() {
    	return campfire;
    }
    
    @Override
    public Room getRoom() {
    	return room;
    }
    
    @Override
    public Context getContext() {
    	return this;
    }
    
    @Override
    public void loadImage(String url, String messageId) {
    	if (!loadImageTasks.containsKey(messageId)) {
			try {
				loadImageTasks.put(messageId, (LoadImageTask) new LoadImageTask(this, messageId).execute(url));
			} catch (RejectedExecutionException e) {
				onLoadImage(null, messageId); // if we can't run it, then just show the text and close up shop
			}
		}
    }
    
    @Override
    public void onLoadImage(BitmapDrawable image, Object tag) {
    	String messageId = (String) tag;
    	loadImageTasks.remove(messageId); // harmless if it doesn't exist
    	cachedImages.put(messageId, image);
		
		MessageAdapter.ViewHolder holder = new MessageAdapter.ViewHolder();
		holder.messageId = messageId;

		View result = getListView().findViewWithTag(holder);
		if (result != null) {
			// replace with actual holder
			holder = (MessageAdapter.ViewHolder) result.getTag();
			if (image != null)
				holder.showImage(image);
			else
				holder.imageFailed();
		}
    }
    
    @Override
    public BitmapDrawable cachedImage(String messageId) {
    	return cachedImages.get(messageId);
    }
	
	private class LoadTranscriptTask extends AsyncTask<Void,Void,ArrayList<Message>> {
		public TranscriptView context;
    	public CampfireException exception = null;
    	HashMap<String,User> users;
    	
    	public LoadTranscriptTask(TranscriptView context) {
    		super();
    		this.context = context;
    		this.users = new HashMap<String,User>();
    	}
    	
    	protected void onScreenLoad(TranscriptView context) {
       		this.context = context;
       	}
    	
    	@Override
    	protected ArrayList<Message> doInBackground(Void... nothing) {
    		
    		try {
				ArrayList<Message> messages = Message.allToday(context.room);
				
				int length = messages.size();
				for (int i=0; i<length; i++) {
					Message message = messages.get(i);
					if (message.user_id != null)
						fillPerson(message);
				}
				return messages;
    		} catch (CampfireException e) {
    			this.exception = e;
    			return null;
    		}
    	}
    	
    	@Override
    	protected void onPostExecute(ArrayList<Message> messages) {
    		context.loadTranscriptTask = null;
    		
    		if (exception == null)
    			context.onLoadTranscripts(messages);
    		else
    			context.onLoadTranscripts(exception);
    	}
    	
    	private void fillPerson(Message message) throws CampfireException {
    		User speaker;
			if (users.containsKey(message.user_id))
				speaker = (User) users.get(message.user_id);
			else {
				speaker = User.find(context.campfire, message.user_id);
				users.put(message.user_id, speaker);
			}
			message.person = speaker.displayName();
    	}
	}
	
	static class TranscriptViewHolder {
		ArrayList<Message> messages;
		LoadTranscriptTask loadTranscriptTask;
		HashMap<String,LoadImageTask> loadImageTasks;
		HashMap<String,BitmapDrawable> cachedImages;
		
		public TranscriptViewHolder(ArrayList<Message> messages, LoadTranscriptTask loadTranscriptTask, HashMap<String,LoadImageTask> loadImageTasks, HashMap<String,BitmapDrawable> cachedImages) {
			this.messages = messages;
			this.loadTranscriptTask = loadTranscriptTask;
			this.loadImageTasks = loadImageTasks;
			this.cachedImages = cachedImages;
		}
	}
}