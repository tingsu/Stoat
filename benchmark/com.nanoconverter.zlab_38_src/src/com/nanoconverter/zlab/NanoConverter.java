package com.nanoconverter.zlab;

import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;
import android.widget.ToggleButton;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

public class NanoConverter extends TabActivity {
 /** Called when the activity is first created. */
	public static NanoConverter mContext = null;
	public static final int LENGTH_LONG = 10;
	
	public EditText text,amountmoney;
	
	int count = 37;
	
	String[] sa = { "USD", "EUR", "CHF", "GBP", "JPY", "UAH", "RUB", "MDL", "BYR", "PLN", "LTL", "LVL", "AZN", "AUD", "AMD", "BGN", "BRL", "HUF", "DKK", "INR", "KZT", "CAD", "KGS", "CNY", "NOK", "RON", "XDR", "SGD", "TJS", "TRY", "TMT", "UZS", "CZK", "SEK", "ZAR", "KRW", "FOO" };
	EditText[] course = new EditText[count];
	EditText[] courserate = new EditText[count];
	RadioButton[] from = new RadioButton[count];
	RadioButton[] to = new RadioButton[count];
	String[] moneycourse = new String[count];
	LinearLayout[] moneycl = new LinearLayout[count];
	View[] moneycls = new View[count];
	String coursebydefaultis = "1";
	boolean[] mactive = new boolean[count];
	boolean reverserates = false;
	public String handlBankID;

	public SharedPreferences nanostore_shared;
	SharedPreferences.Editor nanostore_shared_editor;
	
	Button buttonbank;
	Button buttonloadfrom;
	Button buttonrefresh;
	ToggleButton buttoninverse;

    int checkBank;
    int checkUPDT;
    int checkCurd;
    String BANK_ID;

	double curentfromcourserate = 1.00;
	double curenttocourserate = 1.00;

	String ListCurPreference,ListBankPreference,listUpdate,leftsideselected,rightsideselected;

    ProgressDialog progressDialog;

