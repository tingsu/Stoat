package org.paulmach.textedit;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/****************************************************************
 * FileAutoCompleteArrayAdapter
 *		To customize what is displayed in the autocomplete 
 *		options we use this class, getView() is where the change
 *		happens. */
public class FileAutoCompleteArrayAdapter extends ArrayAdapter<String>
{
	// just a basic constructor
	public FileAutoCompleteArrayAdapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{
		// get the view that would normally be returned
		TextView tv = (TextView) super.getView(position, convertView, parent);
		
		// update the text accordingly
		File f = new File(tv.getText().toString());
		if (f.isDirectory())
			tv.setText(f.getName() + "/");
		else
			tv.setText(f.getName());

		// change some options
		tv.setPadding(10,3,3,3);
		tv.setTextSize(18.0f);
		
		return (View) tv;	// return the view
	}
	
} // end class AutoCompleteArrayAdapter