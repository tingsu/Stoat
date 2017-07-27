/*
 * Copyright (C) 2011 Nephoapp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nephoapp.anarxiv;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.nephoapp.anarxiv.R;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


/**
 * @author lihe
 *
 */
public class PaperDetailWnd extends Activity 
{
	/**
	 * ui components.
	 */
	private ListView _uiAuthorList = null;
	private TextView _uiPaperTitle = null;
	private TextView _uiSummary = null;
	private TextView _uiPaperDate = null;
	private TextView _uiFileSize = null;
	
	/**
	 * gesture detector.
	 */
	private  GestureDetector _gestureDetector = null;
	
	/**
	 * downloader.
	 */
	private ArxivFileDownloader _downloader = new ArxivFileDownloader();
	
	/**
	 * file url.
	 */
	private String _fileUrl = null;
	
	/**
	 * local file path.
	 */
	private String _localFilePath = null;
	
	/**
	 * gesture handler.
	 */
	private class myGestureListener extends GestureDetector.SimpleOnGestureListener
	{
		/**
		 * onFling.
		 */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			if (e1.getX() - e2.getX() > ConstantTable.FLING_MIN_DISTANCE && 
					Math.abs(velocityX) > ConstantTable.FLING_MIN_VELOCITY)
			{
				finish();
			}
			
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}
	
	/**
	 * util: view pdf file.
	 */
	private void viewPdf(File pdfFile)
	{
		if(pdfFile.exists() == false)
		{
			UiUtils.showErrorMessage(this, "File does not exist: " + pdfFile.getName());
			return;
		}
		
		/* get file uri. */
		Uri path = Uri.fromFile(pdfFile);
		
		/* create intent and launch activity. */
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(path, "application/pdf");
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		try
		{
			startActivity(intent);
		}
		catch(ActivityNotFoundException e)
		{
			throw e;
		}
	}
	
	/**
	 * thread for downloading pdf from arxiv.
	 */
	private class PdfDownloadingThread extends Thread
	{
		/**
		 * 
		 */
		@Override
		public void run()
		{
			try
			{
				File pdfFile = new File(_localFilePath);
				
				/* download when the file does not exist. */
				if (pdfFile.exists() == false)
					_downloader.download(_fileUrl, _localFilePath);
				PaperDetailWnd.this.viewPdf(pdfFile);
			}
			catch (ArxivFileDownloader.FileDownloaderException e)
			{
				final String errMsg = e.getMessage();
				
				PaperDetailWnd.this.runOnUiThread
					(
						new Runnable()
						{
							public void run()
							{
								UiUtils.showErrorMessage(PaperDetailWnd.this, errMsg);
							}
						}
					);
			}
			catch (ActivityNotFoundException e)
			{
				PaperDetailWnd.this.runOnUiThread
				(
					new Runnable()
					{
						public void run()
						{
							UiUtils.showErrorMessage(PaperDetailWnd.this, "No PDF view available.");
						}
					}
				);
			}
		}
	}
	
