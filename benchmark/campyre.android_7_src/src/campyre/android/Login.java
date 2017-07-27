package campyre.android;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import campyre.java.Campfire;
import campyre.java.CampfireException;

public class Login extends Activity {
	// high number because other activities will use this code in their case statements
	public static final int RESULT_LOGIN = 1000;
	
	public static final int MENU_ABOUT = 1;
	public static final int MENU_FEEDBACK = 2;
	
	private Campfire campfire;
	private EditText subdomainView, usernameView, passwordView;
	
	private LoginTask loginTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		LoginHolder holder = (LoginHolder) getLastNonConfigurationInstance();
        if (holder != null) {
	    	campfire = holder.campfire;
	    	loginTask = holder.loginTask;
	    }
        
        setupControls();
        
        if (loginTask != null)
    		loginTask.onScreenLoad(this);
	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
    	LoginHolder holder = new LoginHolder();
    	holder.campfire = this.campfire;
    	holder.loginTask = this.loginTask;
    	return holder;
    }
	
	public void login() {
		if (loginTask == null)
        	new LoginTask(this).execute();
	}
	
	public void onLogin(CampfireException exception) {
		if (exception == null) {
			Utils.saveCampfire(this, campfire);
			setResult(RESULT_OK, new Intent());
			finish();
		} else 
			Utils.alert(this, exception);
    };
    
    public void setupControls() {
    	Utils.setTitle(this, R.string.app_name);
    	Utils.setTitleSize(this, 24);
    	
    	subdomainView = (EditText) findViewById(R.id.subdomain);
    	usernameView = (EditText) findViewById(R.id.username);
    	passwordView = (EditText) findViewById(R.id.password);
    	
        subdomainView.setText(Utils.getCampfireValue(this, "subdomain"));
        
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				login();
			}
		});
    }
    
    @Override 
    public boolean onCreateOptionsMenu(Menu menu) { 
	    boolean result = super.onCreateOptionsMenu(menu);
	    
	    menu.add(0, MENU_FEEDBACK, 1, R.string.menu_feedback).setIcon(R.drawable.ic_menu_send);
        menu.add(1, MENU_ABOUT, 2, R.string.menu_about).setIcon(android.R.drawable.ic_menu_help);
        
        return result;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) { 
    	case MENU_ABOUT:
    		showDialog(Utils.ABOUT);
    		break;
    	case MENU_FEEDBACK:
    		startActivity(Utils.feedbackIntent(this));
    		break;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
	protected Dialog onCreateDialog(int id) { 
		return id == Utils.ABOUT ? Utils.aboutDialog(this) : null;
	}
	
	private class LoginTask extends AsyncTask<Void,Void,CampfireException> {
		public Login context;
		private ProgressDialog dialog;
    	
    	public LoginTask(Login context) {
    		super();
    		this.context = context;
    		this.context.loginTask = this;
    	}
    	 
       	@Override
    	protected void onPreExecute() {
            loadingDialog();
    	}
       	
       	public void onScreenLoad(Login context) {
       		this.context = context;
       		loadingDialog();
       	}
    	
    	@Override
    	protected CampfireException doInBackground(Void... nothing) {
    		String subdomain = context.subdomainView.getText().toString().trim().replace(" ", "");
    		context.campfire = new Campfire(subdomain);
    		
			String username = context.usernameView.getText().toString().trim();
			String password = context.passwordView.getText().toString().trim();
			context.campfire.username = username;
			context.campfire.password = password;
    		
    		// save the subdomain and token right away
    		Utils.saveCampfire(context, context.campfire); 
    		
			try {
				context.campfire.login();
			} catch (CampfireException exception) {
				return exception;
			}
			return null;
    	}
    	
    	@Override
    	protected void onPostExecute(CampfireException exception) {
    		if (dialog != null && dialog.isShowing())
    			dialog.dismiss();
    		context.loginTask = null;
    		
    		context.onLogin(exception);
    	}
    	
    	public void loadingDialog() {
    		dialog = new ProgressDialog(context);
    		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		dialog.setMessage("Logging in…");
    		   
    		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
    			@Override
    			public void onCancel(DialogInterface dialog) {
					cancel(true);
					context.loginTask = null; // so that the button will work again
    			}
    		});
           
           dialog.show();
        }
	}
	
	static class LoginHolder {
		Campfire campfire;
		LoginTask loginTask;
	}
}