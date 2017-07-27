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

import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import android.content.ComponentName;
import android.appwidget.AppWidgetManager;

import android.content.Context;
import android.app.PendingIntent;
import android.widget.RemoteViews;

import android.os.SystemClock;
import android.net.Uri;

import java.lang.reflect.Method;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;

import android.widget.Toast;

public class SubarXiv extends Activity implements
        AdapterView.OnItemClickListener {

    public Context thisActivity;

    //UI-Views
    private TextView headerTextView;
    public ListView list;
    
    private String name;
    private String[] items;
    private String[] urls;
    private String[] shortItems;

    private static final Class[] mRemoveAllViewsSignature = new Class[] {
     int.class};
    private static final Class[] mAddViewSignature = new Class[] {
     int.class, RemoteViews.class};
    private Method mRemoveAllViews;
    private Method mAddView;
    private Object[] mRemoveAllViewsArgs = new Object[1];
    private Object[] mAddViewArgs = new Object[2];
    private int mySourcePref =0;

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        arXivDB droidDB = new arXivDB(this);

        if (mySourcePref == 0) {
            String tempquery = "search_query=cat:" + urls[info.position];
            if (info.position == 0) {
                tempquery = tempquery + "*";
            }
            String tempurl = "http://export.arxiv.org/api/query?" + tempquery
                    + "&sortBy=submittedDate&sortOrder=ascending";
            droidDB.insertFeed(shortItems[info.position],
                    tempquery, tempurl, -1,-1);
            Thread t9 = new Thread() {
                public void run() {
                    updateWidget();
                }
            };
            t9.start();
        } else {
            String tempquery = urls[info.position];
            String tempurl = tempquery;
            droidDB.insertFeed(shortItems[info.position]+" (RSS)", shortItems[info.position], tempurl,-2,-2);
            Toast.makeText(this, R.string.added_to_favorites_rss,
              Toast.LENGTH_SHORT).show();
        }
        droidDB.close();

        return true;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submain);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        urls = myIntent.getStringArrayExtra("keyurls");
        items = myIntent.getStringArrayExtra("keyitems");
        shortItems = myIntent.getStringArrayExtra("keyshortitems");

        headerTextView = (TextView) findViewById(R.id.theadersm);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/LiberationSans.ttf");
        headerTextView.setTypeface(face);

        list = (ListView) findViewById(R.id.listsm);

        thisActivity = this;

        headerTextView.setText(name);

        list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items));

        list.setOnItemClickListener(this);
        registerForContextMenu(list);

        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        mySourcePref=Integer.parseInt(prefs.getString("sourcelist", "0"));

    }

    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenuInfo menuInfo) {
        menu.add(0, 1000, 0, R.string.add_favorites);
    }

    public void onItemClick(AdapterView<?> a, View v, int position, long id) {

        if (mySourcePref == 0) {
            Intent myIntent = new Intent(this, SearchListWindow.class);
            myIntent.putExtra("keyname", shortItems[position]);
            String tempquery = "search_query=cat:" + urls[position];
            if (position == 0) {
                tempquery = tempquery + "*";
            }
            myIntent.putExtra("keyquery", tempquery);
            String tempurl = "http://export.arxiv.org/api/query?" + tempquery
                    + "&sortBy=submittedDate&sortOrder=ascending";
            myIntent.putExtra("keyurl", tempurl);
            startActivity(myIntent);
        } else {
            Intent myIntent = new Intent(this, RSSListWindow.class);
            myIntent.putExtra("keyname", shortItems[position]);
            myIntent.putExtra("keyurl", urls[position]);
            startActivity(myIntent);
        }
    }

    public void updateWidget() {
        // Get the layout for the App Widget and attach an on-click listener to the button
        Context context = getApplicationContext();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.arxiv_appwidget);
        // Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, arXiv.class);
        String typestring = "widget";
        intent.putExtra("keywidget",typestring);
        intent.setData((Uri.parse("foobar://"+SystemClock.elapsedRealtime())));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.mainlayout, pendingIntent);

        arXivDB droidDB = new arXivDB(thisActivity);
        List<Feed> favorites = droidDB.getFeeds();
        droidDB.close();

        String favText = "";

        if (favorites.size() > 0) {
            try {
                mRemoveAllViews = RemoteViews.class.getMethod("removeAllViews",
                 mRemoveAllViewsSignature);
                mRemoveAllViewsArgs[0] = Integer.valueOf(R.id.mainlayout);
                mRemoveAllViews.invoke(views, mRemoveAllViewsArgs);

                //views.removeAllViews(R.id.mainlayout);
                
            } catch (Exception ef) {
            }
            for (Feed feed : favorites) {

                if (feed.url.contains("query")) {

                    String urlAddressTemp = "http://export.arxiv.org/api/query?" + feed.shortTitle
                            + "&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=1";

                    int numberOfTotalResults = 0;
                    try {
                        URL url = new URL(urlAddressTemp);
                        SAXParserFactory spf = SAXParserFactory.newInstance();
                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        XMLHandlerSearch myXMLHandler = new XMLHandlerSearch();
                        xr.setContentHandler(myXMLHandler);
                        xr.parse(new InputSource(url.openStream()));
                        numberOfTotalResults = myXMLHandler.numTotalItems;
                    } catch (Exception ef) {
                    }

                    RemoteViews tempViews = new RemoteViews(context.getPackageName(), R.layout.arxiv_appwidget_item);
                    favText = feed.title;
                    if (feed.count > -1) {
                        int newArticles = numberOfTotalResults-feed.count;
                        tempViews.setTextViewText(R.id.number, ""+newArticles);
                    } else {
                        tempViews.setTextViewText(R.id.number, "0");
                    }
                    tempViews.setTextViewText(R.id.favtext, favText);

                    try {
                        mAddView = RemoteViews.class.getMethod("addView",
                         mAddViewSignature);
                        mAddViewArgs[0] = Integer.valueOf(R.id.mainlayout);
                        mAddViewArgs[1] = tempViews;
                        mAddView.invoke(views, mAddViewArgs);
                        //views.addView(R.id.mainlayout, tempViews);
                    } catch (Exception ef) {
                        views.setTextViewText(R.id.subheading,"Widget only supported on Android 2.1+");
                    }
                }
                ComponentName thisWidget = new ComponentName(thisActivity, ArxivAppWidgetProvider.class);
                AppWidgetManager manager = AppWidgetManager.getInstance(thisActivity);
                manager.updateAppWidget(thisWidget, views);
            }
        }

    }

}
