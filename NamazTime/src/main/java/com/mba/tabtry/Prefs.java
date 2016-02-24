package com.mba.tabtry;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Muhammad Bilal on 16/02/2016.
 */
public class Prefs extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.prefrs);
    }
}
