package campyre.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import campyre.android.LoadImageTask.LoadsImage;
import campyre.android.MessageAdapter.RoomContext;
import campyre.java.Campfire;
import campyre.java.CampfireException;
import campyre.java.Message;
import campyre.java.Room;
import campyre.java.User;

public class RoomView extends ListActivity implements RoomContext, LoadsImage {
	private static final int MENU_SETTINGS = 0;
	private static final int MENU_SHORTCUT = 1;
	private static final int MENU_LEAVE = 2;
	
	private static final int AUTOPOLL_INTERVAL = 5; // in seconds
	private static final long JOIN_TIMEOUT = 60; // in seconds
	
	private Campfire campfire;
	private String roomId;
	private Room room;
	
	private HashMap<String,SpeakTask> speakTasks = new HashMap<String,SpeakTask>();
	private LoadRoomTask loadRoomTask;
	private LeaveRoomTask leaveRoomTask;
	private PollTask pollTask;
	
	private int transitId = 1;
	private long lastJoined = 0;
	private String lastMessageId = null;
	
	private String shareText = null;
	private boolean shared = false;
	
	private ArrayList<Message> messages = new ArrayList<Message>();
	private HashMap<String,Message> transitMessages = new HashMap<String,Message>();
	private Message errorMessage;
	
	private HashMap<String,LoadImageTask> loadImageTasks = new HashMap<String,LoadImageTask>();
	private HashMap<String,BitmapDrawable> cachedImages = new HashMap<String,BitmapDrawable>();
	
	private HashMap<String,User> users = new HashMap<String,User>();
	
