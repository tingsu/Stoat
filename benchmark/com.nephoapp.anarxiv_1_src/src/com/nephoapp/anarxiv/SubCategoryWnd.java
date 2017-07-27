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

import com.nephoapp.anarxiv.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;



public class SubCategoryWnd extends Activity implements OnItemClickListener
{
	/* ui components. */
	private ListView _uiSubCatList = null;
	private TextView _uiSubCatName = null;
	
	/** sub category name. */
	private String _subCatName = null;
	
	/** sub category list. */
	private String[] _subCatList = null;
	
	/** gesture detector. */
	private GestureDetector _gestureDetector = null;
	
	/** arxiv loader. */
//	private ArxivLoader _arxivLoader = anarxiv.getArxivLoaderInstance();
	
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
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subcategory);
		
		/* get parameters from intent. */
		Intent intent = getIntent();
		_subCatName = intent.getStringExtra("subcatname");
		_subCatList = intent.getStringArrayExtra("subcatlist");
		
		/* get ui. */
		_uiSubCatList = (ListView)this.findViewById(R.id.subcategorylist);
		_uiSubCatName = (TextView)this.findViewById(R.id.subcategoryname);
		
		/* adapter for the list. */
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, _subCatList);
		_uiSubCatList.setAdapter(aa);
		
		/* title. */
		_uiSubCatName.setText(_subCatName);
		
		/* event listener. */
		_uiSubCatList.setOnItemClickListener(this);
		
		/* register context menu. */
		registerForContextMenu(_uiSubCatList);
		
		/* gesture detector. */
		_gestureDetector = new GestureDetector(this, new myGestureListener());
	}
	
	/** 
	 * handler: onItemClick. 
	 */
	public void onItemClick(AdapterView<?> a, View v, int position, long id) 
	{
		// TODO Auto-generated method stub
		if(a.getId() == R.id.subcategorylist)
		{
			String catName = (String)a.getItemAtPosition(position);
			String qstring = anarxiv._urlTbl.getQueryString(catName);
			
			/* add to recent category table. */
			try
			{
				AnarxivDB.Category category = new AnarxivDB.Category();
				category._name = catName;
				category._parent = _subCatName;
				category._queryWord = qstring;
				AnarxivDB.getInstance().addRecentCategory(category);
			}
			catch (AnarxivDB.DBException e)
			{
				UiUtils.showErrorMessage(this, e.getMessage());
			}
			
			/* start new activity. */
			Intent intent = new Intent(this, PaperListWnd.class);
			intent.putExtra("category", qstring);
			intent.putExtra("categoryname", catName);
			startActivity(intent);
		}
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
	 * override: context menu.
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ctxmenu_add_to_favorite, menu);
		menu.setHeaderTitle("Add to Favorite");
	}
	
	/**
	 * override: context menu handler.
	 */
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		
		/* add the favorite. */
		if (item.getItemId() == R.id.ctxmenu_add_to_favorite)
		{
			/* retrieve names. */
			String catName = (String)_uiSubCatList.getItemAtPosition(info.position);
			String parent = _subCatName;
			
			/* fill in category info. */
			AnarxivDB.Category category = new AnarxivDB.Category();
			category._name = catName;
			category._parent = parent;
			category._queryWord = anarxiv._urlTbl.getQueryString(catName);
			
			/* add to db. */
			try
			{
				AnarxivDB.getInstance().addFavoriteCategory(category);
				UiUtils.showToast(this, "Added to favorite: " + catName);
			}
			catch (AnarxivDB.DBException e)
			{
				UiUtils.showToast(this, e.getMessage());
			}
		}
		
		return super.onContextItemSelected(item);
	}
}
