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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;


public class AnarxivDB
{
	/**
	 * paper information.
	 */
	public static class Paper
	{
		public String _id;
		public String _date;
		public String _title;
		public String _author;
		public String _url;
	}
	
	/**
	 * category information.
	 */
	public static class Category
	{
		public String _name;
		public String _parent;
		public String _queryWord;
	}
	
	/**
	 * database exception.
	 */
	public static class DBException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		DBException()
		{
			
		}
		
		DBException(String msg)
		{
			super(msg);
		}
		
		DBException(String msg, Throwable e)
		{
			super(msg, e);
		}
	}
	
	/**
	 * database name.
	 */
	private static final String _databasePath = "anarxivdb";
	
	/**
	 * table names.
	 */
	private static final String _tbl_RecentPaper = "recent_paper";
	private static final String _tbl_FavoritePaper = "favorite_paper";
	private static final String _tbl_RecentCategory = "recent_category";
	private static final String _tbl_FavoriteCategory = "favorite_category";
	
	/**
	 * table creation statements.
	 */
	private static final String _createTbl_RecentPaper = 
							"create table if not exists " + AnarxivDB._tbl_RecentPaper + 
							"(db_id integer primary key autoincrement, " +
							"_date text, " +
							"_id text, " +
							"_title text, " +
							"_author text, " +
							"_url text)";
	private static final String _createTbl_FavoritePaper = 
							"create table if not exists " + AnarxivDB._tbl_FavoritePaper + 
							"(db_id integer primary key autoincrement, " +
							"_date text, " +
							"_id text, " +
							"_title text, " +
							"_author text, " +
							"_url text)";
	private static final String _createTbl_RecentCategory =
							"create table if not exists " + AnarxivDB._tbl_RecentCategory +
							"(db_id integer primary key autoincrement, " +
							"_name text, " +
							"_parent text, " +
							"_queryword text)";
	private static final String _createTbl_FavoriteCategory =
							"create table if not exists " + AnarxivDB._tbl_FavoriteCategory +
							"(db_id integer primary key autoincrement, " +
							"_name text, " +
							"_parent text, " +
							"_queryword text)";
	
	/**
	 * the sqlite database object.
	 */
	private SQLiteDatabase _sqliteDB = null;
	
	/**
	 * singleton instance.
	 */
	private static AnarxivDB _theInstance = null;
	
	/**
	 * context.
	 */
	private static Context _context = null;
	
	/**
	 * get the instance.
	 */
	public static AnarxivDB getInstance()
	{
		if (AnarxivDB._theInstance == null)
			_theInstance = new AnarxivDB();
		return AnarxivDB._theInstance;
	}
	
	/**
	 * set owner context. must be set before use.
	 */
	public static void setOwner(Context context)
	{
		AnarxivDB._context = context;
	}

	/**
	 * 
	 */
	public AnarxivDB() 
	{
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * retrieve paper table.
	 */
	private ArrayList<Paper> retrievePapersFromDB(String table, Integer limit)
	{
		/* query the database. */
		Cursor c = _sqliteDB.query(table, 
								   new String[] {"_id", "_author", "_title", "_date", "_url"}, 
								   null, 
								   null, 
								   null, 
								   null, 
								   "db_id desc",
								   limit < 0 ? null : limit.toString());
		c.moveToFirst();
		
		ArrayList<Paper> paperList = new ArrayList<Paper>();
		
		for (int i = 0; i < c.getCount(); i ++)
		{
			Paper paper = new Paper();
			paper._author = c.getString(c.getColumnIndex("_author"));
			paper._date = c.getString(c.getColumnIndex("_date"));
			paper._id = c.getString(c.getColumnIndex("_id"));
			paper._title = c.getString(c.getColumnIndex("_title"));
			paper._url = c.getString(c.getColumnIndex("_url"));
			
			paperList.add(paper);
			c.moveToNext();
		}
		
		return paperList;
	}
	
	/**
	 * retrieve category table.
	 */
	private ArrayList<Category> retrieveCategoriesFromDB(String table, Integer limit)
	{
		/* query the database. */
		Cursor c = _sqliteDB.query(table, 
								   new String[] {"_name", "_parent", "_queryword"}, 
								   null, 
								   null, 
								   null, 
								   null, 
								   "db_id desc",
								   limit < 0 ? null : limit.toString());
		c.moveToFirst();
		
		ArrayList<Category> categoryList = new ArrayList<Category>();
		
		for (int i = 0; i < c.getCount(); i ++)
		{
			Category category = new Category();
			category._name = c.getString(c.getColumnIndex("_name"));
			category._parent = c.getString(c.getColumnIndex("_parent"));
			category._queryWord = c.getString(c.getColumnIndex("_queryword"));
			
			categoryList.add(category);
			c.moveToNext();
		}
		
		return categoryList;
	}
	
	/**
	 * util: data converter.
	 */
	public static ContentValues paperToContentValues(Paper paper)
	{
		ContentValues cv = new ContentValues();
		cv.put("_id", paper._id);
		cv.put("_date", paper._date);
		cv.put("_title", paper._title);
		cv.put("_url", paper._url);
		cv.put("_author", paper._author);
		return cv;
	}
	
	/**
	 * util: data converter.
	 */
	public static List<HashMap<String, Object>> paperListToMapList(List<Paper> paperList)
	{
		List<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>();
		
		for (Paper paper: paperList)
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("author", paper._author);
			map.put("date", paper._date);
			map.put("id", paper._id);
			map.put("title", paper._title);
			map.put("url", paper._url);
			
			mapList.add(map);
		}
		
		return mapList;
	}
	
	/**
	 * util: data converter.
	 */
	public static List<HashMap<String, Object>> categoryListToMapList(List<Category> categoryList)
	{
		List<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>();
		
		for (Category category: categoryList)
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", category._name);
			map.put("parent", category._parent);
			map.put("queryword", category._queryWord);
			
			mapList.add(map);
		}
		
		return mapList;
	}
	
	/**
	 * util: data converter.
	 */
	public static ContentValues categoryToContentValues(Category category)
	{
		ContentValues cv = new ContentValues();
		cv.put("_name", category._name);
		cv.put("_parent", category._parent);
		cv.put("_queryword", category._queryWord);
		return cv;
	}
	
	/**
	 * open the database.
	 */
	public void open() throws DBException
	{
		try
		{
			if (_sqliteDB == null)
			{
				/* open database. */
				_sqliteDB = _context.openOrCreateDatabase(AnarxivDB._databasePath, 
														  Context.MODE_WORLD_WRITEABLE,
														  null);
				
				/* create tables. */
				_sqliteDB.execSQL(AnarxivDB._createTbl_RecentPaper);
				_sqliteDB.execSQL(AnarxivDB._createTbl_FavoritePaper);
				_sqliteDB.execSQL(AnarxivDB._createTbl_RecentCategory);
				_sqliteDB.execSQL(AnarxivDB._createTbl_FavoriteCategory);
			}
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * close the database.
	 */
	public void close()
	{
		_sqliteDB.close();
	}

	/**
	 * insert a recent paper.
	 */
	public long addRecentPaper(Paper paper) throws DBException
	{
		try
		{
			_sqliteDB.delete(AnarxivDB._tbl_RecentPaper, "_id = '" + paper._id + "'", null);
			return _sqliteDB.insert(AnarxivDB._tbl_RecentPaper, null, AnarxivDB.paperToContentValues(paper));
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * remove all recent papers.
	 */
	public int removeAllRecentPapers() throws DBException
	{
		try
		{
			return _sqliteDB.delete(AnarxivDB._tbl_RecentPaper, null, null);
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * insert a recent category.
	 */
	public long addRecentCategory(Category category) throws DBException
	{
		try
		{
			_sqliteDB.delete(AnarxivDB._tbl_RecentCategory, "_name = '" + category._name + "' and _parent = '" + category._parent +"'", null);
			return _sqliteDB.insert(AnarxivDB._tbl_RecentCategory, null, AnarxivDB.categoryToContentValues(category));
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * remove all recent categories.
	 */
	public int removeAllRecentCategories() throws DBException
	{
		try
		{
			return _sqliteDB.delete(AnarxivDB._tbl_RecentCategory, null, null);
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * insert a favorite paper.
	 */
	public long addFavoritePaper(Paper paper) throws DBException
	{
		try
		{
			return _sqliteDB.insert(AnarxivDB._tbl_FavoritePaper, null, AnarxivDB.paperToContentValues(paper));
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * remove favorite paper.
	 */
	public int removeFavoritePaper(Paper paper) throws DBException
	{
		try
		{
			if (paper == null)
				return _sqliteDB.delete(AnarxivDB._tbl_FavoritePaper, null, null);
			else
			{
				String where = "_author = '" + paper._author + 
							   "' and _date = '" + paper._date + 
							   "' and _id = '" + paper._id +
							   "' and _title = '" + paper._title + 
							   "' and _url = '" + paper._url + "'";
				return _sqliteDB.delete(AnarxivDB._tbl_FavoritePaper,
										where, 
										null);
			}
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * insert a favorite category.
	 */
	public long addFavoriteCategory(Category category) throws DBException
	{
		try
		{
			return _sqliteDB.insert(AnarxivDB._tbl_FavoriteCategory, null, AnarxivDB.categoryToContentValues(category));
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * remove favorite category.
	 */
	public int removeFavoriteCategory(Category category) throws DBException
	{
		try
		{
			if (category == null)
				return _sqliteDB.delete(AnarxivDB._tbl_FavoriteCategory, null, null);
			else
			{
				String where = "_name = '" + category._name + 
							   "' and _parent = '" + category._parent + 
							   "' and _queryword = '" + category._queryWord + "'";
				return _sqliteDB.delete(AnarxivDB._tbl_FavoriteCategory, 
										where, 
										null);
			}
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * get favorite paper list.
	 */
	public ArrayList<Paper> getFavoritePapers() throws DBException
	{
		try
		{
			return retrievePapersFromDB(AnarxivDB._tbl_FavoritePaper, -1);
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * get most recent papers.
	 */
	public ArrayList<Paper> getRecentPapers(Integer limit) throws DBException
	{
		try
		{
			return retrievePapersFromDB(AnarxivDB._tbl_RecentPaper, limit);
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * get favorite category list.
	 */
	public ArrayList<Category> getFavoriteCategories() throws DBException
	{
		try
		{
			return retrieveCategoriesFromDB(AnarxivDB._tbl_FavoriteCategory, -1);
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
	
	/**
	 * get recent category list.
	 */
	public ArrayList<Category> getRecentCategories() throws DBException
	{
		try
		{
			return retrieveCategoriesFromDB(AnarxivDB._tbl_RecentCategory, -1);
		}
		catch (SQLiteException e)
		{
			throw new DBException(e.getMessage(), e);
		}
	}
}