    Handler handlerCloseThreadforce = new Handler() {@Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDialog.dismiss();}
    };
    Handler handlerCloseThread = new Handler() {@Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (Integer.parseInt(listUpdate) == 2){handlerCloseThreadforce.sendEmptyMessage(0);}}
    };
    Handler handlerERRThread = new Handler() {@Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.checkinternet), Toast.LENGTH_SHORT);
            LinearLayout ToastView = (LinearLayout) toast.getView();
            ImageView imageWorld = new ImageView(getApplicationContext());
            imageWorld.setImageResource(R.drawable.err);
            ToastView.addView(imageWorld, 0);
            toast.show();
        }
    };
    Handler handlerERRdevzero = new Handler() {@Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.devzero), Toast.LENGTH_SHORT);
            LinearLayout ToastView = (LinearLayout) toast.getView();
            ImageView imageWorld = new ImageView(getApplicationContext());
            imageWorld.setImageResource(R.drawable.err);
            ToastView.addView(imageWorld, 0);
            toast.show();
        }
    };
    Handler handlerERRdevnull = new Handler() {@Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.devnull), Toast.LENGTH_SHORT);
            LinearLayout ToastView = (LinearLayout) toast.getView();
            ImageView imageWorld = new ImageView(getApplicationContext());
            imageWorld.setImageResource(R.drawable.err);
            ToastView.addView(imageWorld, 0);
            toast.show();
        }
    };
    Handler handlerGOODtoast = new Handler() {@Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };
    Handler handlerGOODThread = new Handler() {@Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            Toast toast3 = Toast.makeText(getApplicationContext(), getString(R.string.updatecomplete), Toast.LENGTH_SHORT);
            LinearLayout ToastView = (LinearLayout) toast3.getView();
            ImageView imageWorld = new ImageView(getApplicationContext());
            imageWorld.setImageResource(R.drawable.good);
            ToastView.addView(imageWorld, 0);
            toast3.show();
            
            UpdateRates();
            
            SimpleDateFormat dateis = new SimpleDateFormat("dd.MM.yyyy");
       	   	String curentDate = dateis.format(new Date());
       	   	nanostore_shared_editor.putString("LastUpdateDate"+handlBankID, curentDate.toString());
       	   	nanostore_shared_editor.commit();

       	   	nanostore_shared_editor.putString("LastUpdateMs"+handlBankID, String.valueOf(System.currentTimeMillis()));
    	   	nanostore_shared_editor.commit();

       	   	//
       	    if (checkBank == 1) {
       	    	for (int i=0;i<count;i++ ){
       	    		course[i].setText(courserate[i].getText().toString());
       	        	}
       	    }
       	    course[36].setText(courserate[36].getText().toString());

       	    StringBuilder sb = new StringBuilder();
       	    for (int i = 0; i < count; i++) {
       	        sb.append(course[i].getText().toString()).append(",");
       	        }

       	    	nanostore_shared_editor.putString("rates_from_"+handlBankID, sb.toString());
       	    	nanostore_shared_editor.commit();

       			for (int i=0;i<count;i++ ){
       	       		if (from[i].isChecked()){
       	       			nanostore_shared_editor.putString("fromStore", String.valueOf(i));
       	       			nanostore_shared_editor.commit();}
       	       		 if (to[i].isChecked()){
       	       			nanostore_shared_editor.putString("toStore", String.valueOf(i));
       	       			nanostore_shared_editor.commit();}}
       	   	//
        }
    };

 @Override

  public void onCreate(Bundle savedInstanceState) {

	 mContext = this;
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     

     /* Строим ТАБЫ */
     Resources res = getResources();
     TabHost tabHost = getTabHost();

     TabSpec Converter = tabHost.newTabSpec("tab1").setIndicator(getString(R.string.conversetab),
    		 res.getDrawable(R.drawable.ic_tab_convert));

     TabSpec OnlineCourse = tabHost.newTabSpec("tab2").setIndicator(getString(R.string.Coursestab),
    		 res.getDrawable(R.drawable.ic_tab_courses));

     tabHost.addTab(Converter.setContent(R.id.tab1));
     tabHost.addTab(OnlineCourse.setContent(R.id.tab2));
     /* Строим ТАБЫ */
     
     buttonrefresh = (Button) findViewById(R.id.UpdateButton);
 	 buttonbank = (Button) findViewById(R.id.BankChangeButton);
 	 buttonloadfrom = (Button) findViewById(R.id.CopyFromBankButton);
 	 buttoninverse = (ToggleButton) findViewById(R.id.InverseStateButton);
     text = (EditText) findViewById(R.id.ValueFrom);
     amountmoney = (EditText) findViewById(R.id.ValueResult);
     amountmoney.setKeyListener(null);
     
     nanostore_shared = getSharedPreferences("nanostore_shared", 0);
     nanostore_shared_editor = nanostore_shared.edit();
     
     getID();

     text.addTextChangedListener(new TextWatcher(){
		public void afterTextChanged(Editable arg0) {
			UpdateResultsHandler();
		}
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
    	 
     }
     );

     getPrefs();
     
     String datestored = nanostore_shared.getString("LastUpdateDate"+BANK_ID, "7777");
     SimpleDateFormat dateis = new SimpleDateFormat("dd.MM.yyyy");
	 String curentDate = dateis.format(new Date());
	
	/* autoupdate */
	      if (checkBank != 1 && checkUPDT != 0){
	    		 if (checkUPDT == 1) {NanoConverter.mContext.processThread();}
	    	else if (checkUPDT == 2) {NanoConverter.mContext.processThreadforce();}
	    	else if (checkUPDT == 3) {if (!curentDate.toString().equals(datestored)){NanoConverter.mContext.processThread();}}}
	/* autoupdate */
 }

 void getID() {for (int i=0;i<count;i++ ){
	int resID = getResources().getIdentifier("Course" + sa[i],"id", getPackageName());
	course[i] = (EditText)findViewById(resID);
		resID = getResources().getIdentifier("Course" + sa[i] + "rate","id", getPackageName());
	courserate[i] = (EditText)findViewById(resID);}
 }
 
 void getRadio() {for (int i=0;i<count;i++ ){
	int resID = getResources().getIdentifier("from" + sa[i],"id", getPackageName());
 	from[i] = (RadioButton)findViewById(resID);
 		resID = getResources().getIdentifier("to" + sa[i],"id", getPackageName());
 	to[i] = (RadioButton)findViewById(resID);}
 
 String fromStore = nanostore_shared.getString("fromStore", "0");
 String toStore = nanostore_shared.getString("toStore", "0");

 for (int i=0;i<count;i++ ){
		if (fromStore.equals(String.valueOf(i))){
			from[i].setChecked(true);}}
 
  for (int i=0;i<count;i++ ){
		if (toStore.equals(String.valueOf(i))){
			to[i].setChecked(true);}}
 }
 
 void setkey() {for (int i=0;i<count;i++ ){
	 from[i].setOnClickListener(new OnClickListener() {public void onClick(View v) {UpdateResultsHandler();}});
	 to[i].setOnClickListener(new OnClickListener() {public void onClick(View v) {UpdateResultsHandler();}});}	 
 }
 
 void getresID2() {for (int i=0;i<count;i++ ){
	int resID = getResources().getIdentifier(sa[i]+"cl","id", getPackageName());
	moneycl[i] = (LinearLayout)findViewById(resID);
		resID = getResources().getIdentifier(sa[i]+"cls","id", getPackageName());
	moneycls[i] = (View)findViewById(resID);}
 }
 
 protected void onResume() {
	 getRadio();
	 setkey();
	 getresID2();
	 getPrefs();
	 if (reverserates){
		 buttoninverse.setChecked(true);
		 } else {
			 buttoninverse.setChecked(false);
		 }
	 
	 for (int i=0;i<count;i++ ){
		 if (mactive[i] == false){from[i].setVisibility(View.GONE);to[i].setVisibility(View.GONE);moneycls[i].setVisibility(View.GONE);moneycl[i].setVisibility(View.GONE);}
		 if (mactive[i] == true){from[i].setVisibility(View.VISIBLE);to[i].setVisibility(View.VISIBLE);moneycl[i].setVisibility(View.VISIBLE);moneycls[i].setVisibility(View.VISIBLE);}
	 }

	      if (checkBank == 1){TurnOFFrates();buttonloadfrom.setVisibility(View.VISIBLE);} else {TurnONrates();buttonloadfrom.setVisibility(View.GONE);}

	      String[] separated = nanostore_shared.getString("rates_from_"+BANK_ID, "7777").split(",");

	      if (separated[0].equals("7777") && checkBank != 1 ){
	    	  /* if never updated, update */
	    	  NanoConverter.mContext.processThread();
	      } else {
	      for (int i=0;i<count;i++ ){try {course[i].setText(separated[i]);} catch (Exception ioe) {}
	      }}
	      courserate[36].setText(course[36].getText().toString());
	      UpdateRates();

	      super.onResume();
 }
 /* MENU */
 @Override
 public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.mainmenu, menu);
     return true;
 }
 @Override
 public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
     case R.id.quit:{
    	 finish();
    	 return true;}
     case R.id.settings:{
    	 Intent settingsActivity = new Intent(getBaseContext(),
    			 com.nanoconverter.zlab.Preferences.class);
    	 startActivity(settingsActivity);
    	 return true;}
     default:
         return super.onOptionsItemSelected(item);
     }
 }
 
 /* Сохраняем конфиг при выходе */
 @Override
 protected void onStop(){
    super.onStop();
    
    if (checkBank == 1) {
    	for (int i=0;i<count;i++ ){
    		course[i].setText(courserate[i].getText().toString());
        	}
    }
    course[36].setText(courserate[36].getText().toString());
    
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
        sb.append(course[i].getText().toString()).append(",");
        }
   		nanostore_shared_editor.putString("rates_from_"+BANK_ID, sb.toString());
    	nanostore_shared_editor.commit();

		for (int i=0;i<count;i++ ){
       		if (from[i].isChecked()){
       			nanostore_shared_editor.putString("fromStore", String.valueOf(i));
       			nanostore_shared_editor.commit();}
       		 if (to[i].isChecked()){
       			nanostore_shared_editor.putString("toStore", String.valueOf(i));
       			nanostore_shared_editor.commit();}}
 }

void processThread() {
    
	Toast toast2 = Toast.makeText(getApplicationContext(), getString(R.string.updateinprogress), Toast.LENGTH_SHORT);
    LinearLayout ToastView = (LinearLayout) toast2.getView();
    ImageView imageWorld = new ImageView(getApplicationContext());
    imageWorld.setImageResource(R.drawable.dwnld);
    ToastView.addView(imageWorld, 0);
    toast2.show();

    bankIDcheck();
 }

void processThreadforce() {
	progressDialog = ProgressDialog.show(com.nanoconverter.zlab.NanoConverter.mContext, getString(R.string.wait), getString(R.string.updateinprogress));

	new Thread() {
		public void run() {
		killLongForce();
		}
	}.start();
	bankIDcheck();
 }

void bankIDcheck() {
	if (BANK_ID == "CBR"){
        new Thread() {
            public void run() {
           	 runLongProcessCBR();
           	 handlerGOODtoast.sendEmptyMessage(0);
           	 handlerCloseThread.sendEmptyMessage(0);
            }
        }.start();}
   	if (BANK_ID == "NBU"){
   		new Thread() {
   	         public void run() {
   	        	 runLongProcessNBU();
   	        	 handlerGOODtoast.sendEmptyMessage(0);
   	        	 handlerCloseThread.sendEmptyMessage(0);
   	         }
   	     }.start();
   	}
   	if (BANK_ID == "NBRB"){
   		new Thread() {
  	         public void run() {
  	        	 runLongProcessNBRB();
  	        	 handlerGOODtoast.sendEmptyMessage(0);
  	        	 handlerCloseThread.sendEmptyMessage(0);
  	         }
  	     }.start();
  	}
   	if (BANK_ID == "BNM"){
   		new Thread() {
  	         public void run() {
  	        	 runLongProcessBNM();
  	        	 handlerGOODtoast.sendEmptyMessage(0);
  	        	 handlerCloseThread.sendEmptyMessage(0);
  	         }
  	     }.start();
  	}
   	if (BANK_ID == "AZ"){
   		new Thread() {
  	         public void run() {
  	        	 runLongProcessAZ();
  	        	 handlerGOODtoast.sendEmptyMessage(0);
  	        	 handlerCloseThread.sendEmptyMessage(0);
  	         }
  	     }.start();
  	}
   	if (BANK_ID == "ECB"){
   		new Thread() {
  	         public void run() {
  	        	 runLongProcessECB();
  	        	 handlerGOODtoast.sendEmptyMessage(0);
  	        	 handlerCloseThread.sendEmptyMessage(0);
  	         }
  	     }.start();
  	}
   	if (BANK_ID == "FOREX") {
   		new Thread() {
   			public void run() {
   				runLongProcessFOREX();
 	        	handlerGOODtoast.sendEmptyMessage(0);
   				handlerCloseThread.sendEmptyMessage(0);
   			}
 	     }.start();
   	}
}

