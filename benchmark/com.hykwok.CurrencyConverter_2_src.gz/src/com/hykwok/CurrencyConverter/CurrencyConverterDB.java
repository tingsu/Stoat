/*
	Copyright 2010-2012 Kwok Ho Yin

   	Licensed under the Apache License, Version 2.0 (the "License");
   	you may not use this file except in compliance with the License.
   	You may obtain a copy of the License at

    	http://www.apache.org/licenses/LICENSE-2.0

   	Unless required by applicable law or agreed to in writing, software
   	distributed under the License is distributed on an "AS IS" BASIS,
   	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   	See the License for the specific language governing permissions and
   	limitations under the License.
*/

package com.hykwok.CurrencyConverter;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CurrencyConverterDB {

	// This variable is used for debug log (LogCat)
	private static final String TAG = "CC:CurrencyConverterDB";

	// Define default currencies' name and their icons
	// Here, we assume that each currency has its own name and icon
	// Current available currencies name
	public static final String[] currency_name = { 
			"EUR", "USD", "JPY", "BGN", 
            "CZK", "DKK", "GBP", 
            "HUF", "LTL", "LVL", "PLN", 
            "RON", "SEK", "CHF", "NOK", 
            "HRK", "RUB", "TRY", "AUD", 
            "BRL", "CAD", "CNY", "HKD", 
            "IDR", "ILS", "INR", "KRW", "MXN", 
			"MYR", "NZD", "PHP", "SGD", 
            "THB", "ZAR" 
	};
	
	// Current available currencies long name
	public static final Integer[] currency_longname = {
			R.string.szEUR, R.string.szUSD, R.string.szJPY, R.string.szBGN,
			R.string.szCZK, R.string.szDKK, R.string.szGBP,
			R.string.szHUF, R.string.szLTL, R.string.szLVL, R.string.szPLN,
			R.string.szRON, R.string.szSEK, R.string.szCHF, R.string.szNOK,
			R.string.szHRK, R.string.szRUB, R.string.szTRY, R.string.szAUD,
			R.string.szBRL, R.string.szCAD, R.string.szCNY, R.string.szHKD,
			R.string.szIDR, R.string.szILS, R.string.szINR, R.string.szKRW, R.string.szMXN,
			R.string.szMYR, R.string.szNZD, R.string.szPHP, R.string.szSGD,
			R.string.szTHB, R.string.szZAR
	};
	
	// Current available currencies icon
	public static final Integer[] currency_icon = { 
			R.drawable.flag_eur, R.drawable.flag_usd, R.drawable.flag_jpy, R.drawable.flag_bgn,
			R.drawable.flag_czk, R.drawable.flag_dkk, R.drawable.flag_gbp,
			R.drawable.flag_huf, R.drawable.flag_ltl, R.drawable.flag_lvl, R.drawable.flag_pln,
            R.drawable.flag_ron, R.drawable.flag_sek, R.drawable.flag_chf, R.drawable.flag_nok,
            R.drawable.flag_hrk, R.drawable.flag_rub, R.drawable.flag_try, R.drawable.flag_aud,
            R.drawable.flag_brl, R.drawable.flag_cad, R.drawable.flag_cny, R.drawable.flag_hkd,
            R.drawable.flag_idr, R.drawable.flag_ils, R.drawable.flag_inr, R.drawable.flag_kpw, R.drawable.flag_mxn,
            R.drawable.flag_myr, R.drawable.flag_nzd, R.drawable.flag_php, R.drawable.flag_sgd,
            R.drawable.flag_thb, R.drawable.flag_zar
	};
	
	// Database setting variables
	private static final String DATABASE_NAME = "db_cc.db";
	private static final int DATABASE_VERSION = 2;
	
	private Context local_context = null;
	
	/**
	 * 	Table: cc_rates
	 * 	Columns:
	 * 		cc_name		TEXT		// currency symbol
	 * 		cc_rate		FLOAT		// currency rate where the base currency is EUR	
	 */
	private static final String TABLE_CC_RATE = "cc_rates";	
	private static final String COL_CC_NAME = "cc_name";
	private static final String COL_CC_RATE = "cc_rate";
	
	// Database helper class	
	private class CCDB_Helper extends SQLiteOpenHelper {
		public CCDB_Helper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// backup context
			local_context = context;
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
	        
			try {
				String str_sql;
				
				// if database is not existed, create new tables
				str_sql = "CREATE TABLE " + TABLE_CC_RATE + " ( " +
				          COL_CC_NAME + " TEXT" +  ", " +
				          COL_CC_RATE + " FLOAT" +
				          " );";
				Log.d(TAG, "setup tables: SQL="+str_sql);
				db.execSQL(str_sql);
				
				// setup tables
				for(int i=0; i<currency_name.length; i++) {				
					str_sql = "INSERT INTO " + TABLE_CC_RATE + 
					          " ( " + COL_CC_NAME + "," + COL_CC_RATE 
					          + ") VALUES ('" + currency_name[i] + "', 1.0);";
					Log.d(TAG, "setup tables: SQL="+str_sql);
					db.execSQL(str_sql);
				}
				
				UpdateTableFromFile(db);
			} catch (Exception e) {
				Log.e(TAG, "onCreate:" + e.toString());				
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "update database: old version=" + Integer.toString(oldVersion) + " new version=" + Integer.toString(newVersion));
			
			// Delete all existing records and use latest one
			try {
				String str_sql;
				
				// delete all records
				str_sql = "DELETE FROM " + TABLE_CC_RATE + ";";
				Log.d(TAG, "delete all records from the tables: SQL="+str_sql);
				db.execSQL(str_sql);
				
				// setup tables
				for(int i=0; i<currency_name.length; i++) {				
					str_sql = "INSERT INTO " + TABLE_CC_RATE + 
					          " ( " + COL_CC_NAME + "," + COL_CC_RATE 
					          + ") VALUES ('" + currency_name[i] + "', 1.0);";
					Log.d(TAG, "setup tables: SQL="+str_sql);
					db.execSQL(str_sql);
				}
				
				UpdateTableFromFile(db);
			} catch (Exception e) {
				Log.e(TAG, "onUpgrade:" + e.toString());				
			}
		}
		
		private void UpdateTableFromFile(SQLiteDatabase db) {
			// load default data from raw folder
			CurrencyRateParser_ECB	parser = new CurrencyRateParser_ECB();
	        parser.StartParser(local_context, R.raw.eurofxref_daily);
	        List<CurrencyRate> filedata = parser.getRates();
	        
			try {
				String str_sql;
				
				// use default date from raw folder to update table
				for(int i=0; i<filedata.size(); i++) {
					str_sql = "UPDATE " + TABLE_CC_RATE + " SET " +
							  COL_CC_RATE + "=" + Double.toString(filedata.get(i).m_rate) +
		                      " WHERE " + COL_CC_NAME + "='" + filedata.get(i).m_name + "';";
					Log.d(TAG, "setup tables: SQL="+str_sql);
					db.execSQL(str_sql);
				}
			} catch (Exception e) {
				Log.e(TAG, "onCreate:" + e.toString());				
			}
		}
	}
	
	// database variables
	private CCDB_Helper		helper;
	private SQLiteDatabase	core_db = null;
	
	public CurrencyConverterDB(Context context) {
		helper = new CCDB_Helper(context);		
		if(core_db == null) {
			Log.d(TAG, "Open database...");
			core_db = helper.getWritableDatabase();
		}
	}	
	
	@Override
	public void finalize() {
		CloseDB();
	}
	
	public void CloseDB() {
		if(core_db != null) {
			if(core_db.isOpen()) {
				Log.d(TAG, "Close database...");
				core_db.close();
				core_db = null;
			}
		}
	}
	
	// get position
	public int GetCurrencyPosition(String name) {
		int		i;
		
		for(i=0; i<currency_name.length; i++) {
			if(name.compareToIgnoreCase(currency_name[i]) == 0) {
				return i;
			}
		}
		
		return 0;
	}
	
	// get rate by given currency name
	public double GetRates(String name) {
		Cursor	result = null;
		String	str_sql = "SELECT " + COL_CC_RATE + " FROM " + TABLE_CC_RATE + " WHERE " + COL_CC_NAME + "= ?";
		double	result_rate = 0;
		
		Log.d(TAG, "Get CC_NAME = " + name + " rate...");
		
		try {
			result = core_db.rawQuery(str_sql, new String[] { name });
			
			if(result.moveToFirst()) {
				result_rate = result.getDouble(0); 
			}
			
			result.close();
			result = null;
		} catch (Exception e) {
			Log.e(TAG, "GetRates:" + e.toString());
		}
		return result_rate;
	}
	
	// get all data from database
	public Cursor GetAllData() {
		String	str_sql = "SELECT * FROM " + TABLE_CC_RATE;
		
		try {
			return core_db.rawQuery(str_sql, null);
		} catch (Exception e) {
			Log.e(TAG, "GetAllData:" + e.toString());
		}
		return null;
	}
	
	// set rates by given currency name
	public void SetRates(String name, double rates) {
		String 	str_sql;
		
		Log.d(TAG, "Update CC_NAME = " + name + " CC_RATE = " + Double.toString(rates));
		
		try {
			// update new rate
			str_sql = "UPDATE " + TABLE_CC_RATE + " SET " +
			          COL_CC_RATE + "=" + Double.toString(rates) +
			          " WHERE " + COL_CC_NAME + "='" + name + "';";
			Log.d(TAG, "SetRates: SQL="+str_sql);
			core_db.execSQL(str_sql);			
		} catch (Exception e) {
			Log.e(TAG, "SetRates:" + e.toString());
		}
	}
}
