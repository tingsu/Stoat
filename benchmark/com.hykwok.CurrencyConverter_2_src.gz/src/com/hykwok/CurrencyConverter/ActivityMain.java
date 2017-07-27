/*
	Copyright 2010 - 2012 Kwok Ho Yin

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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;

public class ActivityMain extends Activity {
	
	// This variable is used for debug log (LogCat)
	private static final String TAG = "CC:ActivityMain";
	
	private static final char ITEM_CURRENCYA = 0;
	private static final char ITEM_CURRENCYB = 1;
	
	private static final int MENU_ABOUT = Menu.FIRST;
	private static final int MENU_PREFERENCE = Menu.FIRST + 1;
	
	private static final int DIALOG_ABOUT = 1;
	private static final int DIALOG_CURRENCYEDIT = 2;
	
	// Intent string for broadcasting
	private static final String ACTIVITY_TO_SERVICE_BROADCAST = "com.hykwok.action.CC_A_TO_S_BROADCAST";
	private static final String SERVICE_TO_ACTIVITY_BROADCAST = "com.hykwok.action.CC_S_TO_A_BROADCAST";
	
	// Intent key for broadcasting
	private static final String BROADCAST_KEY_ROAMING_OPT = "roaming";
	private static final String BROADCAST_KEY_LASTUPDATETIME = "lastupdatetime";
	
	// Preference keys
	private static final String KEY_PRECISION = "precision";
	private static final String KEY_LISTVIEW_OPT = "listviewclick";
	private static final String KEY_SERVICE_OPT = "service_option";
	private static final String KEY_ROAMING_OPT = "roaming_option";
	private static final String KEY_LASTUPDATETIME = "last_update_time";
	private static final String KEY_BASECURRENCY = "base_currency";
	private static final String KEY_SEL_CURRENCYA = "select_currencyA";
	private static final String KEY_SEL_CURRENCYB = "select_currencyB";	
	
	// backup keys
	private static final String KEY_BK_EDITTEXTA = "backup_edittext_A";
	
	// Message ID
	private static final int GUI_UPDATE_LISTVIEW = 0x100;
	
	private CurrencyConverterDB		m_DB;
	
	private SharedPreferences 		mPrefs;
	
	private Spinner[] 				m_spinner_Currency = { null, null };
	private EditText[] 				m_text_Currency = { null, null };
	private TextView				m_text_BaseCurrency;
	private CurrencyListAdapter		adapter_currencylist;
	private ListView				m_listview_rate;
	private CurrencyRateListAdapter	adapter_currencyratelist;
	
	/// initial variables for controls
	private String					m_Base_C = "";
	private String[]				m_Selected_C = { " ", " " };
	private int						m_current_active_currency = ITEM_CURRENCYA;
	private String					m_ListViewSelected_C = "";
	private String					m_SavedInstanceText = "";
	
	// preferences
	private long					m_lastupdatetime;
	private boolean					m_enableRoaming;
	
	// broadcast receiver
	private Broadcast_Receiver 		my_intent_receiver;
	
	private Intent 					online_intent;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate >>>>>");
    	
        super.onCreate(savedInstanceState);
        
        try {
	        // Note: Android system will select the best layout file 
	        // from the res folder.
	        // Read Android developers documents: 
	        // "Dev Guide"->"Framework Topics"->"Application Resources"->"Providing Resources" for more information
	        setContentView(R.layout.main);
	        
	        // initialize control variables
	        m_spinner_Currency[ITEM_CURRENCYA] = (Spinner) findViewById(R.id.SpinnerCurrencyA);
	        m_spinner_Currency[ITEM_CURRENCYB] = (Spinner) findViewById(R.id.SpinnerCurrencyB);
	        m_text_Currency[ITEM_CURRENCYA] = (EditText) findViewById(R.id.EditTextCurrencyA);
	        m_text_Currency[ITEM_CURRENCYB] = (EditText) findViewById(R.id.EditTextCurrencyB);
	        m_text_BaseCurrency = (TextView) findViewById(R.id.TextViewBaseCurrency);
	        m_listview_rate = (ListView) findViewById(R.id.ListViewRate);
	        
	        // get preferences
	        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	        
	        // use global database
	        m_DB = new CurrencyConverterDB(this);
	        
	        if(savedInstanceState != null) {
	        	// just restore edit text box firstly
	        	String text_CA = savedInstanceState.getString(KEY_BK_EDITTEXTA);
	        	
	        	Log.d(TAG, "txt_CA=" + text_CA);
	        	
	        	m_SavedInstanceText = text_CA;
	        	m_text_Currency[ITEM_CURRENCYA].setText(text_CA);	        	
	        }
	        
	        // create currency list adapter
	        adapter_currencylist = new CurrencyListAdapter(this, CurrencyConverterDB.currency_name, CurrencyConverterDB.currency_icon);
	        
	        for(int i=0; i<2; i++) {
		        // assign currency list adapter to spinner controls
		        m_spinner_Currency[i].setAdapter(adapter_currencylist);
		        
		        // set listener for spinner controls 
		        m_spinner_Currency[i].setOnItemSelectedListener(selectedListener_Currency);
		        
		        // set listener for edit text controls	        
		        m_text_Currency[i].setOnKeyListener(keyListener_Currency);
		        m_text_Currency[i].setOnFocusChangeListener(focusListener_Currency);
		        m_text_Currency[i].setOnEditorActionListener(EditorActionListener);
		        
		        // set input type for edit text controls
		        m_text_Currency[i].setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
		        // when users press enter key, close soft keyboard automatically
		        m_text_Currency[i].setImeOptions(EditorInfo.IME_ACTION_DONE);
	        }
	        
	        // set EditText control will be focused on itself when "enter" key is pressed
	        m_text_Currency[ITEM_CURRENCYA].setNextFocusDownId(R.id.EditTextCurrencyA);
	        m_text_Currency[ITEM_CURRENCYB].setNextFocusDownId(R.id.EditTextCurrencyB);
	        
	        // create currency rate list adapter
	        adapter_currencyratelist = new CurrencyRateListAdapter(this, CurrencyConverterDB.currency_longname, CurrencyConverterDB.currency_icon, m_DB.GetAllData());
	        
	        m_listview_rate.setAdapter(adapter_currencyratelist);
	        
	        // set listener for list view control
	        m_listview_rate.setOnItemClickListener(clickListener_listview);
	        m_listview_rate.setOnItemLongClickListener(longclickListener_listview);
	        
	        // display base currency
	        m_Base_C = mPrefs.getString(KEY_BASECURRENCY, CurrencyConverterDB.currency_name[0]);        
	        adapter_currencyratelist.SetBaseCurrencyIndex(m_DB.GetCurrencyPosition(m_Base_C));
	        m_text_BaseCurrency.setText(m_Base_C);
	        
        	m_Selected_C[ITEM_CURRENCYA] = mPrefs.getString(KEY_SEL_CURRENCYA, CurrencyConverterDB.currency_name[0]);
        	m_Selected_C[ITEM_CURRENCYB] = mPrefs.getString(KEY_SEL_CURRENCYB, CurrencyConverterDB.currency_name[0]);
	        
        	// register broadcast receiver
        	IntentFilter filter = new IntentFilter(SERVICE_TO_ACTIVITY_BROADCAST);
        	my_intent_receiver = new Broadcast_Receiver();
        	registerReceiver(my_intent_receiver, filter);
        } catch (Exception e) {
        	Log.e(TAG, "onCreate:" + e.toString());
        }
    	
        Log.d(TAG, "onCreate <<<<<");
    }
    
    @Override
    public void onStart() {
    	Log.d(TAG, "onStart >>>>>");
    	
    	super.onStart();
    	
    	try {
    		m_spinner_Currency[ITEM_CURRENCYA].setSelection(m_DB.GetCurrencyPosition(m_Selected_C[ITEM_CURRENCYA]));
    		m_spinner_Currency[ITEM_CURRENCYB].setSelection(m_DB.GetCurrencyPosition(m_Selected_C[ITEM_CURRENCYB]));
    		
    		// check service options
			Intent i = new Intent(this, CurrencyConverterService.class);
			
			m_lastupdatetime = mPrefs.getLong(KEY_LASTUPDATETIME, 0);
    		m_enableRoaming = mPrefs.getBoolean(KEY_ROAMING_OPT, false);
    		
    		// Update Preferences
	    	SharedPreferences.Editor editor = mPrefs.edit();
	    	
	    	String sel = mPrefs.getString(KEY_LISTVIEW_OPT, "1");
	    	String precision = mPrefs.getString(KEY_PRECISION, "2");
	    	
	    	editor.putString(KEY_LISTVIEW_OPT, sel);
	    	editor.putString(KEY_PRECISION, precision);
	    	
	    	editor.commit();
	    	
	    	// show last time update
    		TextView lastupdatetext = (TextView)findViewById(R.id.TextViewLastUpdate);
    		String sztime = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(new Date(m_lastupdatetime));
    		lastupdatetext.setText(sztime);
    		
    		// start background update service?
	    	if(mPrefs.getBoolean(KEY_SERVICE_OPT, false) == true) {
	    		i.putExtra(BROADCAST_KEY_ROAMING_OPT, m_enableRoaming);
	    		i.putExtra(BROADCAST_KEY_LASTUPDATETIME, m_lastupdatetime);	    		
	    		this.startService(i);
	    	} else {
	    		this.stopService(i);
	    	}
    	} catch (Exception e) {
    		Log.e(TAG, "Error:" + e.toString());
    	}
    	
    	Log.d(TAG, "onStart <<<<<");
    }
    
    @Override
    public void onRestart() {
    	Log.d(TAG, "onRestart >>>>>");
    	
    	super.onRestart();
    	
    	Log.d(TAG, "onRestart <<<<<");
    }
    
    @Override
    public void onResume() {
    	Log.d(TAG, "onResume >>>>>");
    	
    	super.onResume();
    	
    	Log.d(TAG, "onResume <<<<<");
    }
    
    @Override
    public void onPause() {
    	Log.d(TAG, "onPause >>>>>");
    	
    	super.onPause();
    	
    	Log.d(TAG, "onPause <<<<<");
    }
    
    @Override
    public void onStop() {
    	Log.d(TAG, "onStop >>>>>");
    	
    	super.onStop();
    	
    	Log.d(TAG, "onStop <<<<<");
    }
    
    @Override
    public void onDestroy() {
    	Log.d(TAG, "onDestroy >>>>>");
    	
    	super.onDestroy();
    	
    	// stop service
    	try {
	    	if(mPrefs.getBoolean(KEY_SERVICE_OPT, false) == true) {
	    		Intent i = new Intent(this, CurrencyConverterService.class);
	    		this.stopService(i);
	    	}
	    	
	    	// remove broadcast receiver
			unregisterReceiver(my_intent_receiver);
			
	    	// save preferences
	    	SharedPreferences.Editor editor = mPrefs.edit();
	    	
	    	editor.putString(KEY_SEL_CURRENCYA, m_Selected_C[ITEM_CURRENCYA]);
	    	editor.putString(KEY_SEL_CURRENCYB, m_Selected_C[ITEM_CURRENCYB]);
	    	editor.putString(KEY_BASECURRENCY, m_Base_C);
	    	editor.putLong(KEY_LASTUPDATETIME, m_lastupdatetime);    	
	    	
	    	editor.commit();
	    	
	    	if(m_DB != null) {
	    		m_DB.CloseDB();
	    		m_DB = null;
	    	}
    	} catch (Exception e) {
    		Log.e(TAG, "onDestroy" + e.toString());
    	}
    	
    	Log.d(TAG, "onDestroy <<<<<");
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	Log.d(TAG, "onSaveInstanceState >>>>>");
    	
    	// backup current textview content
    	String txt_CA = m_text_Currency[ITEM_CURRENCYA].getText().toString();
    	
    	Log.d(TAG, "txt_CA=" + txt_CA);
    	
    	outState.putString(KEY_BK_EDITTEXTA, txt_CA);
    	
    	Log.d(TAG, "onSaveInstanceState <<<<<");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuItem menu_item;
    	
    	super.onCreateOptionsMenu(menu);
    	// create menu
    	menu_item = menu.add(0, MENU_PREFERENCE, 0, R.string.szMenu_Preference);
    	menu_item.setIcon(android.R.drawable.ic_menu_preferences);
    	menu_item = menu.add(0, MENU_ABOUT, 1, R.string.szMenu_About);
    	menu_item.setIcon(android.R.drawable.ic_menu_info_details);    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
	    	case MENU_ABOUT:
	    		// show about dialog box
	    		showDialog(DIALOG_ABOUT);
	    		break;
	    		
	    	case MENU_PREFERENCE:
	    		// launch CurrencyPreference activity
	    		Intent i = new Intent(this, CurrencyPreferences.class);
	    		startActivity(i);
	    		break;
	    		
	    	default:
	    		break;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch(id) {
    		case DIALOG_ABOUT:
    			return new AboutDialog(this);
    			
    		case DIALOG_CURRENCYEDIT:
    			return createCurrencyEditDialog(this);
    		
    		default:
    			break;
    	}
    	
    	return null;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	switch(id) {
    		case DIALOG_CURRENCYEDIT:
    			EditText inputvalue = (EditText) dialog.findViewById(R.id.CurrencyEdit_Input);
    	    	CheckBox default_cb = (CheckBox) dialog.findViewById(R.id.CurrencyEdit_CheckBox);
    	    	
    	    	Log.d(TAG, "PrepareDialog: ListViewSelected="+m_ListViewSelected_C);
    	    	
    	    	if(m_ListViewSelected_C == m_Base_C) {
    	    		default_cb.setChecked(true);
    	    	} else {
    	    		default_cb.setChecked(false);
    	    	}
    	    	
    	    	// show currency rate
    	    	inputvalue.setText(adapter_currencyratelist.getDisplayString(m_DB.GetCurrencyPosition(m_ListViewSelected_C)));
    			break;
    			
    		default:
    			break;
    	}
    }
    
    // List view control listeners
    private OnItemClickListener clickListener_listview = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {			 
			try {
				String sel = mPrefs.getString(KEY_LISTVIEW_OPT, "1");
				
				if(sel.equalsIgnoreCase("0")) {
					m_ListViewSelected_C = CurrencyConverterDB.currency_name[position];
					
					Log.d(TAG, "Current selected item for listview is " + m_ListViewSelected_C);
					showDialog(DIALOG_CURRENCYEDIT);
				}
			} catch (Exception e) {
				Log.e(TAG, "onItemClick:" + e.toString());
			}
		}
    };
    
    private OnItemLongClickListener longclickListener_listview = new OnItemLongClickListener() {

		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			try {
				String sel = mPrefs.getString(KEY_LISTVIEW_OPT, "1");
				
				if(sel.equalsIgnoreCase("1")) {
					m_ListViewSelected_C = CurrencyConverterDB.currency_name[position];
				
					Log.d(TAG, "Current selected item for listview is " + m_ListViewSelected_C);
					showDialog(DIALOG_CURRENCYEDIT);
				}
			} catch (Exception e) {
				Log.e(TAG, "onItemLongClick:" + e.toString());
			}
			return true;
		}
    	
    };
    
    // Spinner controls listeners
    private OnItemSelectedListener selectedListener_Currency = new OnItemSelectedListener() {
    	int	nSelected = 0;
    	
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if(parent.getId() == R.id.SpinnerCurrencyA) {
				nSelected = ITEM_CURRENCYA;
			} else {
				nSelected = ITEM_CURRENCYB;
			}
			
			Log.d(TAG, "Current selected item for currency [" + Integer.toString(nSelected) + "] is " + CurrencyConverterDB.currency_name[position]);
			// update selected currency
			m_Selected_C[nSelected] = CurrencyConverterDB.currency_name[position];
			// update another one
			if(m_current_active_currency == ITEM_CURRENCYA) {
				updateCurrencyDisplay(ITEM_CURRENCYB);
			} else {
				updateCurrencyDisplay(ITEM_CURRENCYA);
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}    	
    };
    
    // EditText controls listeners
    private OnEditorActionListener EditorActionListener = new OnEditorActionListener() {
    	int nSelected = 0;
    	
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(v.getId() == R.id.EditTextCurrencyA) {
				nSelected = ITEM_CURRENCYA;
			} else {
				nSelected = ITEM_CURRENCYB;
			}
			
			Log.d(TAG, "Currency [" + Integer.toString(nSelected) + "] EditorAction receives: " + Integer.toString(actionId) + ".");
			
			if(nSelected == ITEM_CURRENCYA) {
				// change current selected currency and update another one 
				updateCurrencyDisplay(ITEM_CURRENCYB);
			} else {
				// change current selected currency and update another one 
				updateCurrencyDisplay(ITEM_CURRENCYA);
			}
			
			return false;
		}
    };
    
    private OnKeyListener keyListener_Currency = new OnKeyListener() {
    	int nSelected = 0;
    	
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			boolean ret_flag = false;
			
			if(v.getId() == R.id.EditTextCurrencyA) {
				nSelected = ITEM_CURRENCYA;
			} else {
				nSelected = ITEM_CURRENCYB;
			}
			
			if(event.getAction() == KeyEvent.ACTION_DOWN) {
				Log.d(TAG, "Currency [" + Integer.toString(nSelected) + "] EditText receives key: " + Integer.toString(keyCode) + ".");
				
				switch(keyCode) {
					case KeyEvent.KEYCODE_ENTER:
						if(nSelected == ITEM_CURRENCYA) {
							// change current selected currency and update another one 
							updateCurrencyDisplay(ITEM_CURRENCYB);							
						} else {
							// change current selected currency and update another one 
							updateCurrencyDisplay(ITEM_CURRENCYA);
						}
						ret_flag = true;
						break;
						
					default:
						break;
				}
			}
			
			return ret_flag;
		}    	
    };
    
    private OnFocusChangeListener focusListener_Currency = new OnFocusChangeListener() {
    	int	nSelected = 0, len;    	
    	String	m_current_input_value = "";

		public void onFocusChange(View v, boolean hasFocus) {
			if(v.getId() == R.id.EditTextCurrencyA) {
				nSelected = ITEM_CURRENCYA;
			} else {
				nSelected = ITEM_CURRENCYB;
			}
			
			m_current_input_value = m_text_Currency[nSelected].getText().toString();
			
			if(hasFocus) {
				Log.d(TAG, "Currency [" + Integer.toString(nSelected) + "] EditText on focus. text=" + m_current_input_value);
				len = m_current_input_value.length();
				if(len > 0) {
					if(m_current_input_value.compareTo(m_SavedInstanceText) == 0) {
						m_SavedInstanceText = "";
						// move the cursor to the end of the text
						m_text_Currency[nSelected].setSelection(len);
					} else {
						m_current_input_value = "";
						m_text_Currency[nSelected].setText(m_current_input_value);
					}
				}
				m_current_active_currency = nSelected;
			} else {
				Log.d(TAG, "Currency [" + Integer.toString(nSelected) + "] EditText loss focus. text=" + m_current_input_value);
				if(m_current_input_value.length() == 0) {
					m_current_input_value = formCurrencyDisplay(0);
					m_text_Currency[nSelected].setText(m_current_input_value);
				}
			}
		}
    };
    
    // Convert double to string for currency
    private String formCurrencyDisplay(double value) {
    	String format = "";
    	
    	try {
	    	switch(Integer.parseInt(mPrefs.getString(KEY_PRECISION, "2"))) {
		    	case 0:
		    		format = "%.0f";
		    		break;
		    		
		    	case 1:
		    		format = "%.1f";
		    		break;
		    		
		    	case 3:
		    		format = "%.3f";
		    		break;
		    		
		    	default:
		    		format = "%.2f";
		    		break;
	    	}
    	} catch (Exception e) {
    		Log.e(TAG, "formCurrencyDisplay:" + e.toString());
    		format = "%.2f";
    	}
    	
    	String result = String.format(Locale.US, format, value);
    	
    	return result;
    }
    
    // update currency display
    private void updateCurrencyDisplay(char toupdateitem) {
    	double	rate_CA = 0, rate_CB = 0;
    	double  in_CA = 0, in_CB = 0;
    	double	result = 0;
    	String  str_CA, str_CB, str_result;
    	
    	// get currency rates
    	rate_CA = m_DB.GetRates(m_Selected_C[ITEM_CURRENCYA]);
    	rate_CB = m_DB.GetRates(m_Selected_C[ITEM_CURRENCYB]);
    	
    	// get input values
    	str_CA = m_text_Currency[ITEM_CURRENCYA].getText().toString();
    	str_CB = m_text_Currency[ITEM_CURRENCYB].getText().toString();
    	
    	try {
	    	in_CA = Double.valueOf(str_CA);
    	} catch(Exception e) {
    		Log.e(TAG, "updateCurrencyDisplay:"+e.toString());
    		in_CA = 0;
    	}
	    	
    	try {
	    	in_CB = Double.valueOf(str_CB);
    	} catch(Exception e) {
    		Log.e(TAG, "updateCurrencyDisplay:"+e.toString());
    		in_CB = 0;
    	}
    	
    	Log.d(TAG, "CA: in = " + in_CA + " rate = " + rate_CA);
    	Log.d(TAG, "CB: in = " + in_CB + " rate = " + rate_CB);
    	
    	if(toupdateitem == ITEM_CURRENCYA) {
    		if(rate_CB == 0) {
    			result = 0;
    		} else {
    			result = rate_CA / rate_CB * in_CB;
    		}
    		
    		str_result = formCurrencyDisplay(result);
    		m_text_Currency[ITEM_CURRENCYA].setText(str_result);    		
    	} else {
    		if(rate_CA == 0) {
    			result = 0;
    		} else {
    			result = rate_CB / rate_CA * in_CA;
    		}
    		
    		str_result = formCurrencyDisplay(result);
    		m_text_Currency[ITEM_CURRENCYB].setText(str_result);
    	}
    }
    
    // create currency edit dialog box
    private Dialog createCurrencyEditDialog(Context context) {
    	Log.d(TAG, "----- createCurrencyEditDialog -----");
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	LayoutInflater inflater = LayoutInflater.from(context);
    	
    	// use currencyeditdialog box layout for content
    	final View ContentView = inflater.inflate(R.layout.currencyeditdialog, null);
    	builder.setView(ContentView);
    	
    	builder.setTitle(R.string.CurrencyEditTitle);
    	builder.setIcon(android.R.drawable.ic_menu_more);
    	
    	CheckBox default_cb = (CheckBox) ContentView.findViewById(R.id.CurrencyEdit_CheckBox);
    	EditText inputvalue = (EditText) ContentView.findViewById(R.id.CurrencyEdit_Input);
    	
    	// set edit box control
    	inputvalue.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
    	
    	// check box button
    	default_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				String result;
				
				EditText inputvalue = (EditText) ContentView.findViewById(R.id.CurrencyEdit_Input);
				
				// set the input box can be edited or not
				if(isChecked) {
					inputvalue.setEnabled(false);
					result = String.format(Locale.US, "%.3f", 1.0);
					inputvalue.setInputType(InputType.TYPE_NULL);
				} else{
					inputvalue.setEnabled(true);
					result = adapter_currencyratelist.getDisplayString(m_DB.GetCurrencyPosition(m_ListViewSelected_C));
					inputvalue.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
				}
				
				// show currency rate
    	    	inputvalue.setText(result);
			}    		
    	});
    	
    	// OK button
    	builder.setPositiveButton(context.getText(R.string.Dialog_OK), 
    	new DialogInterface.OnClickListener() {
    		double rate_input, rate_sel, rate_base;
    		
    		// handle OK button click 
			public void onClick(DialogInterface dialog, int which) {
				EditText inputvalue = (EditText) ContentView.findViewById(R.id.CurrencyEdit_Input);
		    	CheckBox default_cb = (CheckBox) ContentView.findViewById(R.id.CurrencyEdit_CheckBox);				
				
		    	Log.d(TAG, "CurrencyEditDialog: PositiveButtonClick > ListViewSelected="+m_ListViewSelected_C);
				
				try {
					String rate_selected = inputvalue.getText().toString();
					
					if(default_cb.isChecked() == false) {
						// if default currency rate is set, do not calculate the new rate
						rate_input = Double.valueOf(rate_selected);
						
						// convert currency rate to the rate where the base currency is EUR
						if(m_ListViewSelected_C == CurrencyConverterDB.currency_name[0]) {
							// Users modify EUR rate, but we should modify the rate of the current base currency in the database
							// because all rates in database are based on EUR 
							rate_base = 1.0 / rate_input;
							
							// update database
							m_DB.SetRates(m_Base_C, rate_base);														
						} else {
							rate_base = adapter_currencyratelist.getCurrencyRate(m_DB.GetCurrencyPosition(m_Base_C));
							rate_sel = rate_input * rate_base;
							
							// update database
							m_DB.SetRates(m_ListViewSelected_C, rate_sel);
						}
						
						// update currency rate list
						adapter_currencyratelist.updateCurrencyRate();
						adapter_currencyratelist.SetBaseCurrencyIndex(m_DB.GetCurrencyPosition(m_Base_C));
					}
				} catch (Exception e) {
					Log.e(TAG, "showCurrencyEditDialog:" + e.toString());
				}
				
				if(default_cb.isChecked()) {
					// update base currency
					m_Base_C = m_ListViewSelected_C;
					adapter_currencyratelist.SetBaseCurrencyIndex(m_DB.GetCurrencyPosition(m_Base_C));
					m_text_BaseCurrency.setText(m_Base_C);
				}
				
				// refresh list view
				Log.d(TAG, "----- refresh listview -----");
				adapter_currencyratelist.notifyDataSetChanged();
			}    		
    	});
    	
    	// Cancel button
    	builder.setNegativeButton(context.getText(R.string.Dialog_Cancel), 
    	new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// nothing to do for cancel button
			}    		
    	});
    	
    	return builder.create();
    }
    
    // send data to service
    void sendSettingToService() {
		Intent	intent = new Intent(ACTIVITY_TO_SERVICE_BROADCAST);
		
		intent.putExtra(BROADCAST_KEY_ROAMING_OPT, m_enableRoaming);
		intent.putExtra(BROADCAST_KEY_LASTUPDATETIME, m_lastupdatetime);
		
		Log.d(TAG, "send data to service >>>>>");
		sendBroadcast(intent);
	}
    
    // receive data from service
    public class Broadcast_Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// receive intent from service
			Log.d(TAG, "receive data from service >>>>>");
			
			try {
				m_lastupdatetime = intent.getExtras().getLong(BROADCAST_KEY_LASTUPDATETIME, 0);
				
				// copy online data send it to a thread in order to update database
				online_intent = (Intent) intent.clone();
				
				// start a new thread to handle database update
				Thread thread = new Thread(mTask);
				thread.start();
			} catch (Exception e) {
				Log.e(TAG, "Broadcast_Receiver:" + e.toString());
			}
		}
	}
    
    // background thread to update database
    private Runnable mTask = new Runnable() {
    	
    	public void run() {
    		double	rate;
    		
    		try {
    			// get online data from service and update database
    			for(int i=0; i<CurrencyConverterDB.currency_name.length; i++) {
    				rate = online_intent.getExtras().getDouble(CurrencyConverterDB.currency_name[i]);
    				
    				if(rate > 0) {
    					m_DB.SetRates(CurrencyConverterDB.currency_name[i], rate);
    				}
    			}
    			
    			// send message to activity to update listview
    			ActivityMain.this.objHandler.sendEmptyMessage(GUI_UPDATE_LISTVIEW);
    		} catch (Exception e) {
    			Log.e(TAG, "mTask:" + e.toString());
    		}
    	}
    };
    
    // receive message from other threads
    private Handler objHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    			case GUI_UPDATE_LISTVIEW:
    				// refresh list view
    				Log.d(TAG, "----- refresh listview -----");
    				// update currency rate list
					adapter_currencyratelist.updateCurrencyRate();
					adapter_currencyratelist.SetBaseCurrencyIndex(m_DB.GetCurrencyPosition(m_Base_C));
    				adapter_currencyratelist.notifyDataSetChanged();
					// update last update time message
					TextView lastupdatetext = (TextView)findViewById(R.id.TextViewLastUpdate);
					String sztime = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(new Date(m_lastupdatetime));
					lastupdatetext.setText(sztime);
    				break;
    		}
    		
    		super.handleMessage(msg);
    	}    	
    };
}
