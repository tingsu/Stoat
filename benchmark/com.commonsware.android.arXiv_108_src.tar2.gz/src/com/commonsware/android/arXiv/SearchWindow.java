/*
    arXiv mobile - a Free arXiv app for android
    http://code.google.com/p/arxiv-mobile/

    Copyright (C) 2010 Jack Deslippe

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 */

package com.commonsware.android.arXiv;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchWindow extends Activity implements
        AdapterView.OnItemSelectedListener, TextWatcher {

    //UI-Views
    private Button dateBtn;
    private TextView header;
    private EditText field1;
    private EditText field2;
    private EditText field3;
    
    private String finalDate;
    private String query1 = "";
    private String query2 = "";
    private String query3 = "";
    private String textEntryValue1 = "";
    private String textEntryValue2 = "";
    private String textEntryValue3 = "";
    private String[] items = { "Author", "Title", "Abstract", "arXivID" };
    private int iSelected1 = 0;
    private int iSelected2 = 0;
    private int iSelected3 = 0;
    private int mYear;
    private int mMonth;
    private int mDay;

    static final int DATE_DIALOG_ID = 0;

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            dateBtn.setText("" + mYear + "-" + mMonth + "-" + mDay);
            if (mMonth > 9) {
                finalDate = "" + mYear + (mMonth + 1) + mDay + "2399";
            } else {
                finalDate = "" + mYear + "0" + (mMonth + 1) + mDay + "2399";
            }
        }
    };

    public void afterTextChanged(Editable s) {
        // needed for interface, but not used
    }

    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        // needed for interface, but not used
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        header = (TextView) findViewById(R.id.theaderse);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/LiberationSans.ttf");

        header.setTypeface(face);
        header.setText("Search");

        dateBtn = (Button) findViewById(R.id.datebtn);
        Spinner spin1 = (Spinner) findViewById(R.id.spinner1);
        spin1.setOnItemSelectedListener(this);
        Spinner spin2 = (Spinner) findViewById(R.id.spinner2);
        spin2.setOnItemSelectedListener(this);
        Spinner spin3 = (Spinner) findViewById(R.id.spinner3);
        spin3.setOnItemSelectedListener(this);

        ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        aa
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin1.setAdapter(aa);
        spin2.setAdapter(aa);
        spin3.setAdapter(aa);

        field1 = (EditText) findViewById(R.id.field1);
        field1.addTextChangedListener(this);
        field2 = (EditText) findViewById(R.id.field2);
        field2.addTextChangedListener(this);
        field3 = (EditText) findViewById(R.id.field3);
        field3.addTextChangedListener(this);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date currentTime_1 = new Date();
        String finalDate = formatter.format(currentTime_1);
        finalDate = finalDate + "2359";

        Log.d("arXiv - ", finalDate);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        if (mMonth > 9) {
            finalDate = "" + mYear + (mMonth + 1) + mDay + "2399";
        } else {
            finalDate = "" + mYear + "0" + (mMonth + 1) + mDay + "2399";
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
                    mDay);
        }
        return null;
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position,
            long id) {

        long idn = parent.getId();
        if (idn == R.id.spinner1) {
            iSelected1 = position;
        } else if (idn == R.id.spinner2) {
            iSelected2 = position;
        } else if (idn == R.id.spinner3) {
            iSelected3 = position;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String tempt = "";
        tempt = field1.getText().toString();
        if (textEntryValue1 != tempt) {
            textEntryValue1 = tempt;
        }
        tempt = field2.getText().toString();
        if (textEntryValue2 != tempt) {
            textEntryValue2 = tempt;
        }
        tempt = field3.getText().toString();
        if (textEntryValue3 != tempt) {
            textEntryValue3 = tempt;
        }
    }

    public void pressedDateButton(View button) {
        showDialog(DATE_DIALOG_ID);
    }

    public void pressedSearchButton(View button) {
        String query = "";
        String idlist = "";
        String tittext = "Search: " + textEntryValue1;
        if (iSelected1 == 0) {
            query1 = "au:%22" + textEntryValue1.replace(" ", "+").replace("-", "_")
                    + "%22";
            query = query1;
        } else if (iSelected1 == 1) {
            query1 = "ti:%22" + textEntryValue1.replace(" ", "+") + "%22";
            query = query1;
        } else if (iSelected1 == 2) {
            query1 = "abs:%22" + textEntryValue1.replace(" ", "+") + "%22";
            query = query1;
        } else if (iSelected1 == 3) {
            idlist = idlist + textEntryValue1.replace(" ", ",");
        }
        if (!(textEntryValue2 == null || textEntryValue2.equals(""))) {
            tittext = tittext + " " + textEntryValue2;
            if (iSelected2 == 0) {
                query2 = "au:%22" + textEntryValue2.replace(" ", "+").replace("-", "_")
                        + "%22";
                if (!(query == null || query.equals(""))) {
                    query = query + "+AND+" + query2;
                } else {
                    query = query2;
                }
            } else if (iSelected2 == 1) {
                query2 = "ti:%22" + textEntryValue2.replace(" ", "+") + "%22";
                if (!(query == null || query.equals(""))) {
                    query = query + "+AND+" + query2;
                } else {
                    query = query2;
                }
            } else if (iSelected2 == 2) {
                query2 = "abs:%22" + textEntryValue2.replace(" ", "+") + "%22";
                if (!(query == null || query.equals(""))) {
                    query = query + "+AND+" + query2;
                } else {
                    query = query2;
                }
            } else if (iSelected2 == 3) {
                idlist = idlist + textEntryValue2.replace(" ", ",");
            }
        }
        if (!(textEntryValue3 == null || textEntryValue3.equals(""))) {
            tittext = tittext + " " + textEntryValue3;
            if (iSelected3 == 0) {
                query3 = "au:%22" + textEntryValue3.replace(" ", "+").replace("-", "_")
                        + "%22";
                if (!(query == null || query.equals(""))) {
                    query = query + "+AND+" + query3;
                } else {
                    query = query3;
                }
            } else if (iSelected3 == 1) {
                query3 = "ti:%22" + textEntryValue3.replace(" ", "+") + "%22";
                if (!(query == null || query.equals(""))) {
                    query = query + "+AND+" + query3;
                } else {
                    query = query3;
                }
            } else if (iSelected3 == 2) {
                query3 = "abs:%22" + textEntryValue3.replace(" ", "+") + "%22";
                if (!(query == null || query.equals(""))) {
                    query = query + "+AND+" + query3;
                } else {
                    query = query3;
                }
            } else if (iSelected3 == 3) {
                idlist = idlist + textEntryValue3.replace(" ", ",");
            }
        }

        String totalsearch = "";
        if (query == "" || query == null) {
            totalsearch = "search_query=lastUpdatedDate:[199008010001+TO+"
                    + finalDate + "]&";
        } else {
            totalsearch = "search_query=lastUpdatedDate:[199008010001+TO+"
                    + finalDate + "]+AND+" + query + "&";
        }
        totalsearch = totalsearch + "id_list=" + idlist;

        Intent myIntent = new Intent(this, SearchListWindow.class);
        if (tittext.length() > 30) {
            tittext = tittext.substring(0, 30);
        }
        myIntent.putExtra("keyname", tittext);
        String urlad = "http://export.arxiv.org/api/query?"
                + totalsearch
                + "&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
        Log.d("arXiv - ", urlad);
        myIntent.putExtra("keyurl", urlad);
        myIntent.putExtra("keyquery", totalsearch);
        startActivity(myIntent);
    }

}
