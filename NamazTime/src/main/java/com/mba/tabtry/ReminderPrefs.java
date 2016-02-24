package com.mba.tabtry;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Muhammad Bilal on 18/02/2016.
 */
public class ReminderPrefs extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.reminders);
    }
}
