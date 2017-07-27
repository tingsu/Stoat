package org.nerdcircus.android.klaxon;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/* loosely-based on ChangelogActivity from http://foursquared.googlecode.com */

public class Changelog extends Activity {

    private static final String CHANGELOG_HTML_FILE = 
        "file:///android_asset/changelog-en.html";
    
    private WebView mWebViewChanges;
    
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changelog);
        mWebViewChanges = (WebView) findViewById(R.id.changelog_webview);
        mWebViewChanges.loadUrl(CHANGELOG_HTML_FILE); 
    }
}
