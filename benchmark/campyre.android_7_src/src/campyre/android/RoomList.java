package campyre.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import campyre.java.Campfire;
import campyre.java.CampfireException;
import campyre.java.Room;

public class RoomList extends ListActivity { 
	private static final int MENU_SETTINGS = 0;
	private static final int MENU_CLEAR = 1;
	private static final int MENU_ABOUT = 2;
	private static final int MENU_FEEDBACK = 3;
	
	private Campfire campfire = null;
	private ArrayList<Room> rooms = null;
	
	private LoadRoomsTask loadRoomsTask = null;
	
	private boolean forResult = false;
	private String shareText = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_titled);
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
	        forResult = extras.getBoolean("for_result", false);
	        shareText = extras.getString(android.content.Intent.EXTRA_TEXT);
        }
        
        setupControls();
        
        RoomListHolder holder = (RoomListHolder) getLastNonConfigurationInstance();
        if (holder != null) {
	    	rooms = holder.rooms;
	    	loadRoomsTask = holder.loadRoomsTask;
	    	if (loadRoomsTask != null)
	    		loadRoomsTask.onScreenLoad(this);
        }
        
        verifyLogin();
    }
    
    public void verifyLogin() {
    	campfire = Utils.getCampfire(this);
        if (campfire != null)
        	onLogin();
        else
	        startActivityForResult(new Intent(this, Login.class), Login.RESULT_LOGIN);
    }
    
    public void onLogin() {
    	loadRooms();
    }
    
    public void loadRooms() {
    	if (loadRoomsTask == null) {
	    	if (rooms == null)
	    		new LoadRoomsTask(this).execute();
	    	else
	    		displayRooms();
    	}
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	RoomListHolder holder = new RoomListHolder();
    	holder.rooms = this.rooms;
    	holder.loadRoomsTask = this.loadRoomsTask;
    	return holder;
    }
    
    public void onLoadRooms(ArrayList<Room> rooms, CampfireException exception) {
    	if (exception == null && rooms != null) {
    		this.rooms = rooms;
    		displayRooms();
    	} else {
    		this.rooms = new ArrayList<Room>();
    		displayRooms(exception);
		}
    }
    
    public void selectRoom(Room room) {
    	if (forResult) { // for file uploading
        	setResult(RESULT_OK, new Intent().putExtra("room_id", room.id));
        	finish();
    	} else {
    		startActivity(new Intent(this, RoomTabs.class)
    			.putExtra("room", room)
    			.putExtra("shareText", shareText));
    	}
    }
    
    public void displayRooms() {
    	if (rooms.size() <= 0)
    		Utils.showEmpty(this, R.string.no_rooms);
    	else
    		setListAdapter(new RoomAdapter(this, rooms));
    }
    
    public void displayRooms(CampfireException exception) {
    	Utils.showRefresh(this, R.string.rooms_error);
    }
    
    public void setupControls() {
    	Utils.setTitle(this, R.string.room_list_title);
    	
    	Utils.setLoading(this, R.string.loading_rooms);
    	
		((Button) findViewById(R.id.refresh)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				rooms = null;
				Utils.showLoading(RoomList.this);
				loadRooms();
			}
		});
    }
    
    public void onListItemClick(ListView parent, View v, int position, long id) {
    	selectRoom((Room) parent.getItemAtPosition(position));
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
	    
	    menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(1, MENU_CLEAR, 1, R.string.menu_logout).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        menu.add(2, MENU_FEEDBACK, 3, R.string.menu_feedback).setIcon(R.drawable.ic_menu_send);
        menu.add(3, MENU_ABOUT, 4, R.string.menu_about).setIcon(android.R.drawable.ic_menu_help);
        
        return result;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) { 
    	case MENU_CLEAR:
    		Utils.logoutCampfire(this);
    		finish();
    		break;
    	case MENU_FEEDBACK:
    		startActivity(Utils.feedbackIntent(this));
    		break;
    	case MENU_SETTINGS:
    		startActivity(new Intent(this, Settings.class));
    		break;
    	case MENU_ABOUT:
    		showDialog(Utils.ABOUT);
    		break;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
	protected Dialog onCreateDialog(int id) { 
		return id == Utils.ABOUT ? Utils.aboutDialog(this) : null;
	}
    
    private class RoomAdapter extends ArrayAdapter<Room> {
    	LayoutInflater inflater;

        public RoomAdapter(Activity context, ArrayList<Room> items) {
            super(context, 0, items);
            inflater = LayoutInflater.from(context);
        }

		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout view;
			
			if (convertView == null)
				view = (LinearLayout) inflater.inflate(R.layout.room_item, null);
			else
				view = (LinearLayout) convertView;
				
			Room room = getItem(position);
			((TextView) view.findViewById(R.id.name)).setText(room.name);
			if ( room.topic != null ) {
				((TextView) view.findViewById(R.id.topic)).setText(room.topic);
			} else {
				((TextView) view.findViewById(R.id.topic)).setText(R.string.room_has_no_topic);
			}
			return view;
		}
    }
    
    private class LoadRoomsTask extends AsyncTask<Void,Void,ArrayList<Room>> {
    	public RoomList context;
    	public CampfireException exception = null;
    	
    	public LoadRoomsTask(RoomList context) {
    		super();
    		this.context = context;
    		this.context.loadRoomsTask = this;
    	}
       	
       	protected void onScreenLoad(RoomList context) {
       		this.context = context;
       	}
       	
       	@Override
    	protected ArrayList<Room> doInBackground(Void... nothing) {
    		try {
				return Room.all(context.campfire);
			} catch (CampfireException e) {
				this.exception = e;
				return null;
			}
    	}
    	
    	@Override
    	protected void onPostExecute(ArrayList<Room> rooms) {
    		context.loadRoomsTask = null;
    		
    		context.onLoadRooms(rooms, exception);
    	}
    }
    
    static class RoomListHolder {
    	ArrayList<Room> rooms;
    	LoadRoomsTask loadRoomsTask;
    	boolean error;
    }
    
}