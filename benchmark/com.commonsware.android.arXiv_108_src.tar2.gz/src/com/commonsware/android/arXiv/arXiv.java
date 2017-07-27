/*
y    arXiv mobile - a Free arXiv app for android
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
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;

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

import android.preference.PreferenceManager;
import android.content.SharedPreferences;

public class arXiv extends Activity implements AdapterView.OnItemClickListener {

    public Context thisActivity;

    //UI-Views
    private TextView header;
    private ListView catList;
    private ListView favList;
  
    private arXivDB droidDB;
    private int vFlag = 1;
    private int mySourcePref = 0;
    public static final int ABOUT_ID = Menu.FIRST + 1;
    public static final int HISTORY_ID = Menu.FIRST + 2;
    public static final int CLEAR_ID = Menu.FIRST + 3;
    public static final int PREF_ID = Menu.FIRST + 4;
    public static final int DONATE_ID = Menu.FIRST + 5;
    private List<Feed> favorites;
    private List<History> historys;

    private static final Class[] mRemoveAllViewsSignature = new Class[] {
     int.class};
    private static final Class[] mAddViewSignature = new Class[] {
     int.class, RemoteViews.class};
    private Method mRemoveAllViews;
    private Method mAddView;
    private Object[] mRemoveAllViewsArgs = new Object[1];
    private Object[] mAddViewArgs = new Object[2];
    private String[] unreadList;
    private String[] favoritesList;
    private Boolean vFromWidget = false;

    String[] items = { "Astrophysics", "Condensed Matter", "Computer Science",
            "General Relativity", "HEP Experiment", "HEP Lattice",
            "HEP Phenomenology", "HEP Theory", "Mathematics",
            "Mathematical Physics", "Misc Physics", "Nonlinear Sciences",
            "Nuclear Experiment", "Nuclear Theory", "Quantitative Biology",
            "Quantitative Finance", "Quantum Physics", "Statistics" };

    int[] itemsFlag = { 1, 2, 3, 0, 0, 0, 0, 0, 4, 0, 5, 6, 0, 0, 7, 8, 0, 9 };

    String[] shortItems = { "Astrophysics", "Condensed Matter",
            "Computer Science", "General Relativity", "HEP Experiment",
            "HEP Lattice", "HEP Phenomenology", "HEP Theory", "Mathematics",
            "Math. Physics", "Misc Physics", "Nonlinear Sci.", "Nuclear Exp.",
            "Nuclear Theory", "Quant. Biology", "Quant. Finance",
            "Quantum Physics", "Statistics" };

    String[] urls = { "astro-ph", "cond-mat", "cs", "gr-qc", "hep-ex",
            "hep-lat", "hep-ph", "hep-th", "math", "math-ph", "physics",
            "nlin", "nucl-ex", "nucl-th", "q-bio", "q-fin", "quant-ph", "stat" };

    String[] asItems = { "Astrophysics All",
            "Cosmology and Extragalactic Astrophysics",
            "Earth & Planetary Astrophysics", "Galaxy Astrophysics",
            "HE Astrophysical Phenomena",
            "Instrumentation and Methods for Astrophysics",
            "Solar and Stellar Astrophysics" };

    String[] asURLs = { "astro-ph", "astro-ph.CO", "astro-ph.EP",
            "astro-ph.GA", "astro-ph.HE", "astro-ph.IM", "astro-ph.SR" };

    String[] asShortItems = { "Astrophysics All",
            "Cosm. & Ext-Gal. Astrophysics", "Earth & Planetary Astrophysics",
            "Galaxy Astrophysics", "HE Astrophysical Phenomena",
            "Instrumentation and Methods for Astrophysics",
            "Solar and Stellar Astrophysics" };

    String[] cmItems = { "Condensed Matter All",
            "Disordered Systems and Neural Networks", "Materials Science",
            "Mesoscale and Nanoscale Physics", "Other Condensed Matter",
            "Quantum Gases", "Soft Condensed Matter", "Statistical Mechanics",
            "Strongly Correlated Electrons", "Superconductivity" };

    String[] cmURLs = { "cond-mat", "cond-mat.dis-nn", "cond-mat.mtrl-sci",
            "cond-mat.mes-hall", "cond-mat.other", "cond-mat.quant-gas",
            "cond-mat.soft", "cond-mat.stat-mech", "cond-mat.str-el",
            "cond-mat.supr-con" };

    String[] cmShortItems = { "Cond. Matter All",
            "Disord. Systems & Neural Networks", "Materials Science",
            "Mesoscale and Nanoscale Physics", "Other Condensed Matter",
            "Quantum Gases", "Soft Condensed Matter", "Statistical Mechanics",
            "Strongly Correlated Electrons", "Superconductivity" };

    String[] csItems = { "Computer Science All", "Architecture",
            "Artificial Intelligence", "Computation and Language",
            "Computational Complexity",
            "Computational Engineering, Finance and Science",
            "Computational Geometry", "CS and Game Theory",
            "Computer Vision and Pattern Recognition", "Computers and Society",
            "Cryptography and Security", "Data Structures and Algorithms",
            "Databases", "Digital Libraries", "Discrete Mathematics",
            "Distributed, Parallel, and Cluster Computing",
            "Formal Languages and Automata Theory", "General Literature",
            "Graphics", "Human-Computer Interaction", "Information Retrieval",
            "Information Theory", "Learning", "Logic in Computer Science",
            "Mathematical Software", "Multiagent Systems", "Multimedia",
            "Networking and Internet Architecture",
            "Neural and Evolutionary Computing", "Numerical Analysis",
            "Operating Systems", "Other Computer Science", "Performance",
            "Programming Languages", "Robotics", "Software Engineering",
            "Sound", "Symbolic Computation" };

    String[] csURLs = { "cs", "cs.AR", "cs.AI", "cs.CL", "cs.CC", "cs.CE",
            "cs.CG", "cs.GT", "cs.CV", "cs.CY", "cs.CR", "cs.DS", "cs.DB",
            "cs.DL", "cs.DM", "cs.DC", "cs.FL", "cs.GL", "cs.GR", "cs.HC",
            "cs.IR", "cs.IT", "cs.LG", "cs.LO", "cs.MS", "cs.MA", "cs.MM",
            "cs.NI", "cs.NE", "cs.NA", "cs.OS", "cs.OH", "cs.PF", "cs.PL",
            "cs.RO", "cs.SE", "cs.SD", "cs.SC" };

    String[] csShortItems = { "Computer Science All", "Architecture",
            "Artificial Intelligence", "Computation and Language",
            "Computational Complexity",
            "Comp. Eng., Fin. & Science",
            "Computational Geometry", "CS and Game Theory",
            "Computer Vision and Pattern Recognition", "Computers and Society",
            "Cryptography and Security", "Data Structures and Algorithms",
            "Databases", "Digital Libraries", "Discrete Mathematics",
            "Distributed, Parallel, and Cluster Computing",
            "Formal Languages and Automata Theory", "General Literature",
            "Graphics", "Human-Computer Interaction", "Information Retrieval",
            "Information Theory", "Learning", "Logic in Computer Science",
            "Mathematical Software", "Multiagent Systems", "Multimedia",
            "Networking and Internet Architecture",
            "Neural and Evolutionary Computing", "Numerical Analysis",
            "Operating Systems", "Other Computer Science", "Performance",
            "Programming Languages", "Robotics", "Software Engineering",
            "Sound", "Symbolic Computation" };

    String[] mtItems = { "Math All", "Algebraic Geometry",
            "Algebraic Topology", "Analysis of PDEs", "Category Theory",
            "Classical Analysis of ODEs", "Combinatorics",
            "Commutative Algebra", "Complex Variables",
            "Differential Geometry", "Dynamical Systems",
            "Functional Analysis", "General Mathematics", "General Topology",
            "Geometric Topology", "Group Theory", "Math History and Overview",
            "Information Theory", "K-Theory and Homology", "Logic",
            "Mathematical Physics", "Metric Geometry", "Number Theory",
            "Numerical Analysis", "Operator Algebras",
            "Optimization and Control", "Probability", "Quantum Algebra",
            "Representation Theory", "Rings and Algebras", "Spectral Theory",
            "Statistics (Math)", "Symplectic Geometry" };

    String[] mtURLs = { "math", "math.AG", "math.AT", "math.AP", "math.CT",
            "math.CA", "math.CO", "math.AC", "math.CV", "math.DG", "math.DS",
            "math.FA", "math.GM", "math.GN", "math.GT", "math.GR", "math.HO",
            "math.IT", "math.KT", "math.LO", "math.MP", "math.MG", "math.NT",
            "math.NA", "math.OA", "math.OC", "math.PR", "math.QA", "math.RT",
            "math.RA", "math.SP", "math.ST", "math.SG" };

    String[] mtShortItems = { "Math All", "Algebraic Geometry",
            "Algebraic Topology", "Analysis of PDEs", "Category Theory",
            "Classical Analysis of ODEs", "Combinatorics",
            "Commutative Algebra", "Complex Variables",
            "Differential Geometry", "Dynamical Systems",
            "Functional Analysis", "General Mathematics", "General Topology",
            "Geometric Topology", "Group Theory", "Math History and Overview",
            "Information Theory", "K-Theory and Homology", "Logic",
            "Mathematical Physics", "Metric Geometry", "Number Theory",
            "Numerical Analysis", "Operator Algebras",
            "Optimization and Control", "Probability", "Quantum Algebra",
            "Representation Theory", "Rings and Algebras", "Spectral Theory",
            "Statistics (Math)", "Symplectic Geometry" };

    String[] mpItems = { "Physics (Misc) All", "Accelerator Physics",
            "Atmospheric and Oceanic Physics", "Atomic Physics",
            "Atomic and Molecular Clusters", "Biological Physics",
            "Chemical Physics", "Classical Physics", "Computational Physics",
            "Data Analysis, Statistics, and Probability", "Fluid Dynamics",
            "General Physics", "Geophysics", "History of Physics",
            "Instrumentation and Detectors", "Medical Physics", "Optics",
            "Physics Education", "Physics and Society", "Plasma Physics",
            "Popular Physics", "Space Physics" };

    String[] mpURLs = { "physics", "physics.acc-ph", "physics.ao-ph",
            "physics.atom-ph", "physics.atm-clus", "physics.bio-ph",
            "physics.chem-ph", "physics.class-ph", "physics.comp-ph",
            "physics.data-an", "physics.flu-dyn", "physics.gen-ph",
            "physics.geo-ph", "physics.hist-ph", "physics.ins-det",
            "physics.med-ph", "physics.optics", "physics.ed-ph",
            "physics.soc-ph", "physics.plasm-ph", "physics.pop-ph",
            "physics.space-ph" };

    String[] mpShortItems = { "Physics (Misc) All", "Accelerator Physics",
            "Atmospheric and Oceanic Physics", "Atomic Physics",
            "Atomic and Molecular Clusters", "Biological Physics",
            "Chemical Physics", "Classical Physics", "Computational Physics",
            "Data Analysis, Statistics, and Probability", "Fluid Dynamics",
            "General Physics", "Geophysics", "History of Physics",
            "Instrumentation and Detectors", "Medical Physics", "Optics",
            "Physics Education", "Physics and Society", "Plasma Physics",
            "Popular Physics", "Space Physics" };

    String[] nlItems = { "Nonlinear Sciences All",
            "Adaptation and Self-Organizing Systems",
            "Cellular Automata and Lattice Gases", "Chaotic Dynamics",
            "Exactly Solvable and Integrable Systems",
            "Pattern Formation and Solitons" };

    String[] nlURLs = { "nlin", "nlin.AO", "nlin.CG", "nlin.CD", "nlin.SI",
            "nlin.PS" };

    String[] nlShortItems = { "Nonlinear Sciences",
            "Adaptation and Self-Organizing Systems",
            "Cellular Automata and Lattice Gases", "Chaotic Dynamics",
            "Exactly Solvable and Integrable Systems",
            "Pattern Formation and Solitons" };

    String[] qbItems = { "Quant. Biology All", "Biomolecules", "Cell Behavior",
            "Genomics", "Molecular Networks", "Neurons and Cognition",
            "Quant. Biology Other", "Populations and Evolutions",
            "Quantitative Methods", "Subcellular Processes",
            "Tissues and Organs" };

    String[] qbURLs = { "q-bio", "q-bio.BM", "q-bio.CB", "q-bio.GN",
            "q-bio.MN", "q-bio.NC", "q-bio.OT", "q-bio.PE", "q-bio.QM",
            "q-bio.SC", "q-bio.TO" };

    String[] qbShortItems = { "Quant. Bio. All", "Biomolecules",
            "Cell Behavior", "Genomics", "Molecular Networks",
            "Neurons and Cognition", "QB Other", "Populations and Evolutions",
            "Quantitative Methods", "Subcellular Processes",
            "Tissues and Organs" };

    String[] qfItems = { "Quant. Finance All", "Computational Finance",
            "General Finance", "Portfolio Management",
            "Pricing and Securities", "Risk Management", "Statistical Finance",
            "Trading and Market Microstructure" };

    String[] qfURLs = { "q-fin", "q-fin.CP", "q-fin.GN", "q-fin.PM",
            "q-fin.PR", "q-fin.RM", "q-fin.ST", "q-fin.TR" };

    String[] qfShortItems = { "Quant. Fin. All", "Computational Finance",
            "General Finance", "Portfolio Management",
            "Pricing and Securities", "Risk Management", "Statistical Finance",
            "Trading and Market Microstructure" };

    String[] stItems = { "Statistics All", "Stats. Applications",
            "Stats. Computation", "Machine Learning", "Stats. Methodology",
            "Stats. Theory" };

    String[] stURLs = { "stat", "stat.AP", "stat.CO", "stat.ML", "stat.ME",
            "stat.TH" };

    String[] stShortItems = { "Statistics All", "Stats. Applications",
            "Stats. Computation", "Machine Learning", "Stats. Methodology",
            "Stats. Theory" };

    private boolean applyMenuChoice(MenuItem item) {
        switch (item.getItemId()) {
        case ABOUT_ID:
            String str = getString(R.string.about_text);
            TextView wv = new TextView(this);
            wv.setPadding(16, 0, 16, 16);
            wv.setText(str);

            ScrollView scwv = new ScrollView(this);
            scwv.addView(wv);

            Dialog dialog = new Dialog(this) {
                public boolean onKeyDown(int keyCode, KeyEvent event) {
                    if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT)
                        this.dismiss();
                    return true;
                }
            };
            dialog.setTitle(R.string.about_arxiv_droid);
            dialog
                    .addContentView(scwv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            dialog.show();
            return (true);
        case HISTORY_ID:
            Intent myIntent = new Intent(this, HistoryWindow.class);
            startActivity(myIntent);
            return (true);
        case CLEAR_ID:
            deleteFiles();
            return (true);
        case PREF_ID:
            startActivity(new Intent(this, EditPreferences.class));
            return (true);
        case DONATE_ID:
            Intent goToMarket = null;
            goToMarket = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.jd.android.arXiv"));
            try {
                startActivity(goToMarket);
            } catch (Exception ef) {
                Toast.makeText(this, "Market Not Installed", Toast.LENGTH_SHORT).show();
            }
            return (true);
        }
        return (false);
    }

    private void deleteFiles() {
        File dir = new File("/sdcard/arXiv");

        String[] children = dir.list();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                String filename = children[i];
                File f = new File("/sdcard/arXiv/" + filename);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        File dir2 = new File("/emmc/arXiv");

        String[] children2 = dir2.list();
        if (children2 != null) {
            for (int i = 0; i < children2.length; i++) {
                String filename = children2[i];
                File f = new File("/emmc/arXiv/" + filename);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        dir2 = new File("/media/arXiv");

        children2 = dir2.list();
        if (children2 != null) {
            for (int i = 0; i < children2.length; i++) {
                String filename = children2[i];
                File f = new File("/media/arXiv/" + filename);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        Log.d("Arx","Opening Database 1");
        droidDB = new arXivDB(this);
        historys = droidDB.getHistory();

        for (History history : historys) {
            droidDB.deleteHistory(history.historyId);
        }
        droidDB.close();
        Log.d("Arx","Closed Database 1");

        Toast.makeText(this, "Deleted PDF history", Toast.LENGTH_SHORT).show();
    }

    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        Log.d("Arx","Opening Database 2");
        droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();

        int icount = 0;
        if (vFlag == 0) {
            for (Feed feed : favorites) {
                if (icount == info.position) {
                    droidDB.deleteFeed(feed.feedId);
                }
                icount++;
            }
            Thread t9 = new Thread() {
                public void run() {
                    updateWidget();
                }
            };
            t9.start();
        } else {
            if (mySourcePref == 0) {
                String tempquery = "search_query=cat:" + urls[info.position] + "*";
                String tempurl = "http://export.arxiv.org/api/query?" + tempquery
                      + "&sortBy=submittedDate&sortOrder=ascending";
                droidDB.insertFeed(shortItems[info.position], tempquery, tempurl,-1,-1);
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
        }

        droidDB.close();
        Log.d("Arx","Closed Database 2");

        updateFavList();

        return true;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int version = android.os.Build.VERSION.SDK_INT;

        if (version > 6) {
            setContentView(R.layout.mainnew);
        } else {
            setContentView(R.layout.mainold);
        }

        Resources res = getResources();

        thisActivity = this;

        header = (TextView) findViewById(R.id.theader);
        catList = (ListView) findViewById(R.id.catlist);
        favList = (ListView) findViewById(R.id.favlist);
        catList.setOnItemClickListener(this);
        favList.setOnItemClickListener(this);

        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        TabHost tabs = (TabHost) findViewById(R.id.tabhost);
        tabs.setup();

        if (version > 7) {
            View vi;
            vi = LayoutInflater.from(this).inflate(R.layout.my_tab_indicator,
                    tabs.getTabWidget(), false);

            ImageView tempimg = (ImageView) vi.findViewById(R.id.icon);
            TextView temptxt = (TextView) vi.findViewById(R.id.title);
            tempimg.setImageResource(R.drawable.cat);
            temptxt.setText("Categories");

            TabHost.TabSpec spec = tabs.newTabSpec("tag1");
            spec.setContent(R.id.catlist);
            spec.setIndicator(vi);
            tabs.addTab(spec);

            vi = LayoutInflater.from(this).inflate(R.layout.my_tab_indicator,
                    tabs.getTabWidget(), false);

            tempimg = (ImageView) vi.findViewById(R.id.icon);
            temptxt = (TextView) vi.findViewById(R.id.title);
            tempimg.setImageResource(R.drawable.fav);
            temptxt.setText("Favorites");

            spec = tabs.newTabSpec("tag2");
            spec.setContent(R.id.favlist);
            spec.setIndicator(vi);
            tabs.addTab(spec);

            TabWidget tabWidget = tabs.getTabWidget();
            for (int i = 0; i < tabWidget.getChildCount(); i++) {
                RelativeLayout tabLayout = (RelativeLayout) tabWidget
                        .getChildAt(i);
                tabLayout.setBackgroundDrawable(res
                        .getDrawable(R.drawable.my_tab_indicator));
            }
            try {
                Class[] mSetStripEnabledSignature = new Class[] { boolean.class };

                Method mSetStripEnabled = TabWidget.class.getMethod(
                        "setStripEnabled", mSetStripEnabledSignature);

                Object[] SEArgs = new Object[1];
                SEArgs[0] = Boolean.TRUE;

                mSetStripEnabled.invoke(tabWidget, SEArgs);

                Class[] mSetRightStripDrawableSignature = new Class[] { int.class };

                Method mSetRightStripDrawable = TabWidget.class.getMethod(
                        "setRightStripDrawable",
                        mSetRightStripDrawableSignature);

                SEArgs = new Object[1];
                SEArgs[0] = R.drawable.tab_bottom_right_v4;

                mSetRightStripDrawable.invoke(tabWidget, SEArgs);

                Method mSetLeftStripDrawable = TabWidget.class
                        .getMethod("setLeftStripDrawable",
                                mSetRightStripDrawableSignature);

                SEArgs = new Object[1];
                SEArgs[0] = R.drawable.tab_bottom_left_v4;

                mSetLeftStripDrawable.invoke(tabWidget, SEArgs);

            } catch (Exception ef) {
                Log.e("arXiv - ", "Strip fail: " + ef);
            }

        } else {
            TabHost.TabSpec spec = tabs.newTabSpec("tag1");
            spec.setContent(R.id.catlist);
            spec.setIndicator("Categories", res.getDrawable(R.drawable.cat));
            tabs.addTab(spec);
            spec = tabs.newTabSpec("tag2");
            spec.setContent(R.id.favlist);
            spec.setIndicator("Favorites", res.getDrawable(R.drawable.fav));
            tabs.addTab(spec);
        }

        catList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items));
        registerForContextMenu(catList);

        Log.d("Arx","Opening Database 3");
        droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();
        droidDB.close();
        Log.d("Arx","Closed Database 3");

        List<String> lfavorites = new ArrayList<String>();
        List<String> lunread = new ArrayList<String>();
        for (Feed feed : favorites) {
            String unreadString = "";
            if (feed.unread > 99) {
              unreadString = "99+";
            } else if (feed.unread == -2) {
              unreadString = "-";
            } else if (feed.unread <= 0) {
              unreadString = "0";
            } else if (feed.unread < 10) {
              unreadString = ""+feed.unread;
            } else {
              unreadString = ""+feed.unread;
            }
            lfavorites.add(feed.title);
            lunread.add(unreadString);
        }

        favoritesList = new String[lfavorites.size()];
        unreadList = new String[lfavorites.size()];

        lfavorites.toArray(favoritesList);
        lunread.toArray(unreadList);

        //favList.setAdapter(new ArrayAdapter<String>(this,
        //        android.R.layout.simple_list_item_1, lfavorites));
        favList.setAdapter(new myCustomAdapter());
        registerForContextMenu(favList);

        try {
            Intent myInIntent = getIntent();
            String mytype = myInIntent.getStringExtra("keywidget");

            if (mytype != null) {
                vFromWidget = true;
                tabs.setCurrentTabByTag("tag2");
            }
        } catch (Exception ef) {
            Log.e("arxiv","Failed to change tab "+ef);
        }

        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        mySourcePref=Integer.parseInt(prefs.getString("sourcelist", "0"));

    }

    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenuInfo menuInfo) {

        try {
        } catch (ClassCastException e) {
            return;
        }
        if (view.getId() == R.id.favlist) {
            menu.add(0, 1000, 0, R.string.remove_favorites);
            vFlag = 0;
        } else {
            menu.add(0, 1000, 0, R.string.add_favorites);
            vFlag = 1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return (super.onCreateOptionsMenu(menu));
    }

    public void onItemClick(AdapterView<?> a, View v, int position, long id) {

        if (a.getId() == R.id.favlist) {

            String tempname = "";
            String tempurl = "";
            String tempquery = "";

            Log.d("Arx","Opening Database 4");
            droidDB = new arXivDB(this);
            favorites = droidDB.getFeeds();
            droidDB.close();
            Log.d("Arx","Closed Database 4");

            int icount = 0;
            for (Feed feed : favorites) {
                if (icount == position) {
                    tempquery = feed.title;
                    tempname = feed.shortTitle;
                    tempurl = feed.url;
                }
                icount++;
            }

            // JRD - What do we do here;
            if (tempurl.contains("query")) {
                Intent myIntent = new Intent(this, SearchListWindow.class);
                myIntent.putExtra("keyquery", tempname);
                myIntent.putExtra("keyname", tempquery);
                myIntent.putExtra("keyurl", tempurl);
                startActivity(myIntent);
            } else {
                Intent myIntent = new Intent(this, RSSListWindow.class);
                myIntent.putExtra("keyname", tempname);
                myIntent.putExtra("keyurl", tempurl);
                startActivity(myIntent);
            }

        } else {
            if (itemsFlag[position] == 0) {
                if (mySourcePref == 1) {
                    Intent myIntent = new Intent(this, RSSListWindow.class);
                    myIntent.putExtra("keyname", shortItems[position]);
                    myIntent.putExtra("keyurl", urls[position]);
                    startActivity(myIntent);
                } else {
                    Intent myIntent = new Intent(this, SearchListWindow.class);
                    myIntent.putExtra("keyname", shortItems[position]);
                    String tempquery = "search_query=cat:" + urls[position] + "*";
                    myIntent.putExtra("keyquery", tempquery);
                    String tempurl = "http://export.arxiv.org/api/query?"
                        + tempquery
                        + "&sortBy=submittedDate&sortOrder=ascending";
                    myIntent.putExtra("keyurl", tempurl);
                    startActivity(myIntent);
                }
            } else {
                Intent myIntent = new Intent(this, SubarXiv.class);
                myIntent.putExtra("keyname", shortItems[position]);

                switch (itemsFlag[position]) {
                case 1:
                    myIntent.putExtra("keyitems", asItems);
                    myIntent.putExtra("keyurls", asURLs);
                    myIntent.putExtra("keyshortitems", asShortItems);
                    break;
                case 2:
                    myIntent.putExtra("keyitems", cmItems);
                    myIntent.putExtra("keyurls", cmURLs);
                    myIntent.putExtra("keyshortitems", cmShortItems);
                    break;
                case 3:
                    myIntent.putExtra("keyitems", csItems);
                    myIntent.putExtra("keyurls", csURLs);
                    myIntent.putExtra("keyshortitems", csShortItems);
                    break;
                case 4:
                    myIntent.putExtra("keyitems", mtItems);
                    myIntent.putExtra("keyurls", mtURLs);
                    myIntent.putExtra("keyshortitems", mtShortItems);
                    break;
                case 5:
                    myIntent.putExtra("keyitems", mpItems);
                    myIntent.putExtra("keyurls", mpURLs);
                    myIntent.putExtra("keyshortitems", mpShortItems);
                    break;
                case 6:
                    myIntent.putExtra("keyitems", nlItems);
                    myIntent.putExtra("keyurls", nlURLs);
                    myIntent.putExtra("keyshortitems", nlShortItems);
                    break;
                case 7:
                    myIntent.putExtra("keyitems", qbItems);
                    myIntent.putExtra("keyurls", qbURLs);
                    myIntent.putExtra("keyshortitems", qbShortItems);
                    break;
                case 8:
                    myIntent.putExtra("keyitems", qfItems);
                    myIntent.putExtra("keyurls", qfURLs);
                    myIntent.putExtra("keyshortitems", qfShortItems);
                    break;
                case 9:
                    myIntent.putExtra("keyitems", stItems);
                    myIntent.putExtra("keyurls", stURLs);
                    myIntent.putExtra("keyshortitems", stShortItems);
                    break;
                }
                startActivity(myIntent);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        mySourcePref=Integer.parseInt(prefs.getString("sourcelist", "0"));

        Log.d("Arx","Opening Database 5");
        droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();
        droidDB.close();
        Log.d("Arx","Closed Database 5");

        if (!vFromWidget) {
            //Should check for new articles?
            Thread t10 = new Thread() {
                public void run() {
                    updateWidget();
                }
            };
            t10.start();
        }

        List<String> lfavorites = new ArrayList<String>();
        List<String> lunread = new ArrayList<String>();
        for (Feed feed : favorites) {
            String unreadString = "";
            if (feed.unread > 99) {
              unreadString = "99+";
            } else if (feed.unread == -2) {
              unreadString = "-";
            } else if (feed.unread <= 0) {
              unreadString = "0";
            } else if (feed.unread < 10) {
              unreadString = ""+feed.unread;
            } else {
              unreadString = ""+feed.unread;
            }
            lfavorites.add(feed.title);
            lunread.add(unreadString);
        }

        favoritesList = new String[lfavorites.size()];
        unreadList = new String[lfavorites.size()];
 
        lfavorites.toArray(favoritesList);
        lunread.toArray(unreadList);

        //favList.setAdapter(new ArrayAdapter<String>(this,
        //        android.R.layout.simple_list_item_1, lfavorites));
        favList.setAdapter(new myCustomAdapter());
        registerForContextMenu(favList);

    }

    private void populateMenu(Menu menu) {
        menu.add(Menu.NONE, ABOUT_ID, Menu.NONE, R.string.about_arxiv_droid);
        menu.add(Menu.NONE, HISTORY_ID, Menu.NONE, R.string.view_history);
        menu.add(Menu.NONE, CLEAR_ID, Menu.NONE, R.string.clear_history);
        menu.add(Menu.NONE, PREF_ID, Menu.NONE, R.string.preferences);
        menu.add(Menu.NONE, DONATE_ID, Menu.NONE, R.string.donate);
    }

    public void searchPressed(View buttoncover) {
        Intent myIntent = new Intent(this, SearchWindow.class);
        startActivity(myIntent);
    }

    public void updateFavList() {

        Log.d("Arx","Opening Database 6");
        droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();
        droidDB.close();
        Log.d("Arx","Closed Database 6");

        List<String> lfavorites = new ArrayList<String>();
        List<String> lunread = new ArrayList<String>();
        for (Feed feed : favorites) {
            String unreadString = "";
            if (feed.unread > 99) {
              unreadString = "99+";
            } else if (feed.unread == -2) {
              unreadString = "-";
            } else if (feed.unread <= 0) {
              unreadString = "0";
            } else if (feed.unread < 10) {
              unreadString = ""+feed.unread;
            } else {
              unreadString = ""+feed.unread;
            }
            lfavorites.add(feed.title);
            lunread.add(unreadString);
        }

        favoritesList = new String[lfavorites.size()];
        unreadList = new String[lfavorites.size()];

        lfavorites.toArray(favoritesList);
        lunread.toArray(unreadList);

        //favList.setAdapter(new ArrayAdapter<String>(this,
        //        android.R.layout.simple_list_item_1, lfavorites));
        favList.setAdapter(new myCustomAdapter());
        registerForContextMenu(favList);

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

        Log.d("Arx","Opening Database 7");
        droidDB = new arXivDB(thisActivity);
        List<Feed> favorites = droidDB.getFeeds();
        droidDB.close();
        Log.d("Arx","Closed Database 7");

        String favText = "";

        if (favorites.size() > 0) {
            boolean vUnreadChanged = false;
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
                        if (newArticles >= 0) {
                            tempViews.setTextViewText(R.id.number, ""+newArticles);
                        } else {
                            tempViews.setTextViewText(R.id.number, "0");
                        }
                        if (newArticles != feed.unread) {
                            vUnreadChanged = true;
                            arXivDB dDB = new arXivDB(thisActivity);
                            dDB.updateFeed(feed.feedId,feed.title,feed.shortTitle,feed.url,feed.count,newArticles);
                            dDB.close();
                        }
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

            if (vUnreadChanged) {
                handlerSetList.sendEmptyMessage(0);
            }

        }

    }

    class myCustomAdapter extends ArrayAdapter {

        myCustomAdapter() {
            super(arXiv.this, R.layout.favoritesrow, favoritesList);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View row=convertView;
            ViewHolder holder;

            if (row==null) {
                LayoutInflater inflater=getLayoutInflater();
                row=inflater.inflate(R.layout.favoritesrow, parent, false);
                holder=new ViewHolder();
                holder.text1=(TextView)row.findViewById(R.id.text1);
                holder.text2=(TextView)row.findViewById(R.id.text2);
                row.setTag(holder);
            } else {
                holder=(ViewHolder)row.getTag();
            }
            holder.text1.setText(unreadList[position]);
            holder.text2.setText(favoritesList[position]);
            return(row);

        }

        public class ViewHolder{
            public TextView text1;
            public TextView text2;
        }

    }

    private Handler handlerSetList = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            updateFavList();

        }
    };

}