void killLongForce() {
	try {
	Thread.sleep(10*1000);
	handlerCloseThreadforce.sendEmptyMessage(0);
	} catch (Exception ioe) {
	    	}
}
 public void runLongProcessCBR() {
	
  try {
	  	 boolean sec = true;
	  		Document doc = null;
	  		
	  	    try {
	  	    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     	   	DocumentBuilder db = dbf.newDocumentBuilder();	
	  	     	   	URL url = new URL("http://www.cbr.ru/scripts/XML_daily.asp");
	  	     	   	doc = db.parse(new InputSource(url.openStream()));
	  		     } catch (Exception ioe) {
	  		    	 sec = false;
	  		    	handlerERRThread.sendEmptyMessage(0);
	  		    	 }
	  	if (sec == true){
	  		try {
	  		NodeList charlist = doc.getElementsByTagName("CharCode");
	  		NodeList nomlist = doc.getElementsByTagName("Nominal");
	  		NodeList list = doc.getElementsByTagName("Value");
	  		   int len = list.getLength();
	  		
	  		   String[] sas = { "USD", "EUR", "CHF", "GBP", "JPY", "UAH", "RUB", "MDL", "BYR", "PLN", "LTL", "LVL", "AZN", "AUD", "AMD", "BGN", "BRL", "HUF", "DKK", "INR", "KZT", "CAD", "KGS", "CNY", "NOK", "RON", "XDR", "SGD", "TJS", "TRY", "TMT", "UZS", "CZK", "SEK", "ZAR", "KRW" };
	  		   String[] coursenew = new String[36];
	  		   for(int i = 0; i<36; i++){coursenew[i] = "0";}
	  		   
	  		   for(int i = 0; i<len; i++)
	  		   {
	  			   /* ID */
	  			   	Node ch= charlist.item(i);
	  			    ch.getNodeValue();
	  		   		ch.getFirstChild().getNodeValue();
	  		   		String chStr  = ch.getFirstChild().getNodeValue();
	  		   		int[] chpos = new int[36];
	  		   		for(int j = 0; j<36; j++){chpos[j] = 7777;}
	  		   		for(int j = 0; j<36; j++){
	  		   			if (chStr.equals(sas[j])) {
	  		   			chpos[j] = i;
	  		   			}
	  		   		}
	  		   		
	  		   		/* rate */
		  		   	Node r= nomlist.item(i);
	  			    r.getNodeValue();
	  		   		r.getFirstChild().getNodeValue();
	  		   		String nStr  = r.getFirstChild().getNodeValue();
	  		   		int[] nd = new int[36];
	  		   		for(int j = 0; j<36; j++){nd[j] = 1;}
	  		   		for(int j = 0; j<36; j++){
	  		   			if (chStr.equals(sas[j])) {
	  		   			nd[j] = Integer.parseInt(nStr);
	  		   			}
	  		   		}
	  			   
	  			   /* data */
	  			    Node n= list.item(i);
	  		   		n.getNodeValue();
	  		   		n.getFirstChild().getNodeValue();
	  		   		String dateCurrencyStr  = n.getFirstChild().getNodeValue();
  		   		
	  		   		coursenew[6] = "1.00";
	  		   		
	  		   		double coursetrue;
	  		   		
		  		   	for(int j = 0; j<36; j++){
		  		   	if(i == chpos[j])	{coursenew[j] = dateCurrencyStr.replace(",", ".");
			   			coursetrue = ( Double.parseDouble(coursenew[j]) / nd[j] );
			   			coursenew[j] = (Double.toString(coursetrue));
			   			}
	  		   		}
		  		   	
		  		  if (i == len-1){
		  			  for(int j = 0; j<36; j++){
			  		   	 course[j].setText(coursenew[j]);
				   			}
		  		  }
	  		   }
	  		 
	  		 handlBankID="CBR";
	  		 handlerGOODThread.sendEmptyMessage(0);
	  		 
	  			} catch (Exception ioe) {
	  		    	 //donothing
	  		    	}
	  	} else { }	  	 
     } catch (Exception ioe) {
    	 
     }
 }
 
 public void runLongProcessNBU() {

	  try {
		  	 boolean sec = true;
		  		Document doc = null;
		  		
		  	    try {
		  	    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     	   	DocumentBuilder db = dbf.newDocumentBuilder();	
		  	     	   	URL url = new URL("http://pfsoft.com.ua/service/currency/");
		  	     	   	doc = db.parse(new InputSource(url.openStream()));
		  		     } catch (Exception ioe) {
		  		    	 sec = false;
		  		    	handlerERRThread.sendEmptyMessage(0);
		  		    	 }
		  	if (sec == true){
		  		try {
			  		NodeList charlist = doc.getElementsByTagName("CharCode");
			  		NodeList nomlist = doc.getElementsByTagName("Nominal");
			  		NodeList list = doc.getElementsByTagName("Value");
			  		   int len = list.getLength();
			  		
			  		   String[] sas = { "USD", "EUR", "CHF", "GBP", "JPY", "UAH", "RUB", "MDL", "BYR", "PLN", "LTL", "LVL", "AZM", "AUD", "AMD", "BGN", "BRL", "HUF", "DKK", "INR", "KZT", "CAD", "KGS", "CNY", "NOK", "RON", "XDR", "SGD", "TJS", "TRY", "TMT", "UZS", "CZK", "SEK", "ZAR", "KRW" };
			  		   String[] coursenew = new String[36];
			  		   for(int i = 0; i<36; i++){coursenew[i] = "0";}
			  		   
			  		   for(int i = 0; i<len; i++)
			  		   {
			  			   /* ID */
			  			   	Node ch= charlist.item(i);
			  			    ch.getNodeValue();
			  		   		ch.getFirstChild().getNodeValue();
			  		   		String chStr  = ch.getFirstChild().getNodeValue();
			  		   		int[] chpos = new int[36];
			  		   		for(int j = 0; j<36; j++){chpos[j] = 7777;}
			  		   		for(int j = 0; j<36; j++){
			  		   			if (chStr.equals(sas[j])) {
			  		   			chpos[j] = i;
			  		   			}
			  		   		}
			  		   		
			  		   		/* rate */
				  		   	Node r= nomlist.item(i);
			  			    r.getNodeValue();
			  		   		r.getFirstChild().getNodeValue();
			  		   		String nStr  = r.getFirstChild().getNodeValue();
			  		   		int[] nd = new int[36];
			  		   		for(int j = 0; j<36; j++){nd[j] = 1;}
			  		   		for(int j = 0; j<36; j++){
			  		   			if (chStr.equals(sas[j])) {
			  		   			nd[j] = Integer.parseInt(nStr);
			  		   			}
			  		   		}
			  			   
			  			   /* data */
			  			    Node n= list.item(i);
			  		   		n.getNodeValue();
			  		   		n.getFirstChild().getNodeValue();
			  		   		String dateCurrencyStr  = n.getFirstChild().getNodeValue();
		  		   		
			  		   		coursenew[5] = "1.00";
			  		   		
			  		   		double coursetrue;
			  		   		
				  		   	for(int j = 0; j<36; j++){
				  		   	if(i == chpos[j])	{coursenew[j] = dateCurrencyStr.replace(",", ".");
					   			coursetrue = ( Double.parseDouble(coursenew[j]) / nd[j] );
					   			coursenew[j] = (Double.toString(coursetrue));
					   			}
			  		   		}
				  		   	
				  		  if (i == len-1){
				  			  for(int j = 0; j<36; j++){
					  		   	 course[j].setText(coursenew[j]);
						   			}
				  		  }
			  		   }

				  	 handlBankID="NBU";
			  		 handlerGOODThread.sendEmptyMessage(0);
			  		 
			  			} catch (Exception ioe) {
		  		    	 //donothing
		  		    	}
		  	} else { }
	     } catch (Exception ioe) {
	    	 
	     }
	 }
 
 public void runLongProcessBNM() {

	  try {
		  	 boolean sec = true;
		  		Document doc = null;
		  		
		  	    try {
		  	    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     	   	DocumentBuilder db = dbf.newDocumentBuilder();
		  	     	   	
		  	     	   	SimpleDateFormat dateis = new SimpleDateFormat("dd.MM.yyyy"); // "yyyyMMdd_HHmmss"
		  	     	   	String curentDate = dateis.format(new Date());
		  	     	   	String uri = "http://www.bnm.md/md/official_exchange_rates?get_xml=1&date="+curentDate;
		  	     	   	
		  	     	   	URL url = new URL(uri);
		  	     	   	
		  	     	   	doc = db.parse(new InputSource(url.openStream()));
		  		     } catch (Exception ioe) {
		  		    	 sec = false;
		  		    	handlerERRThread.sendEmptyMessage(0);
		  		    	 }
		  	if (sec == true){
		  		try {
			  		NodeList charlist = doc.getElementsByTagName("CharCode");
			  		NodeList nomlist = doc.getElementsByTagName("Nominal");
			  		NodeList list = doc.getElementsByTagName("Value");
			  		   int len = list.getLength();
			  		
			  		   String[] sas = { "USD", "EUR", "CHF", "GBP", "JPY", "UAH", "RUB", "MDL", "BYR", "PLN", "LTL", "LVL", "AZN", "AUD", "AMD", "BGN", "BRL", "HUF", "DKK", "INR", "KZT", "CAD", "KGS", "CNY", "NOK", "RON", "XDR", "SGD", "TJS", "TRY", "TMT", "UZS", "CZK", "SEK", "ZAR", "KRW" };
			  		   String[] coursenew = new String[36];
			  		   for(int i = 0; i<36; i++){coursenew[i] = "0";}
			  		   
			  		   for(int i = 0; i<len; i++)
			  		   {
			  			   /* ID */
			  			   	Node ch= charlist.item(i);
			  			    ch.getNodeValue();
			  		   		ch.getFirstChild().getNodeValue();
			  		   		String chStr  = ch.getFirstChild().getNodeValue();
			  		   		int[] chpos = new int[36];
			  		   		for(int j = 0; j<36; j++){chpos[j] = 7777;}
			  		   		for(int j = 0; j<36; j++){
			  		   			if (chStr.equals(sas[j])) {
			  		   			chpos[j] = i;
			  		   			}
			  		   		}
			  		   		
			  		   		/* rate */
				  		   	Node r= nomlist.item(i);
			  			    r.getNodeValue();
			  		   		r.getFirstChild().getNodeValue();
			  		   		String nStr  = r.getFirstChild().getNodeValue();
			  		   		int[] nd = new int[36];
			  		   		for(int j = 0; j<36; j++){nd[j] = 1;}
			  		   		for(int j = 0; j<36; j++){
			  		   			if (chStr.equals(sas[j])) {
			  		   			nd[j] = Integer.parseInt(nStr);
			  		   			}
			  		   		}
			  			   
			  			   /* data */
			  			    Node n= list.item(i);
			  		   		n.getNodeValue();
			  		   		n.getFirstChild().getNodeValue();
			  		   		String dateCurrencyStr  = n.getFirstChild().getNodeValue();
		  		   		
			  		   		coursenew[7] = "1.00";
			  		   		
			  		   		double coursetrue;
			  		   		
				  		   	for(int j = 0; j<36; j++){
				  		   	if(i == chpos[j])	{coursenew[j] = dateCurrencyStr.replace(",", ".");
					   			coursetrue = ( Double.parseDouble(coursenew[j]) / nd[j] );
					   			coursenew[j] = (Double.toString(coursetrue));
					   			}
			  		   		}
				  		   	
				  		  if (i == len-1){
				  			  for(int j = 0; j<36; j++){
					  		   	 course[j].setText(coursenew[j]);
						   			}
				  		  }
			  		   }

				  	 handlBankID="BNM";
			  		 handlerGOODThread.sendEmptyMessage(0);
			  		 
			  			} catch (Exception ioe) {
		  		    	 //donothing
		  		    	}
		  	} else { }
	     } catch (Exception ioe) {
	    	 
	     }
	 }
 
 public void runLongProcessNBRB() {

	  try {
		  	 boolean sec = true;
		  		Document doc = null;
		  		
		  	    try {
		  	    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     	   	DocumentBuilder db = dbf.newDocumentBuilder();	
		  	     	   	URL url = new URL("http://www.nbrb.by/Services/XmlExRates.aspx");
		  	     	   	doc = db.parse(new InputSource(url.openStream()));
		  		     } catch (Exception ioe) {
		  		    	 sec = false;
		  		    	handlerERRThread.sendEmptyMessage(0);
		  		    	 }
		  	if (sec == true){
		  		try {
			  		NodeList charlist = doc.getElementsByTagName("CharCode");
			  		NodeList nomlist = doc.getElementsByTagName("Scale");
			  		NodeList list = doc.getElementsByTagName("Rate");
			  		   int len = list.getLength();
			  		
			  		   String[] sas = { "USD", "EUR", "CHF", "GBP", "JPY", "UAH", "RUB", "MDL", "BYR", "PLN", "LTL", "LVL", "AZN", "AUD", "AMD", "BGN", "BRL", "HUF", "DKK", "INR", "KZT", "CAD", "KGS", "CNY", "NOK", "RON", "XDR", "SGD", "TJS", "TRY", "TMT", "UZS", "CZK", "SEK", "ZAR", "KRW" };
			  		   String[] coursenew = new String[36];
			  		   for(int i = 0; i<36; i++){coursenew[i] = "0";}
			  		   
			  		   for(int i = 0; i<len; i++)
			  		   {
			  			   /* ID */
			  			   	Node ch= charlist.item(i);
			  			    ch.getNodeValue();
			  		   		ch.getFirstChild().getNodeValue();
			  		   		String chStr  = ch.getFirstChild().getNodeValue();
			  		   		int[] chpos = new int[36];
			  		   		for(int j = 0; j<36; j++){chpos[j] = 7777;}
			  		   		for(int j = 0; j<36; j++){
			  		   			if (chStr.equals(sas[j])) {
			  		   			chpos[j] = i;
			  		   			}
			  		   		}
			  		   		
			  		   		/* rate */
				  		   	Node r= nomlist.item(i);
			  			    r.getNodeValue();
			  		   		r.getFirstChild().getNodeValue();
			  		   		String nStr  = r.getFirstChild().getNodeValue();
			  		   		int[] nd = new int[36];
			  		   		for(int j = 0; j<36; j++){nd[j] = 1;}
			  		   		for(int j = 0; j<36; j++){
			  		   			if (chStr.equals(sas[j])) {
			  		   			nd[j] = Integer.parseInt(nStr);
			  		   			}
			  		   		}
			  			   
			  			   /* data */
			  			    Node n= list.item(i);
			  		   		n.getNodeValue();
			  		   		n.getFirstChild().getNodeValue();
			  		   		String dateCurrencyStr  = n.getFirstChild().getNodeValue();
		  		   		
			  		   		coursenew[8] = "1.00";
			  		   		
			  		   		double coursetrue;
			  		   		
				  		   	for(int j = 0; j<36; j++){
				  		   	if(i == chpos[j])	{coursenew[j] = dateCurrencyStr.replace(",", ".");
					   			coursetrue = ( Double.parseDouble(coursenew[j]) / nd[j] );
					   			coursenew[j] = (Double.toString(coursetrue));
					   			}
			  		   		}
				  		   	
				  		  if (i == len-1){
				  			  for(int j = 0; j<36; j++){
					  		   	 course[j].setText(coursenew[j]);
						   			}
				  		  }
			  		   }

					 handlBankID="NBRB";
			  		 handlerGOODThread.sendEmptyMessage(0);
			  		 
			  			} catch (Exception ioe) {
		  		    	 //donothing
		  		    	}
		  	} else { }
		  	// Thread.sleep(1*1000);
	     } catch (Exception ioe) {
	    	 
	     }
	 }
 
 public void runLongProcessAZ() {

	  try {
		  	 boolean sec = true;
		  		Document doc = null;
		  		
		  	    try {
		  	    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     	   	DocumentBuilder db = dbf.newDocumentBuilder();
		  	     	   	
		  	     	   	SimpleDateFormat dateis = new SimpleDateFormat("dd.MM.yyyy"); // "yyyyMMdd_HHmmss"
		  	     	   	String curentDate = dateis.format(new Date());
		  	     	   	String uri = "http://www.cbar.az/currencies/"+curentDate+".xml";

		  	     	   	URL url = new URL(uri);
		  	     	   	
		  	     	   	doc = db.parse(new InputSource(url.openStream()));
		  		     } catch (Exception ioe) {
		  		    	 sec = false;
		  		    	handlerERRThread.sendEmptyMessage(0);
		  		    	 }
		  	if (sec == true){
		  		try {
			  		NodeList charlist = doc.getElementsByTagName("Valute");
			  		NodeList nomlist = doc.getElementsByTagName("Nominal");
			  		NodeList list = doc.getElementsByTagName("Value");
			  		   int len = list.getLength();

			  		   String[] sas = { "USD", "EUR", "CHF", "GBP", "JPY", "UAH", "RUR", "MDL", "BYR", "PLN", "LTL", "LVL", "AZN", "AUD", "AMD", "BGN", "BRL", "HUF", "DKK", "INR", "KZT", "CAD", "KGS", "CNY", "NOK", "RON", "XDR", "SGD", "TJS", "TRY", "TMT", "UZS", "CZK", "SEK", "ZAR", "KRW" };
			  		   String[] coursenew = new String[36];
			  		   for(int i = 0; i<36; i++){coursenew[i] = "0";}

			  		   for(int i = 0; i<len; i++)
			  		   {
			  			   /* ID */
			  			   	Node ch= charlist.item(i);
			  			    ch.getNodeValue();
			  			    ch.getFirstChild().getNodeValue();
			  			    String chStr = ch.getAttributes().getNamedItem("Code").getNodeValue();
			  		   		int[] chpos = new int[36];
			  		   		for(int j = 0; j<36; j++){chpos[j] = 7777;}
			  		   		for(int j = 0; j<36; j++){
			  		   			if (chStr.equals(sas[j])) {
			  		   			chpos[j] = i;
			  		   			}
			  		   		}
			  		   		
			  		   		/* rate */
				  		   	Node r= nomlist.item(i);
			  			    r.getNodeValue();
			  		   		r.getFirstChild().getNodeValue();
			  		   		String nStr  = r.getFirstChild().getNodeValue();
			  		   		int[] nd = new int[36];
			  		   		for(int j = 0; j<36; j++){nd[j] = 1;}
			  		   		for(int j = 0; j<36; j++){
			  		   			if (chStr.equals(sas[j])) {
			  		   			nd[j] = Integer.parseInt(nStr);
			  		   			}
			  		   		}
			  			   
			  			   /* data */
			  			    Node n= list.item(i);
			  		   		n.getNodeValue();
			  		   		n.getFirstChild().getNodeValue();
			  		   		String dateCurrencyStr  = n.getFirstChild().getNodeValue();
		  		   		
			  		   		coursenew[12] = "1.00";
			  		   		
			  		   		double coursetrue;
			  		   		
				  		   	for(int j = 0; j<36; j++){
				  		   	if(i == chpos[j])	{coursenew[j] = dateCurrencyStr.replace(",", ".");
					   			coursetrue = ( Double.parseDouble(coursenew[j]) / nd[j] );
					   			coursenew[j] = (Double.toString(coursetrue));
					   			}
			  		   		}
				  		   	
				  		  if (i == len-1){
				  			  for(int j = 0; j<36; j++){
					  		   	 course[j].setText(coursenew[j]);
						   			}
				  		  }
			  		   }

					 handlBankID="AZ";
			  		 handlerGOODThread.sendEmptyMessage(0);
			  		 
			  			} catch (Exception ioe) {
		  		    	 //donothing
		  		    	}
		  	} else {}
	     } catch (Exception ioe) {
	    	 
	     }
	 }
 
 public void runLongProcessECB() {
	  try {
		  	 boolean sec = true;
		  		Document doc = null;
		  		
		  	    try {
		  	    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     	   	DocumentBuilder db = dbf.newDocumentBuilder();
		  	     	   	String uri = "http://www.ecb.int/stats/eurofxref/eurofxref-daily.xml";

		  	     	   	URL url = new URL(uri);
		  	     	   	
		  	     	   	doc = db.parse(new InputSource(url.openStream()));
		  		     } catch (Exception ioe) {
		  		    	 sec = false;
		  		    	handlerERRThread.sendEmptyMessage(0);
		  		    	 }
		  	if (sec == true){
		  		try {
			  		NodeList list = doc.getElementsByTagName("Cube");
			  		int len = list.getLength()-2;
			  		
			  		   String[] sas = { "USD", "EUR", "CHF", "GBP", "JPY", "UAH", "RUB", "MDL", "BYR", "PLN", "LTL", "LVL", "AZN", "AUD", "AMD", "BGN", "BRL", "HUF", "DKK", "INR", "KZT", "CAD", "KGS", "CNY", "NOK", "RON", "XDR", "SGD", "TJS", "TRY", "TMT", "UZS", "CZK", "SEK", "ZAR", "KRW" };
			  		   String[] coursenew = new String[36];
			  		   for(int i = 0; i<36; i++){coursenew[i] = "0";}

			  		   for(int i = 0; i<len; i++)
			  		   {
			  			   /* ID */
			  			    String chStr = list.item(i+2).getAttributes().getNamedItem("currency").getNodeValue();
			  		   		int[] chpos = new int[36];
			  		   		for(int j = 0; j<36; j++){chpos[j] = 7777;}
			  		   		for(int j = 0; j<36; j++){
			  		   			if (chStr.equals(sas[j])) {
			  		   			chpos[j] = i;
			  		   			}
			  		   		}
			  		   		
			  		   		/* rate */
			  		   		int[] nd = new int[36];
			  		   		for(int j = 0; j<36; j++){nd[j] = 1;}
			  			   
			  			   /* data */
			  			    String dateCurrencyStr = list.item(i+2).getAttributes().getNamedItem("rate").getNodeValue();

			  		   		coursenew[1] = "1.00";

			  		   		double coursetrue;
			  		   		
				  		   	for(int j = 0; j<36; j++){
				  		   	if(i == chpos[j])	{coursenew[j] = dateCurrencyStr.replace(",", ".");
					   			coursetrue = ( 1/Double.parseDouble(coursenew[j]) );
					   			coursenew[j] = (Double.toString(coursetrue));
					   			}
			  		   		}
				  		   	
				  		  if (i == len-1){
				  			  for(int j = 0; j<36; j++){
					  		   	 course[j].setText(coursenew[j]);
						   			}
				  		  }
			  		   }

					 handlBankID="ECB";
			  		 handlerGOODThread.sendEmptyMessage(0);
			  		 
			  			} catch (Exception ioe) {
		  		    	 //donothing
		  		    	}
		  	} else {}
	     } catch (Exception ioe) {
	    	 
	     }
	 }
 
 public void runLongProcessFOREX() {

	  try {
		  	 boolean sec = true;
		  		Document doc = null;
		  		
		  	    try {
		  	    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     	   	DocumentBuilder db = dbf.newDocumentBuilder();	
		  	     	   	URL url = new URL("http://rss.timegenie.com/forex.xml");
		  	     	   	doc = db.parse(new InputSource(url.openStream()));
		  		     } catch (Exception ioe) {
		  		    	 sec = false;
		  		    	handlerERRThread.sendEmptyMessage(0);
		  		    	 }
		  	if (sec == true){
		  		try {
			  		NodeList charlist = doc.getElementsByTagName("code");
			  		NodeList list = doc.getElementsByTagName("rate");
			  		   int len = list.getLength();
			  		
			  		   String[] sas = { "USD", "EUR", "CHF", "GBP", "JPY", "UAH", "RUB", "MDL", "BYR", "PLN", "LTL", "LVL", "AZM", "AUD", "AMD", "BGN", "BRL", "HUF", "DKK", "INR", "KZT", "CAD", "KGS", "CNY", "NOK", "RON", "XDR", "SGD", "TJS", "TRY", "TMT", "UZS", "CZK", "SEK", "ZAR", "KRW" };
			  		   String[] coursenew = new String[36];
			  		   for(int i = 0; i<36; i++){coursenew[i] = "0";}
			  		   
			  		   for(int i = 0; i<len; i++)
			  		   {
			  			   /* ID */
			  			   	Node ch= charlist.item(i);
			  			    ch.getNodeValue();
			  		   		ch.getFirstChild().getNodeValue();
			  		   		String chStr  = ch.getFirstChild().getNodeValue();
			  		   		int[] chpos = new int[36];
			  		   		for(int j = 0; j<36; j++){chpos[j] = 7777;}
			  		   		for(int j = 0; j<36; j++){
			  		   			if (chStr.equals(sas[j])) {
			  		   			chpos[j] = i;
			  		   			}
			  		   		}
			  		   		
			  			   /* data */
			  			    Node n= list.item(i);
			  		   		n.getNodeValue();
			  		   		n.getFirstChild().getNodeValue();
			  		   		String dateCurrencyStr  = n.getFirstChild().getNodeValue();

			  		   		double coursetrue;

				  		   	for(int j = 0; j<36; j++){
				  		   	if(i == chpos[j])	{coursenew[j] = dateCurrencyStr.replace(",", ".");
					   			coursetrue = ( 1/Double.parseDouble(coursenew[j]) );
					   			coursenew[j] = (Double.toString(coursetrue));
					   			}
			  		   		}
				  		   	
				  		  if (i == len-1){
				  			  for(int j = 0; j<36; j++){
					  		   	 course[j].setText(coursenew[j]);
						   			}
				  		  }
			  		   }

					 handlBankID="FOREX";
			  		 handlerGOODThread.sendEmptyMessage(0);
			  		 
			  			} catch (Exception ioe) {
		  		    	 //donothing
		  		    	}
		  	} else { }
	     } catch (Exception ioe) {
	    	 
	     }
	 }
 
 
 /// Обработчик пересчета
 void UpdateResultsHandler() {
	  course[36].setText(courserate[36].getText().toString());

	    for (int i=0;i<count;i++ ){
	    	int resID = getResources().getIdentifier("from" + sa[i],"id", getPackageName());
	    	from[i] = (RadioButton)findViewById(resID);
	    	}
	    
	    for (int i=0;i<count;i++ ){
	    	int resID = getResources().getIdentifier("to" + sa[i],"id", getPackageName());
	    	to[i] = (RadioButton)findViewById(resID);
	    	}
	    
         if (text.getText().length() == 0) {amountmoney.setText("0.00");return;} else
         if (text.getText().toString().equals("-")) {amountmoney.setText("0.00");return;} else
        	 
         if (checkBank != 1) {
        	 for (int i=0;i<count;i++ ){
        	 try {
        		if (from[i].isChecked()){	if ((i == 36) && (reverserates)){	curentfromcourserate = (1/Double.parseDouble(course[i].getText().toString()));
        			 				 } else if (to[36].isChecked()) {			curentfromcourserate = Double.parseDouble(courserate[i].getText().toString());
        			 				 } else {									curentfromcourserate = Double.parseDouble(course[i].getText().toString());}
        		}
        		 if (to[i].isChecked()){	if ((i == 36) && (reverserates)){	curenttocourserate = (1/Double.parseDouble(course[i].getText().toString()));
        			 				 } else if (from[36].isChecked()) {			curenttocourserate = Double.parseDouble(courserate[i].getText().toString());
		        			 		 } else {									curenttocourserate = Double.parseDouble(course[i].getText().toString());}
        		 }
          	 } catch (Exception ioe) {handlerERRdevnull.sendEmptyMessage(0);}
        	 }
        	 
        	 try {		BigDecimal x = new BigDecimal(Double.parseDouble(text.getText().toString()) * curentfromcourserate / curenttocourserate);
        	  		   	x = x.setScale(2, BigDecimal.ROUND_HALF_UP); // Точность округления расчетов при курсах от банка
        	  		   	amountmoney.setText(x.toString());
        	 } catch (Exception ioe) {handlerERRdevzero.sendEmptyMessage(0);}
        	 
         } else {
				for (int i = 0; i < count; i++) {
	        	try {
					if (from[i].isChecked()) {
						curentfromcourserate = Double.parseDouble(courserate[i].getText().toString());
					}
					if (to[i].isChecked()) {
						curenttocourserate = Double.parseDouble(courserate[i].getText().toString());
					}
	          	} catch (Exception ioe) {handlerERRdevnull.sendEmptyMessage(0);}
				}
				
	        	 if (reverserates){
		        		 try {	BigDecimal x = new BigDecimal(Double.parseDouble(text.getText().toString()) * curenttocourserate / curentfromcourserate);
				  		   		x = x.setScale(2, BigDecimal.ROUND_HALF_UP); // Точность округления расчетов при курсах от пользователя
				  		   		amountmoney.setText(x.toString());
		        		 } catch (Exception ioe) {handlerERRdevzero.sendEmptyMessage(0);}
				  } else {
					  try {	BigDecimal x = new BigDecimal(Double.parseDouble(text.getText().toString()) * curentfromcourserate / curenttocourserate);
			  		   		x = x.setScale(2, BigDecimal.ROUND_HALF_UP); // Точность округления расчетов при курсах от пользователя
			  		   		amountmoney.setText(x.toString());
		        	 	} catch (Exception ioe) {handlerERRdevzero.sendEmptyMessage(0);}
				  }
         }
     }
 
 public void BankChangeClickHandler(View view){
	 
	 AlertDialog.Builder BankSelect = new AlertDialog.Builder(this);
	 BankSelect.setTitle(getResources().getString(R.string.bank_is_this));
	 
	 LayoutInflater inflater = getLayoutInflater();
	 View radioLayout = inflater.inflate(R.layout.popup_bank, null);
	 BankSelect.setView(radioLayout);

	 final RadioButton[] radiobank = new RadioButton[8];
	 final String[] banktext = getResources().getStringArray(R.array.listsourceArray);

	 for (int i=0;i<8;i++ ){
			int resID = radioLayout.getResources().getIdentifier("radio" + i,"id", getPackageName());
			radiobank[i] = (RadioButton)radioLayout.findViewById(resID);
			radiobank[i].setText(banktext[i]);
			if (checkBank==i){radiobank[i].setChecked(true);};
	 }
	 
	    if (checkBank == 1) {
	    	StringBuilder sb = new StringBuilder();
		    for (int i = 0; i < count; i++) {
		        sb.append(courserate[i].getText().toString()).append(",");
		        }
		   		nanostore_shared_editor.putString("rates_from_"+BANK_ID, sb.toString());
		    	nanostore_shared_editor.commit();
	    }

	 BankSelect.setPositiveButton("Ok",
	   new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
	   	 for (int i=0;i<8;i++ ){if (radiobank[i].isChecked()){which=i;};}
	     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	     prefs.edit().putString("listSourcesDefault", String.valueOf(which)).commit();
	     
	     //// update view
	     getRadio();
		 setkey();
		 getresID2();
		 getPrefs();

		      if (checkBank == 1){TurnOFFrates();buttonloadfrom.setVisibility(View.VISIBLE);} else {TurnONrates();buttonloadfrom.setVisibility(View.GONE);}

		      String[] separated = nanostore_shared.getString("rates_from_"+BANK_ID, "7777").split(",");

		      if (separated[0].equals("7777") && checkBank != 1 ){
		    	  /* if never updated, update */
		    	  NanoConverter.mContext.processThread();
		      } else {
		      for (int i=0;i<count;i++ ){try {course[i].setText(separated[i]);} catch (Exception ioe) {}
		      }}
		      courserate[36].setText(course[36].getText().toString());
		      UpdateRates();
	     ////
	    }
	   });
	 AlertDialog BankSelectDialog = BankSelect.create();
	 BankSelectDialog.show();
 }
 
 public void CopyFromBankClickHandler(View view){

	 AlertDialog.Builder BankSelect = new AlertDialog.Builder(this);
	 BankSelect.setTitle(getResources().getString(R.string.presets));
	 BankSelect.setMessage(getResources().getString(R.string.presets_disc)+":");
	 
	 LayoutInflater inflater = getLayoutInflater();
	 View radioLayout = inflater.inflate(R.layout.popup_bank, null);
	 BankSelect.setView(radioLayout);

	 final RadioButton[] radiobank = new RadioButton[8];
	 final String[] bankid = getResources().getStringArray(R.array.listBankID);
	 final String[] banktext = getResources().getStringArray(R.array.listsourceArray);

	 for (int i=0;i<8;i++ ){
			int resID = radioLayout.getResources().getIdentifier("radio" + i,"id", getPackageName());
			radiobank[i] = (RadioButton)radioLayout.findViewById(resID);
			radiobank[i].setText(banktext[i]);
			if (checkBank==i){radiobank[i].setChecked(true);};
	 }

	 BankSelect.setPositiveButton("Ok",
	   new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
	   	 for (int i=0;i<8;i++ ){if (radiobank[i].isChecked()){which=i;};}	    	
	   	 checkBank=which;
	   	 BANK_ID=bankid[which];

	     //// update view
	     getRadio();
		 setkey();
		 getresID2();

		      String[] separated = nanostore_shared.getString("rates_from_"+BANK_ID, "7777").split(",");

		      if (separated[0].equals("7777") && checkBank != 1 ){
		    	  /* if never updated, update */
		    	  NanoConverter.mContext.processThread();
		      } else {
		      for (int i=0;i<count;i++ ){try {course[i].setText(separated[i]);} catch (Exception ioe) {}
		      }}
		      courserate[36].setText(course[36].getText().toString());
		      UpdateRates();
	     ////
		      BANK_ID="USER_DATA";
		      checkBank=1;
	    }
	   });
	 AlertDialog BankSelectDialog = BankSelect.create();
	 BankSelectDialog.show();
 }
 public void InverseStateClickHandler(View view){
     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
     if (reverserates){
    	 prefs.edit().putBoolean("revratesswitch", false).commit();
    	 reverserates = false;
     } else {
    	 prefs.edit().putBoolean("revratesswitch", true).commit();
    	 reverserates = true;
     }
	 UpdateRates();
 }
 
 /* Обработчик обновления */
 public void UpdateButtonClickHandler(View view) {
     switch (view.getId()) {
     case R.id.UpdateButton:
 		NanoConverter.mContext.processThread();
     }
 }
 
  void TurnONrates() {
	  for (int i=0;i<count;i++ ){
		  courserate[i].setKeyListener(null);
		  }
	    buttonrefresh.setEnabled(true);
	    courserate[36].setKeyListener(course[0].getKeyListener());
}
  
  void TurnOFFrates() {
	  for (int i=0;i<count;i++ ){
		  courserate[i].setKeyListener(course[0].getKeyListener());
		  }
	    buttonrefresh.setEnabled(false);
  }
  
