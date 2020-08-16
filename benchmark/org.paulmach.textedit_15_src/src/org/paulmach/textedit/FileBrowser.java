package org.paulmach.textedit;

import java.io.File;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/****************************************************************
 * FileBrowser
 *		The activity where you browse all the files and such 
 *		This file needs a few more comments, I'll admit */
public class FileBrowser extends Activity
{
	private final static int MENU_MKDIR = Menu.FIRST;
	
	private final int DIALOG_FILE_OPTIONS = 1;
	private final int DIALOG_RENAME = 2;
	private final int DIALOG_MKDIR = 3;
	private final int DIALOG_SHOULD_DELETE = 4;
	private final int DIALOG_COULDNT_DELETE = 5;
	private final int DIALOG_COULDNT_RENAME = 6;
	private final int DIALOG_COULDNT_MKDIR = 7;
	private final int DIALOG_COULDNT_DELETEDIR = 8;
	
	private File dialog_file;
	
	protected View textEntryView = null;
	protected View mkDIRView = null;
	
	protected FileViewArrayAdapter fileAdapter;
	protected ListView fileBrowserList;
	protected TextView fileBrowserPath;
	protected TextView fileBrowserNoFiles;

	protected CharSequence filePath = "";
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// we are doing our own title
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// setup the view
		setContentView(R.layout.filebrowser_all);

		/**********************
		 * Figure out default location */
		
		/**********************
		 * setup what the buttons should do */
		ImageButton useThisButton = (ImageButton) findViewById(R.id.fb_usethisdirbutton);
		ImageButton upButton = (ImageButton) findViewById(R.id.fb_upbutton);
		
		useThisButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

