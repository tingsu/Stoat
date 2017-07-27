package campyre.android;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import campyre.java.Room;

public class RoomTabs extends TabActivity {
	public String roomId, roomName;
	public Room room;
	
	public String shareText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room);
		
		Bundle extras = getIntent().getExtras();
		room = (Room) extras.getSerializable("room");
		if (room != null) {
			roomId = room.id;
			roomName = room.name;
		} else {
			roomId = extras.getString("room_id");
			roomName = extras.getString("room_name");
		}
		shareText = extras.getString("shareText");
		
		setupTabs();
	}
	
	public void setupTabs() {
		TabHost tabHost = getTabHost();
		
		tabHost.addTab(newTab("room", Utils.truncate(roomName, 35), roomIntent()));
		tabHost.addTab(newTab("transcript", R.string.tab_transcript, transcriptIntent()));
		
		tabHost.setCurrentTab(0);
	}
	
	public Intent roomIntent() {
		return new Intent(this, RoomView.class)
			.putExtra("room_id", roomId)
			.putExtra("room", room) // may be null
			.putExtra("shareText", shareText);
	}
	
	public Intent transcriptIntent() {
		return new Intent(this, TranscriptView.class).putExtra("room_id", roomId);
	}
	
	public TabSpec newTab(String id, int title, Intent intent) {
		return newTab(id, getResources().getString(title), intent);
	}
	
	public TabSpec newTab(String id, String title, Intent intent) {
		View tabView = LayoutInflater.from(this).inflate(R.layout.tab, null);
		((TextView) tabView.findViewById(R.id.tab_name)).setText(title);
		return getTabHost().newTabSpec(id).setIndicator(tabView).setContent(intent);
	}
	
}