	/**
	 * thread for getting file size.
	 */
	private class FileSizeLoadingThread extends Thread
	{
		/**
		 * 
		 */
		@Override
		public void run()
		{
			/* try to get file size. */
			try
			{
				final int fileSize = ArxivFileDownloader.getFileSize(_fileUrl);
				
				PaperDetailWnd.this.runOnUiThread(
													new Runnable() 
													{
														public void run() 
														{
															if (fileSize != -1)
																_uiFileSize.setText("File Size: " + fileSize / 1000 + " KB");
															else
																_uiFileSize.setText("File Size: unknown");
														}
													}
												 );
			}
			catch (Exception e)
			{
				
			}
		}
	}
	
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.paper_detail_view);
		
		/* get intent and associated params. */
		Intent intent = getIntent();
		@SuppressWarnings("unchecked")
		HashMap<String, Object> detail = (HashMap<String, Object>)intent.getSerializableExtra("paperdetail");
		
		displayPaperDetail(detail);
	}
	
	/**
	 * display paper detail.
	 */
	protected void displayPaperDetail(HashMap<String, Object> detail)
	{
		if (detail == null)
			return;
		
		/* get ui components. */
		_uiAuthorList = (ListView)findViewById(R.id.paperdetail_authorlist);
		_uiPaperTitle = (TextView)findViewById(R.id.paperdetail_title);
		_uiSummary = (TextView)findViewById(R.id.paperdetail_summary);
		_uiPaperDate = (TextView)findViewById(R.id.paperdetail_date);
		_uiFileSize = (TextView)findViewById(R.id.paperdetail_filesize);
		
		/* set text data. */
		_uiPaperTitle.setText((String)detail.get("title"));
		_uiPaperDate.setText((String)detail.get("date"));
		_uiSummary.setText((String)detail.get("summary"));
		
		/* set file url. */
		_fileUrl = (String)detail.get("url");
		
		/* set list data. */
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
																R.layout.author_list_item,
																(ArrayList<String>)detail.get("authorlist"));
		_uiAuthorList.setAdapter(adapter);
		
		/* setup local file path. */
		String[] urlParts = _fileUrl.split("/");
		_localFilePath = ConstantTable.getAppRootDir() + "/" + urlParts[urlParts.length - 1] + ".pdf";
		
		/* context menu. */
		registerForContextMenu(_uiSummary);
		
		/* gesture detector. */
		_gestureDetector = new GestureDetector(this, new myGestureListener());
		
		/* load file size. */
		UiUtils.showToast(this, getResources().getString(R.string.loading_file_size));
		new FileSizeLoadingThread().start();
	}
	
	/**
	 * intercept all touch event for gesture detector.
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		_gestureDetector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}
	
	/**
	 * create options menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_paperdetail, menu);
		return true;
	}
	
	/**
	 * handle options menu.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_paperdetail_view)
		{
			new PdfDownloadingThread().start();
			UiUtils.showToast(this, getResources().getString(R.string.loading_file));
		}
		else if (item.getItemId() == R.id.menu_paperdetail_delete)
		{
			try
			{
				StorageUtils.removeFile(_localFilePath);
				UiUtils.showToast(this, getResources().getString(R.string.file_deleted));
			}
			catch (Exception e)
			{
				
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * context menu.
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ctxmenu_paper_detail, menu);
		menu.setHeaderTitle("Options");
	}
	
	/**
	 * context menu handler.
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.ctxmenu_paper_detail_view)
		{
			new PdfDownloadingThread().start();
			UiUtils.showToast(this, getResources().getString(R.string.loading_file));
		}
		else if (item.getItemId() == R.id.ctxmenu_paper_detail_delete)
		{
			try
			{
				StorageUtils.removeFile(_localFilePath);
				UiUtils.showToast(this, getResources().getString(R.string.file_deleted));
			}
			catch (Exception e)
			{
				
			}
		}
		else if (item.getItemId() == R.id.ctxmenu_paper_detail_add_to_favorite)
		{
			/* get data from intent. */
			Intent intent = getIntent();
			@SuppressWarnings("unchecked")
			HashMap<String, Object> detail = (HashMap<String, Object>)intent.getSerializableExtra("paperdetail");
			
			/* fill in paper struct. */
			AnarxivDB.Paper paper = new AnarxivDB.Paper();
			paper._author = (String)detail.get("author");
			paper._date = (String)detail.get("date");
			paper._id = (String)detail.get("id");
			paper._title = (String)detail.get("title");
			paper._url = (String)detail.get("url");
			
			try
			{
				AnarxivDB.getInstance().addFavoritePaper(paper);
				UiUtils.showToast(this, "Added to favorite: " + paper._title);
			}
			catch (AnarxivDB.DBException e)
			{
				UiUtils.showToast(this, e.getMessage());
			}
		}
		
		return super.onContextItemSelected(item);
	}
}