	            setResult(1, (new Intent()).setAction((String) filePath));
	            finish();				
			}
		});
		
		upButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				File f = new File((String) filePath);
				if (filePath != null && !filePath.equals("/"))
					updateFileBrowserList(f.getParent());
			}
		});

		/**********************
		 * setup what the listview should do */
		fileBrowserList = (ListView) findViewById(R.id.filelist);
		fileBrowserPath = (TextView) findViewById(R.id.filepath);
		fileBrowserNoFiles = (TextView) findViewById(R.id.nofilemessage);
				
		/**********************
		 * setup what the adapter */		
		fileAdapter = new FileViewArrayAdapter(this);
		fileAdapter.setMode(FileViewArrayAdapter.FILE_BROWSER_MODE);
		fileBrowserList.setAdapter(fileAdapter);

		fileBrowserList.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				File f = new File(arg0.getItemAtPosition(arg2).toString());
				dialog_file = f;
				showDialog(DIALOG_FILE_OPTIONS);
				
				return true;
		}} );
		
		fileBrowserList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {

				File f = new File(arg0.getItemAtPosition(arg2).toString());
				if (f.isDirectory())
					updateFileBrowserList(f.toString());
				else {
					// we clicked a file so go back
					filePath = (CharSequence) arg0.getItemAtPosition(arg2).toString();
					setResult(1, (new Intent()).setAction((String) filePath));

					finish();
				}						
		}} );
				
		/**********************
		 * get the first location */
		Intent intent = getIntent();
		filePath = intent.getAction();
		
		updateFileBrowserList(filePath.toString());
	
	} // end onCreate()
	
	/****************************************************************
	 * onPrepareDialog()
	 * 		This function is called EVERY time a dialog is displayed */
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		switch (id) {
			case DIALOG_FILE_OPTIONS: {
				dialog.setTitle(dialog_file.getName());
				break;
			}
			case DIALOG_SHOULD_DELETE: {
				dialog.setTitle("Confirm deletion of " + dialog_file.getName());
				break;
			}
			case DIALOG_RENAME: {
				EditText v = (EditText) textEntryView.findViewById(R.id.filename_edit);
				v.setText(dialog_file.getName());
				v.setSelection(v.getText().length(),v.getText().length());
				break;
			}
			case DIALOG_MKDIR: {
				EditText v = (EditText) mkDIRView.findViewById(R.id.filename_edit);
				v.setText("");
			}
		}
	} // end onPrepareDialog()
	
	/****************************************************************
	 * onCreateDialog()
	 * 		This function is called the FIRST time a dialog is displayed */
	protected Dialog onCreateDialog(int id)
	{
		switch (id) {
			default:
			case DIALOG_FILE_OPTIONS: {
				
				String[] options = {new String("Delete File"), new String("Rename File")};
				
				return new AlertDialog.Builder(this)
				.setTitle(dialog_file.getName())
				.setItems(options, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0)
						{
							showDialog(DIALOG_SHOULD_DELETE);
						} else if (which == 1) {
							showDialog(DIALOG_RENAME);						
						}
					}
				})
				.create();
			}
			case DIALOG_RENAME: {
				// create the layout we want
				LayoutInflater factory = LayoutInflater.from(this);
				textEntryView = factory.inflate(R.layout.dialog_rename, null);
				
				// the actual dialog being created
				return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle("Rename File")
					.setView(textEntryView)
					.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// what to do when positive clicked
							TextView v = (TextView) textEntryView.findViewById(R.id.filename_edit);
							
							File f = new File(dialog_file.getParent() + "/" + v.getText().toString());
							if (!dialog_file.renameTo(f))
							{
								showDialog(DIALOG_COULDNT_RENAME);
							}
							updateFileBrowserList((String) filePath, false);
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

						}
					})
				.create();
			}
			case DIALOG_MKDIR: {
				// create the layout we want
				LayoutInflater factory = LayoutInflater.from(this);
				mkDIRView = factory.inflate(R.layout.dialog_mkdir, null);
				
				// the actual dialog being created
				return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle("Create Directory")
					.setView(mkDIRView)
					.setPositiveButton("Create", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// what to do when positive clicked
							TextView v = (TextView) mkDIRView.findViewById(R.id.filename_edit);
							
							File f = new File(filePath + "/" + v.getText().toString());
							if (!f.mkdir())
							{
								showDialog(DIALOG_COULDNT_MKDIR);
							}
							updateFileBrowserList((String) filePath, false);
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

						}
					})
				.create();
			}
			case DIALOG_SHOULD_DELETE: {
				return new AlertDialog.Builder(this)
				.setTitle("Confirm deletion of " + dialog_file.getName())
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (!dialog_file.delete())
						{
							if (dialog_file.isDirectory())
								showDialog(DIALOG_COULDNT_DELETEDIR);
							else
								showDialog(DIALOG_COULDNT_DELETE);	
						}
						updateFileBrowserList((String) filePath, false);
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
					}
				})
                .create();
			}
			case DIALOG_COULDNT_DELETE: {
				return new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage("Unable to delete file.")
				.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
					}
				})
                .create();
			}
			case DIALOG_COULDNT_DELETEDIR: {
				return new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage("Unable to delete directory.\n\n- A directory must be empty to be deleted")
				.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
					}
				})
                .create();
			}
			case DIALOG_COULDNT_RENAME: {
				return new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage("Unable to rename file.")
				.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
					}
				})
                .create();
			}
			case DIALOG_COULDNT_MKDIR: {
				return new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage("Unable to create directory.")
				.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
					}
				})
                .create();
			}
		}
	} // end onCreateDialog()

	
	/****************************************************************
	 * menu Functions */	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_MKDIR, 0, "Create Directory").setShortcut('0', 'c').setIcon(android.R.drawable.ic_menu_add);

		return true;
	} // end onCreateOptionsMenu()

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case MENU_MKDIR:
				showDialog(DIALOG_MKDIR);
				return true;
		}
		
		return true;
	} // end onOptionsItemSelected()
	
	
	/********************************
	 * Save the state */
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		
		// this mode needs to be saved when the dialog box flips
		savedInstanceState.putInt("FB_mode", fileAdapter.mode);
		savedInstanceState.putString("FilePath", filePath.toString());
		if (dialog_file != null)
			savedInstanceState.putString("dialog_file", dialog_file.getAbsolutePath());
		else
			savedInstanceState.putString("dialog_file", "");
		
		super.onSaveInstanceState(savedInstanceState);
	} // end onSaveInstanceState()

	/********************************
	 * Restore the state */
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	 
		// this mode needs to be saved when the dialog box flips.
		fileAdapter.mode = savedInstanceState.getInt("FB_mode");
		filePath = savedInstanceState.getString("FilePath");
		dialog_file = new File(savedInstanceState.getString("dialog_file"));
		
		// fileBrowserReturnPath is saved by the pmTextEdit class 
		// onRestore and onSave methods
		updateFileBrowserList(filePath.toString(), false);
		
	} // end onRestoreInstanceState()
	
	/****************************************************************
	 * updateFileBrowserList()
	 * 		Reads files to update the filebrowser when stuff is clicked */
	private void updateFileBrowserList(String location)
	{
		updateFileBrowserList(location, true);
	}
	
	private void updateFileBrowserList(String location, boolean backToTop)
	{
		if (location == null)
			location = "";

		File loc = new File(location);
		
		if (!loc.isDirectory())
			loc = loc.getParentFile();

		// just in case our directory is bad and our file turned out bad
		if (loc == null)
			loc = new File("/sdcard/");

		File[] files;
		files = loc.listFiles();
		if (files != null)
			Arrays.sort(files);
		else
			files = new File[0];
		
		fileAdapter.clear();
		
		int i, nonHidden = 0, len = files.length;
		
		for(i = 0; i < len; i++)
		{
			if (files[i].isDirectory() && !files[i].isHidden())
			{
				fileAdapter.add(files[i].toString());
				nonHidden++;
			}
		}
		
		for(i = 0; i < len; i++)
		{
			if (!files[i].isDirectory() && !files[i].isHidden())
			{
				fileAdapter.add(files[i].toString());
				nonHidden++;
			}
		}
		
		if (nonHidden == 0)
			fileBrowserNoFiles.setVisibility(View.VISIBLE);
		else
			fileBrowserNoFiles.setVisibility(View.GONE);
		
		// update the path
		if (loc.getPath().equals("/"))
		{
			fileBrowserPath.setText("Location: /");
			filePath = "/";
		} else {
			fileBrowserPath.setText("Location: " + loc.getPath() + "/");
			filePath = loc.getPath() + "/";
		}
		
		if (backToTop)
			fileBrowserList.setSelection(0);
		
		setResult(1, (new Intent()).setAction((String) filePath));
	} // end updateFileBrowserList()
} // end class fileBrowser