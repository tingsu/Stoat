package fr.keuse.rightsalert.adapter;

import java.util.ArrayList;

import fr.keuse.rightsalert.entity.ApplicationEntity;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ApplistAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<ApplicationEntity> applications;
	
	public ApplistAdapter(Context context, ArrayList<ApplicationEntity> applications) {
		this.context = context;
		this.applications = applications;
	}

	public int getCount() {
		return applications.size();
	}

	public ApplicationEntity getItem(int location) {
		return applications.get(location);
	}

	public long getItemId(int location) {
		return location;
	}

	public View getView(int location, View convertView, ViewGroup parent) {
		ApplicationEntity application = getItem(location);
		
		ImageView icon = new ImageView(context);
		TextView name = new TextView(context);
		TextView score = new TextView(context);
		LinearLayout view = new LinearLayout(context);
		
		icon.setImageDrawable(application.getIcon());
		icon.setAdjustViewBounds(true);
		icon.setMaxHeight(40);
		icon.setMaxWidth(40);
		
		name.setText(application.getName());
		
		score.setText(String.valueOf(application.getScore()));
		score.setGravity(Gravity.RIGHT);
		
		view.setOrientation(LinearLayout.HORIZONTAL);
		view.addView(icon);
		view.addView(name);
		view.addView(score, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		return view;
	}

}