void getPrefs() {

             SharedPreferences prefs = PreferenceManager
                             .getDefaultSharedPreferences(getBaseContext());
             
        	 for (int i=0;i<count;i++ ){
        	    	boolean resID = prefs.getBoolean("checkbox"+sa[i], true);
        	    	mactive[i] = resID;
        	 }

            ListCurPreference = prefs.getString("listCurByDefault", "0");
            ListBankPreference = prefs.getString("listSourcesDefault", "0");
            listUpdate = prefs.getString("listUpdate", "0");
            reverserates = prefs.getBoolean("revratesswitch", false);
            
            checkUPDT = Integer.parseInt(listUpdate);
            checkBank = Integer.parseInt(ListBankPreference);
      	    checkCurd = Integer.parseInt(ListCurPreference);

            	if (checkBank == 0){ BANK_ID = "CBR";} else
            	if (checkBank == 1){ BANK_ID = "USER_DATA";} else
                if (checkBank == 2){ BANK_ID = "NBU";} else
                if (checkBank == 3){ BANK_ID = "NBRB";} else
                if (checkBank == 4){ BANK_ID = "BNM";} else
                if (checkBank == 5){ BANK_ID = "AZ";} else
                if (checkBank == 6){ BANK_ID = "ECB";} else
                if (checkBank == 7){ BANK_ID = "FOREX";}
            
            String bkgr = prefs.getString("bkgcheckbox", "0");
            View maintabhost = findViewById(android.R.id.tabhost);
            View scrl1 = findViewById(R.id.scroll1);
            View scrl2 = findViewById(R.id.scroll2);
            
            if (bkgr.equals("0")){
            	maintabhost.setBackgroundColor(Color.BLACK);
                scrl1.setBackgroundDrawable(getResources().getDrawable(R.drawable.bkgb));scrl2.setBackgroundDrawable(getResources().getDrawable(R.drawable.bkgb));} else
            if (bkgr.equals("1")){
            	maintabhost.setBackgroundColor(Color.BLACK);
            	scrl1.setBackgroundDrawable(null);scrl2.setBackgroundDrawable(null);} else
            if (bkgr.equals("2")){
            	maintabhost.setBackgroundColor(Color.DKGRAY);
            	scrl1.setBackgroundDrawable(null);scrl2.setBackgroundDrawable(null);} else
            if (bkgr.equals("3")){
            	maintabhost.setBackgroundDrawable(getWallpaper());
            	scrl1.setBackgroundDrawable(null);scrl2.setBackgroundDrawable(null);} else
            if (bkgr.equals("4")){
            	maintabhost.setBackgroundDrawable(getResources().getDrawable(R.drawable.drr));
                scrl1.setBackgroundDrawable(null);scrl2.setBackgroundDrawable(null);} else
            if (bkgr.equals("5")){
            	maintabhost.setBackgroundDrawable(getResources().getDrawable(R.drawable.dgr));
                scrl1.setBackgroundDrawable(null);scrl2.setBackgroundDrawable(null);} else
            if (bkgr.equals("6")){
            	maintabhost.setBackgroundDrawable(getResources().getDrawable(R.drawable.ggr));
                scrl1.setBackgroundDrawable(null);scrl2.setBackgroundDrawable(null);} else
            if (bkgr.equals("7")){
            	maintabhost.setBackgroundDrawable(getResources().getDrawable(R.drawable.gdr));
            	scrl1.setBackgroundDrawable(null);scrl2.setBackgroundDrawable(null);} else
            if (bkgr.equals("8")){
            	maintabhost.setBackgroundDrawable(getResources().getDrawable(R.drawable.bkgmain));
            	scrl1.setBackgroundDrawable(null);scrl2.setBackgroundDrawable(null);} else
            if (bkgr.equals("9")){
                maintabhost.setBackgroundDrawable(getResources().getDrawable(R.drawable.flower));
                scrl1.setBackgroundDrawable(null);scrl2.setBackgroundDrawable(null);
                }
}
    
  void UpdateRates() {
	  BigDecimal y= new BigDecimal(0);
	  zerocheck();
	  
	  if (checkBank == 1) {for (int j=0;j<count;j++){from[j].setEnabled(true);to[j].setEnabled(true);courserate[j].setEnabled(true);}}
		
		  for (int i=0;i<count;i++ ){
			  if (checkCurd == i && Double.parseDouble(course[i].getText().toString()) != 0){coursebydefaultis = course[i].getText().toString();}
			  if (checkCurd == i && Double.parseDouble(course[i].getText().toString()) == 0){{Toast toast2 = Toast.makeText(getApplicationContext(), getString(R.string.zerocheck), Toast.LENGTH_LONG);toast2.show();coursebydefaultis = course[0].getText().toString();}}
			  }

		  for (int i=0;i<count;i++ ){
			  if (i != 36){
			  if (reverserates){
				  	if (Float.parseFloat(course[i].getText().toString())!=0){
					y = new BigDecimal(Double.parseDouble(coursebydefaultis)/Double.parseDouble(course[i].getText().toString()));} else {y = new BigDecimal(0);}
			  } else {
					y = new BigDecimal(Double.parseDouble(course[i].getText().toString())/Double.parseDouble(coursebydefaultis));}

	  		  y = y.setScale(4, BigDecimal.ROUND_HALF_UP);  // Точность округления вкладки курсы
			  courserate[i].setText(y.toString());
			  }}
	  
  }
  void zerocheck() {
	  for (int j=0;j<count;j++){
		  try {
          if (Double.parseDouble(course[j].getText().toString()) == 0){
          	from[j].setEnabled(false);
          	to[j].setEnabled(false);
          	courserate[j].setEnabled(false);
          	from[36].setEnabled(true);to[36].setEnabled(true);courserate[36].setEnabled(true);
  		 } else {
  			from[j].setEnabled(true);to[j].setEnabled(true);courserate[j].setEnabled(true);
  		 }} catch (Exception ioe) {handlerERRdevnull.sendEmptyMessage(0);}
          }
  }
}