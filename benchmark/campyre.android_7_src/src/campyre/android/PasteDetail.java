package campyre.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class PasteDetail extends Activity {
	private static String TIMESTAMP_FORMAT = "MMM d, h:mm a";
	
	String roomName;
	String person, paste;
	Date timestamp;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.paste);
		
		Bundle extras = getIntent().getExtras();
		roomName = extras.getString("room_name");
		person = extras.getString("person");
		paste = extras.getString("paste");
		timestamp = (Date) extras.getSerializable("timestamp");
		
		setupControls();
	}
	
	public void setupControls() {
		setTitle(roomName);
		
		String formatted = new SimpleDateFormat(TIMESTAMP_FORMAT).format(timestamp);
		
		((TextView) findViewById(R.id.person)).setText(person + ", at " + formatted + ":");
		((TextView) findViewById(R.id.paste)).setText(paste);
	}
}