	private EditText body;
	private Button speak;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room_view);
		
		Message.loadImages = Utils.getBooleanPreference(this, Settings.LOAD_IMAGES_KEY, Settings.LOAD_IMAGES_DEFAULT);
		
		Bundle extras = getIntent().getExtras();
		roomId = extras.getString("room_id"); // will always be set
		room = (Room) extras.getSerializable("room"); // may be null
		shareText = extras.getString("shareText");
		
		setupControls();
		
		if (savedInstanceState != null) {
			transitId = savedInstanceState.getInt("transitId");
			lastJoined = savedInstanceState.getLong("lastJoined");
		}
		
		RoomViewHolder holder = (RoomViewHolder) getLastNonConfigurationInstance();
		if (holder != null) {
			campfire = holder.campfire;
			room = holder.room;
			messages = holder.messages;
			transitMessages = holder.transitMessages;
			errorMessage = holder.errorMessage;
			users = holder.users;
			speakTasks = holder.speakTasks;
			loadImageTasks = holder.loadImageTasks;
			loadRoomTask = holder.loadRoomTask;
			leaveRoomTask = holder.leaveRoomTask;
			pollTask = holder.pollTask;
			cachedImages = holder.cachedImages;
			shared = holder.shared;
		}
		
		if (speakTasks != null) {
			Iterator<SpeakTask> iterator = speakTasks.values().iterator();
			while (iterator.hasNext())
				iterator.next().onScreenLoad(this);
		}
		
		if (loadImageTasks != null) {
			Iterator<LoadImageTask> iterator = loadImageTasks.values().iterator();
			while (iterator.hasNext())
				iterator.next().onScreenLoad(this);
		}
		
		if (pollTask != null)
			pollTask.onScreenLoad(this);
		
		if (loadRoomTask != null)
			loadRoomTask.onScreenLoad(this);
		
		if (leaveRoomTask != null)
			leaveRoomTask.onScreenLoad(this);
		
		verifyLogin();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		RoomViewHolder holder = new RoomViewHolder();
		holder.campfire = this.campfire;
		holder.room = this.room;
		holder.messages = this.messages;
		holder.transitMessages = this.transitMessages;
		holder.errorMessage = this.errorMessage;
		holder.users = this.users;
		holder.speakTasks = this.speakTasks;
		holder.loadImageTasks = this.loadImageTasks;
		holder.loadRoomTask = this.loadRoomTask;
		holder.pollTask = this.pollTask;
		holder.shared = this.shared;
		holder.cachedImages = this.cachedImages;
		holder.leaveRoomTask = this.leaveRoomTask;
		return holder;
	}
	
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("transitId", transitId);
		outState.putLong("lastJoined", lastJoined);
		super.onSaveInstanceState(outState);
	}
	
	private void onLogin() {
		loadRoom();
	}
	
	private void onRoomLoaded() {
		updateMessages();
		
		body.setFocusableInTouchMode(true);
		body.setEnabled(true);
		speak.setEnabled(true);
		((TextView) findViewById(R.id.empty_message)).setText(R.string.loading_messages);
		
		startPoll();
	}
	
	private void onRoomLoaded(CampfireException exception) {
		Utils.alert(this, exception);
		finish();
	}
	
	private int preferredMaxMessages() {	
	 	return Utils.getIntPreferenceFromString(this, Settings.NUMBER_MESSAGES_KEY, Settings.NUMBER_MESSAGES_DEFAULT);	
	}
	
	private void onPoll(ArrayList<Message> messages) {
		int max = preferredMaxMessages();
		
		// filter out some messages according to user preferences
		for (int i=0; i<messages.size(); i++) {
			Message message = messages.get(i);
			if (messageAllowed(message.type))
				this.messages.add(message);
		}
		
		if (this.messages.size() > max) {
		 	List<Message> withinMax = this.messages.subList(this.messages.size() - max, this.messages.size());	
		 	this.messages = new ArrayList<Message>();
		 	this.messages.addAll(withinMax);
		}
		
		errorMessage = null;
		
		// one-way, since no other "Loading..." messages will be shown after this.
		if (messages.size() == 0) {
			findViewById(R.id.empty_spinner).setVisibility(View.GONE);
			((TextView) findViewById(R.id.empty_message)).setText(R.string.no_messages);
		} else {
			String newLastMessageId = messages.get(messages.size() - 1).id;
			if (!newLastMessageId.equals(lastMessageId)) {
				lastMessageId = newLastMessageId;
				updateMessages();
			}
		}
	}
	
	// polling failed, messages still has the old list
	private void onPoll(CampfireException exception) {
		errorMessage = new Message("error", Message.ERROR, exception.getMessage());
		updateMessages();
	}
	
	private void onSpeak(Message message, String transitId) {
		transitMessages.remove(transitId);
		messages.add(message);
		updateMessages();
	}
	
	private void onSpeak(CampfireException exception, String transitId) {
		transitMessages.remove(transitId);
		updateMessages();
		Utils.alert(this, exception);
	}
	
	private void updateMessages() {
		ArrayList<Message> allMessages = new ArrayList<Message>();
		allMessages.addAll(messages);
		allMessages.addAll(transitMessages.values());
		if (errorMessage != null)
			allMessages.add(errorMessage);
		
		// refresh screen and try to control scrolling intelligently
		boolean wasAtBottom = scrolledToBottom();
		int position = scrollPosition();
		
		setListAdapter(new MessageAdapter(this, allMessages));
		
		if (wasAtBottom)
			scrollToBottom();
		else
			scrollToPosition(position);
	}
	
	private boolean messageAllowed(int type) {
		switch(type) {
		case Message.ENTRY:
		case Message.LEAVE:
			return Utils.getBooleanPreference(this, Settings.ENTRY_EXIT_KEY, Settings.ENTRY_EXIT_DEFAULT);
		case Message.TIMESTAMP:
			return Utils.getBooleanPreference(this, Settings.TIMESTAMPS_KEY, Settings.TIMESTAMPS_DEFAULT);
		default:
			return true;
		}
	}
	
	private void setupControls() {
		body = (EditText) findViewById(R.id.room_message_body);
		body.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE)
					speak();
				return false;
			}
		});
		
		body.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					speak();
					return true;
				}
				return false;
			}
		});
		
		if (shareText != null && !shared) {
			body.setText(shareText);
			shared = true;
		}
		
		speak = (Button) this.findViewById(R.id.room_speak);
		speak.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				speak();
			}
		});
	}
	
	private boolean scrolledToBottom() {
		if (getListAdapter() == null)
			return true;
		return (getListView().getLastVisiblePosition() == (getListAdapter().getCount()-1));
	}
	
	private void scrollToBottom() {
		getListView().setSelection(getListAdapter().getCount()-1);
	}
	
	private int scrollPosition() {
		return getListView().getFirstVisiblePosition();
	}
	
	private void scrollToPosition(int position) {
		getListView().setSelection(position);
	}
	
	private void speak() {
		String msg = body.getText().toString().trim();
		
		if (!msg.equals("")) {
			body.setText("");
			
			String id = transitId + "-" + campfire.user_id;
			transitId += 1;
			Message message = new Message(id, Message.TRANSIT, msg);
			transitMessages.put(id, message);
			
			// avoid refreshing the whole adapter if I don't have to
			((MessageAdapter) getListAdapter()).add(message);
			scrollToBottom();
			
			// actually do the speaking in the background
			new SpeakTask(this, message).execute();
		}
	}

	private void loadRoom() {
		if (room != null)
			onRoomLoaded();
		else {
			if (loadRoomTask == null)
				new LoadRoomTask(this).execute();
		}
	}
	
	private void startPoll() {
		if (pollTask == null)
			pollTask = (PollTask) new PollTask(this).execute();
	}
	
	// Fetches latest MAX_MESSAGES from the transcript, then for each message,
	// looks up the associated User to assign a display name.
	// We use the "users" HashMap to cache Users from the network. 
	private ArrayList<Message> poll(Room room, HashMap<String,User> users) throws CampfireException {
		int maxMessages = preferredMaxMessages();

		ArrayList<Message> messages = Message.recent(room, maxMessages, lastMessageId);
		int length = messages.size();
		for (int i=0; i<length; i++) {
			Message message = messages.get(i);
			if (message.user_id != null)
				fillPerson(message, users);
		}
		return messages;
	}
	
	private void fillPerson(Message message, HashMap<String,User> users) throws CampfireException {
		User speaker;
		if (users.containsKey(message.user_id))
			speaker = (User) users.get(message.user_id);
		else {
			speaker = User.find(campfire, message.user_id);
			users.put(message.user_id, speaker);
		}
		message.person = speaker.displayName();
	}
	
	private void verifyLogin() {
		if (campfire != null) 
			onLogin();
		else {
			campfire = Utils.getCampfire(this);
	        if (campfire != null)
	        	onLogin();
	        else
	        	startActivityForResult(new Intent(this, Login.class), Login.RESULT_LOGIN);
		} 
    }
	
	private void leaveRoom() {
		if (leaveRoomTask == null)
			new LeaveRoomTask(this).execute();
	}
	
	private void onLeaveRoom() {
		finish();
	}
	
	private void onLeaveRoom(CampfireException e) {
		Utils.alert(this, e);
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
    	}
    }
	
	@Override 
    public boolean onCreateOptionsMenu(Menu menu) { 
	    boolean result = super.onCreateOptionsMenu(menu);
	    
	    menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
	    	.setIcon(android.R.drawable.ic_menu_preferences);
	    menu.add(1, MENU_SHORTCUT, 1, R.string.menu_shortcut)
	    	.setIcon(android.R.drawable.ic_menu_add);
        menu.add(2, MENU_LEAVE, 2, R.string.menu_leave)
        	.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        
        return result;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case MENU_SETTINGS:
    		startActivity(new Intent(this, Settings.class));
    		break;
    	case MENU_SHORTCUT:
    		Utils.installShortcut(this, room);
    		break;
    	case MENU_LEAVE:
    		leaveRoom();
    		break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    private boolean shouldJoin() {
    	return (System.currentTimeMillis() - lastJoined) > (JOIN_TIMEOUT * 1000);
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
    public BitmapDrawable cachedImage(String messageId) {
    	return cachedImages.get(messageId);
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
    
    private class PollTask extends AsyncTask<Void,ArrayList<Message>,Integer> {
    	public RoomView context;
    	public CampfireException exception = null;
    	private int pollFailures = 0;
    	
    	public PollTask(RoomView context) {
    		super();
    		this.context = context;
    	}
    	
    	protected void onScreenLoad(RoomView context) {
       		this.context = context;
       	}
    	
    	@Override
    	protected Integer doInBackground(Void... nothing) {
    		new Thread() {
    			
    			@SuppressWarnings("unchecked") // for the autocasting to publishProgress
				public void run() {
		    		while(true) {
						try {
							publishProgress(context.poll(context.room, context.users));
							
							// ping the room so we don't get idle-kicked out
							if (context.shouldJoin()) {
								context.room.join();
								context.lastJoined = System.currentTimeMillis();
							}
						} catch(CampfireException e) {
							exception = e;
							publishProgress((ArrayList<Message>) null);
						}
						
						try {
							sleep(AUTOPOLL_INTERVAL * 1000);
						} catch(InterruptedException ex) {
							// well, I never
						}
					}
    			}
    		}.start();
    		return -1; // Integer instead of Void, to avoid compiler errors in Eclipse
    	}
    	
    	@Override
    	public void onProgressUpdate(ArrayList<Message>... messages) {
    		if (exception == null) {
    			pollFailures = 0;
    			context.onPoll(messages[0]);
    		} else {
    			pollFailures += 1;
    			context.onPoll(new CampfireException(exception, "Connection error while trying to poll. (Try #" + pollFailures + ")"));
    		}
    	}
	};
	
	private class SpeakTask extends AsyncTask<Void,Void,Message> {
		public RoomView context;
    	public CampfireException exception = null;
    	private Message transitMessage;
    	
    	public SpeakTask(RoomView context, Message transitMessage) {
    		super();
    		this.context = context;
    		this.context.speakTasks.put(transitMessage.id, this);
    		this.transitMessage = transitMessage;
    	}
       	
       	protected void onScreenLoad(RoomView context) {
       		this.context = context;
       	}
       	
    	@Override
    	protected Message doInBackground(Void... nothing) {
    		try {
    			// in case we've been idle-kicked out since we last spoke
    			if (context.shouldJoin()) { 
    				context.room.join();
    				context.lastJoined = System.currentTimeMillis();
    			}
    			
    			Message newMessage = context.room.speak(transitMessage.body);
    			context.fillPerson(newMessage, context.users);
    			return newMessage;
			} catch (CampfireException e) {
				this.exception = e;
				return null;
			}
    	}
    	
    	@Override
    	protected void onPostExecute(Message newMessage) {
    		context.speakTasks.remove(transitMessage.id);
    		
    		if (exception == null)
    			context.onSpeak(newMessage, transitMessage.id);
    		else
    			context.onSpeak(exception, transitMessage.id);
    	}
	}
	
	private class LoadRoomTask extends AsyncTask<Void,String,CampfireException> {
		public RoomView context;
    	
    	public Room room = null;
    	public HashMap<String,User> users;
    	
    	public LoadRoomTask(RoomView context) {
    		super();
    		this.context = context;
    		this.context.loadRoomTask = this;
    		
    		// get the current state of the user cache, so that we can write to it as we poll
    		// and then assign it back to the new context
    		// preserves caching in the case of a screen flip during this task
    		users = this.context.users;
    	}
    	
    	public void onScreenLoad(RoomView context) {
	    	this.context = context;
    	}
    	 
       	@Override
    	protected CampfireException doInBackground(Void... nothing) {
    		try {
    			room = Room.find(context.campfire, context.roomId);
    			
    			// cache the initial users now while we can
    			if (room.initialUsers != null) {
    				int length = room.initialUsers.size();
    				for (int i=0; i<length; i++) {
    					User user = room.initialUsers.get(i);
    					users.put(user.id, user);
    				}
    			}
			} catch (CampfireException e) {
				return e;
			}
			return null;
    	}
    	
    	@Override
    	protected void onPostExecute(CampfireException exception) {
    		context.loadRoomTask = null;
    		
    		context.room = room;
    		context.users = users;
    		   		
    		if (exception == null)
    			context.onRoomLoaded();
    		else
    			context.onRoomLoaded(exception);
    	}
	}
	
	private class LeaveRoomTask extends AsyncTask<Void,Void,Boolean> {
		public RoomView context;
    	public CampfireException exception = null;
    	private ProgressDialog dialog = null;
    	
    	public LeaveRoomTask(RoomView context) {
    		super();
    		this.context = context;
    		this.context.leaveRoomTask = this;
    	}
    	
    	@Override
    	protected void onPreExecute() {
            loadingDialog();
    	}
    	
       	protected void onScreenLoad(RoomView context) {
       		this.context = context;
       		loadingDialog();
       	}
       	
       	protected void loadingDialog() {
       		dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(context.getResources().getString(R.string.leaving_room));
            
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
    			@Override
    			public void onCancel(DialogInterface dialog) {
    				cancel(true);
    			}
    		});
            
            dialog.show();
       	}
       	
    	@Override
    	protected Boolean doInBackground(Void... nothing) {
    		try {
    			if (room != null) {
    				room.leave();
    				return Boolean.TRUE;
    			} else
    				return Boolean.FALSE;
			} catch (CampfireException e) {
				this.exception = e;
				return null;
			}
    	}
    	
    	@Override
    	protected void onPostExecute(Boolean value) {
    		if (dialog != null && dialog.isShowing())
    			dialog.dismiss();
    		 
    		context.leaveRoomTask = null;
    		
    		if (value == Boolean.TRUE && exception == null)
    			context.onLeaveRoom();
    		else
    			context.onLeaveRoom(exception);
    	}
	}
	
	static class RoomViewHolder {
		Campfire campfire;
		Room room;
		ArrayList<Message> messages;
		HashMap<String,Message> transitMessages;
		Message errorMessage;
		HashMap<String,User> users;
		HashMap<String,SpeakTask> speakTasks;
		HashMap<String,LoadImageTask> loadImageTasks;
		LoadRoomTask loadRoomTask;
		PollTask pollTask;
		LeaveRoomTask leaveRoomTask;
		HashMap<String,BitmapDrawable> cachedImages;
		boolean shared;
	}
}
