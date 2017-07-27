package fr.keuse.rightsalert.activity;

import java.util.ArrayList;

import fr.keuse.rightsalert.R;
import fr.keuse.rightsalert.adapter.ApplistAdapter;
import fr.keuse.rightsalert.entity.ApplicationEntity;
import fr.keuse.rightsalert.handler.LoadApplicationsHandler;
import fr.keuse.rightsalert.helper.Score;
import fr.keuse.rightsalert.preference.RightsalertPreference;
import fr.keuse.rightsalert.thread.LoadApplicationsThread;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ApplistActivity extends Activity implements DialogInterface.OnCancelListener, DialogInterface.OnClickListener {
	private final static int PREFERENCE_ACTIVITY_CODE = 1;
	
	private ProgressDialog progress;
	private static LoadApplicationsThread thread;
	private ArrayList<ApplicationEntity> applications;
	private ApplistAdapter adapter;
	private TextView count;
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applist);
        
        count = (TextView) findViewById(R.id.applist_count);
        ListView list = (ListView) findViewById(R.id.applist_list);
        
        progress = new ProgressDialog(this);
        
        applications = (ArrayList<ApplicationEntity>) getLastNonConfigurationInstance();
        if(applications == null)
        	applications = new ArrayList<ApplicationEntity>();
        
        adapter = new ApplistAdapter(this, applications);
        list.setAdapter(adapter);
        
        if(applications.size() == 0) {
        	loadApplications();
        } else {
        	refreshView();
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_settings:
			Intent intent = new Intent(this, RightsalertPreference.class);
			startActivityForResult(intent, PREFERENCE_ACTIVITY_CODE);
			break;
		case R.id.menu_about:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			View alertView = getLayoutInflater().inflate(R.layout.about, null);
			TextView codesite = (TextView) alertView.findViewById(R.id.about_codesite);
			codesite.setMovementMethod(LinkMovementMethod.getInstance());
			TextView translation = (TextView) alertView.findViewById(R.id.about_translation);
			translation.setMovementMethod(LinkMovementMethod.getInstance());
			alert.setView(alertView);
			alert.setNeutralButton(android.R.string.ok, null);
			alert.create().show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
		case PREFERENCE_ACTIVITY_CODE:
			applications.clear();
			loadApplications();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(progress.isShowing())
			progress.dismiss();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return applications;
	}

	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
		case DialogInterface.BUTTON_NEGATIVE:
			onCancel(dialog);
		}
	}

	public void onCancel(DialogInterface dialog) {
		thread.interrupt();
		progress.dismiss();
		finish();
	}
	
	public void loadApplications() {
		PackageManager pm = getPackageManager();
        
        progress.setProgress(0);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setTitle(getString(R.string.applist_loading));
        progress.setMessage(getString(R.string.applist_loading));
        progress.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.applist_cancel), this);
        progress.setOnCancelListener(this);
    
        LoadApplicationsHandler handler = new LoadApplicationsHandler(progress, this, applications);
        
        if(thread == null || !thread.isAlive()) {
        	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        	Score.setPreferences(preferences);
	        thread = new LoadApplicationsThread(pm, preferences);
	        thread.start();
        }
        thread.setHandler(handler);
    	thread.sendOpenPopup();
	}
	
	public void refreshView() {
		adapter.notifyDataSetChanged();
		count.setText(getString(R.string.applist_count, applications.size()));
	}
}