package campyre.java;

import java.io.Serializable;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;


public class Campfire implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// Change this to use your own user agent
	public static final String USER_AGENT = "android-campfire (http://github.com/klondike/android-campfire)";
	
	public String subdomain, token;
	public String username, password;
	public String user_id = null;
		
	public Campfire(String subdomain) {
		this.subdomain = subdomain;
	}
	
	public Campfire(String subdomain, String token, String user_id) {
		this.subdomain = subdomain;
		this.token = token;
		this.user_id = user_id;
	}
	
	public void login() throws CampfireException {
		HttpResponse response = new CampfireRequest(this).get(mePath());
		int statusCode = response.getStatusLine().getStatusCode();
		// if API key is wrong, we'll get a 401 status code (HttpStatus.SC_UNAUTHORIZED)
		// if it gets a 200, then save the info from the response
		switch (statusCode) {
		case HttpStatus.SC_OK:
			try {
				JSONObject user = new JSONObject(CampfireRequest.responseBody(response)).getJSONObject("user");
				this.user_id = user.getString("id");
				this.token = user.getString("api_auth_token");
			} catch (JSONException e) {
				throw new CampfireException(e, "Couldn't load user details on login.");
			}
			break;
		case HttpStatus.SC_UNAUTHORIZED:
			throw new CampfireException("Invalid credentials.");
		case HttpStatus.SC_NOT_FOUND:
			throw new CampfireException("Incorrect Campfire URL; correct the subdomain.");
		default:
			throw new CampfireException("Unknown error code " + statusCode + " on login.");
		}
	}
	
	public static String mePath() {
		return "/users/me";
	}
	
	public static String roomPath(String room_id) {
		return "/room/" + room_id;
	}
	
	public static String roomsPath() {
		return "/rooms";
	}
	
	public static String userPath(String user_id) {
		return "/users/" + user_id;
	}
	
	public static String joinPath(String room_id) {
		return roomPath(room_id) + "/join";
	}
	
	public static String speakPath(String room_id) {
		return roomPath(room_id) + "/speak";
	}
	
	public static String leavePath(String room_id) {
		return roomPath(room_id) + "/leave";
	}
	
	public static String uploadPath(String room_id) {
		return roomPath(room_id) + "/uploads";
	}
	
}