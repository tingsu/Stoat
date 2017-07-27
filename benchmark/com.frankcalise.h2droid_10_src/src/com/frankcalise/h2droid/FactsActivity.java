package com.frankcalise.h2droid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class FactsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set up main layout
        setContentView(R.layout.activity_water_facts);
        
        // Get facts from string array resource and
        // create string for text view
        String[] facts = getResources().getStringArray(R.array.water_facts);
        String factsText = "";
        
        int numFacts = facts.length;
        for (int i = 0; i < numFacts; i++) {
        	factsText += facts[i] + "\n\n";
        }
        
        // Populate the text view with the
        // string of facts
        final TextView tv = (TextView)findViewById(R.id.facts_tv);
        tv.setText(factsText);
    }
}
