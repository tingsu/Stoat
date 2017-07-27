package com.kkinder.charmap;

import android.content.Context;
import android.text.ClipboardManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ButtonMapAdapter extends BaseAdapter {
    private Context mContext;
    private String[] characters = {};
    private View appEditArea;
    private EditText appEditor;
    private Charmap activity;
    
    public ButtonMapAdapter(Context c, Charmap a, View editArea, EditText editor, String[] chars) {
        mContext = c;
        activity = a;
        characters = chars;
        appEditArea = editArea;
        appEditor = editor;
    }

    public int getCount() {
        return characters.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ButtonView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	Button button;
        
    	if (convertView == null) {
            button = new Button(mContext);
        } else {
        	button = (Button) convertView;
        }
    	button.setText(characters[position]);
    	
    	button.setOnClickListener(new OnClickListener() {
    		@Override
    	    public void onClick(View v) {
    			if (appEditArea.getVisibility() == View.VISIBLE) {
    			    appEditor.append(((Button)v).getText());
    			} else {
        			ClipboardManager ClipMan = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        			ClipMan.setText(((Button)v).getText());
        			
        			CharSequence text = "\"" + ((Button)v).getText() + "\" copied to clipboard.";
        			int duration = Toast.LENGTH_SHORT;

        			Toast toast = Toast.makeText(mContext, text, duration);
        			toast.show();
        			activity.finish();
    			}
    	    }
    	});
        return button;
    }
}
