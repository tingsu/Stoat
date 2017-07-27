package campyre.java;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Message {
	// special message types
	public static final int UNSUPPORTED = -3;
	public static final int ERROR = -2; // can be used by clients to make messages that didn't come from the Campfire
	public static final int TRANSIT = -1; // can be used by clients to make messages that are still in transit
	
	// Campfire message types
	// The Android client depends on these beginning at 0, and going to SUPPORTED_MESSAGE_TYPES - 1.
	public static final int TEXT = 0;
	public static final int IMAGE = 1;
	public static final int TIMESTAMP = 2;
	public static final int ENTRY = 3;
	public static final int LEAVE = 4;
	public static final int PASTE = 5;
	public static final int TOPIC = 6;
	
	// Here for the Android client: the number of supported message types (keep in sync with the constants above)
	public static final int SUPPORTED_MESSAGE_TYPES = 7;
	
	public static boolean loadImages;
	
	public int type;
	public String id, user_id, body;
	public Date timestamp;
	
	private String[] inFormat = new String[] {"yy/MM/dd HH:mm:ss Z"};
	
	// Here for the Android client, the display name to put on the Message object itself
	public String person;
	
	// for making artificial messages (really just intended to serve the Android client)
	// only make them if you know what you're doing (as they'll be missing fields!)
	public Message(String id, int type, String body) {
		this.id = id;
		this.type = type;
		this.body = body;
	}
	
	public Message(JSONObject json) throws JSONException, DateParseException {
		String body = denull(json.getString("body"));
		
		this.body = body;
		this.type = typeFor(json.getString("type"), body);
		
		this.id = json.getString("id");
		this.user_id = denull(json.getString("user_id"));
		this.timestamp = DateUtils.parseDate(json.getString("created_at"), inFormat);
		this.person = null;
	}
	
	public static ArrayList<Message> allToday(Room room) throws CampfireException {
		ArrayList<Message> messages = new ArrayList<Message>();
		
		try {
			JSONArray items = new CampfireRequest(room.campfire).getList(todayPath(room.id), "messages");
			int length = items.length();
			
			for (int i=0; i<length; i++) {
				Message message = new Message(items.getJSONObject(i));
				if (message.type != UNSUPPORTED)
					messages.add(message);
			}
			
		} catch (JSONException e) {
			throw new CampfireException(e, "Could not load messages from their JSON.");
		} catch (DateParseException e) {
			throw new CampfireException(e, "Could not parse date from a message's JSON.");
		}
		
		return messages; 
	}

	public static ArrayList<Message> recent(Room room, int max, String lastSeen) throws CampfireException {
		ArrayList<Message> messages = new ArrayList<Message>();
		
		try {
			HashMap<String,String> parameters = new HashMap<String,String>();
			parameters.put("limit", String.valueOf(max));
			if (lastSeen != null) 
				parameters.put("since_message_id", lastSeen);
			
			JSONArray items = new CampfireRequest(room.campfire).getList(recentPath(room.id), parameters, "messages");
			int length = items.length();
			for (int i=0; i<length; i++) {
				Message message = new Message(items.getJSONObject(i));
				if (message.type != UNSUPPORTED)
					messages.add(message);
			}
			
		} catch (JSONException e) {
			throw new CampfireException(e, "Could not load messages from their JSON.");
		} catch (DateParseException e) {
			throw new CampfireException(e, "Could not parse date from a message's JSON.");
		}
		
		return messages;
	}
	
	private static int typeFor(String type, String body) {
		if (type.equals("TextMessage")) {
			if (imageLink(body))
				return IMAGE;
			else
				return TEXT;
		} else if (type.equals("PasteMessage"))
			return PASTE;
		else if (type.equals("TimestampMessage"))
			return TIMESTAMP;
		else if (type.equals("EnterMessage"))
			return ENTRY;
		else if (type.equals("LeaveMessage") || type.equals("KickMessage"))
			return LEAVE;
		else if (type.equals("TopicChangeMessage"))
			return TOPIC;
		else
			return UNSUPPORTED;
	}

	public static String recentPath(String room_id) {
		return "/room/" + room_id + "/recent";
	}
	
	public static String todayPath(String room_id) {
		return "/room/" + room_id + "/transcript";
	}
	
	private String denull(String maybeNull) {
		if (maybeNull.equals("null"))
			return null;
		else
			return maybeNull;
	}
	
	// depends on the assumption that we'll only render image links that are the entirety of the body
	// if we ever expand this assumption, this will need to also extract the URL
	public static boolean imageLink(String body) {
		if (!loadImages) {
			return false;
		}
		Pattern pattern = Pattern.compile("^(http[^\\s]+(?:jpe?g|gif|png))(\\?[^\\s]*)?$");
		Matcher matcher = pattern.matcher(body);
		return matcher.matches();
	}
}