package com.mba.tabtry;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {


    double lat, lon;
    SharedPreferences sharedprefs;
    Boolean fajr, zhr, asr, maghrib, isha;
    String reqNamazTime, hour, remaining, mins;
    int fajrMin, zhrMin, asrMin, maghribMin, ishaMin;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mba.tabtry.R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(com.mba.tabtry.R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedprefs = getSharedPreferences("dirPref", 0);
        if ((sharedprefs.getString("latitude", "").equals("") && sharedprefs.getString("longitude", "").equals("")) || (sharedprefs.getString("latitude", "0").equals("0") && sharedprefs.getString("longitude", "").equals("0"))) {
            Intent i = new Intent(MainActivity.this, LocationTracker.class);
            startActivity(i);

        } else {
            lat = Double.parseDouble(sharedprefs.getString("latitude", ""));
            lon = Double.parseDouble(sharedprefs.getString("longitude", ""));
        }


        TabLayout tabLayout = (TabLayout) findViewById(com.mba.tabtry.R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Namaz"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);


        final ViewPager viewPager = (ViewPager) findViewById(com.mba.tabtry.R.id.pager);
        final PageAdapter adapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void prayerReminders() {
        String prayerName;
        if (fajr) {
            prayerName = "Fajr";
            gettingRequestedTime(0, prayerName);
        }
        if (zhr) {
            prayerName = "Dhuhr";
            gettingRequestedTime(2, prayerName);

        }

        if (asr) {
            prayerName = "Asr";
            gettingRequestedTime(3, prayerName);

        }

        if (maghrib) {
            prayerName = "Maghrib";
            gettingRequestedTime(5, prayerName);

        }

        if (isha) {
            prayerName = "Isha";
            gettingRequestedTime(6, prayerName);

        }

    }

    private void gettingRequestedTime(int i, String prayerName) {
        reqNamazTime = getReqTime(i);
        if (reqNamazTime.contains("am")) {

            StringTokenizer tokens = new StringTokenizer(reqNamazTime, ":");
            hour = tokens.nextToken();
            if (hour.equals("12")) {
                hour = "00";
            }
            remaining = tokens.nextToken();
            tokens = new StringTokenizer(remaining, " ");
            mins = tokens.nextToken();
        } else if (reqNamazTime.contains("pm")) {
            StringTokenizer tokens = new StringTokenizer(reqNamazTime, ":");
            hour = tokens.nextToken();
            if (Integer.parseInt(hour) < 12) {
                int hours = Integer.parseInt(hour) + 12;
                hour = String.valueOf(hours);
            }
            remaining = tokens.nextToken();
            tokens = new StringTokenizer(remaining, " ");
            mins = tokens.nextToken();
        } else {
            StringTokenizer tokens = new StringTokenizer(reqNamazTime, ":");
            hour = tokens.nextToken();
            mins = tokens.nextToken();
        }

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(mins));
        calendar.set(Calendar.SECOND, 0);

        switch (prayerName) {
            case "Fajr":
                calendar.add(Calendar.MINUTE, fajrMin);
                break;
            case "Dhuhr":
                calendar.add(Calendar.MINUTE, zhrMin);
                break;
            case "Asr":
                calendar.add(Calendar.MINUTE, asrMin);
                break;
            case "Maghrib":
                calendar.add(Calendar.MINUTE, maghribMin);
                break;
            case "Isha":
                calendar.add(Calendar.MINUTE, ishaMin);
                break;
        }
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        Bundle bundle = new Bundle();
        bundle.putString("prayer", prayerName);
        bundle.putString("hour", hour);
        bundle.putString("mins", mins);
        bundle.putString("lat", String.valueOf(lat));
        bundle.putString("lon", String.valueOf(lon));
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getBaseContext(), ReminderReciever.class);
        intent.putExtras(bundle);
        PendingIntent pendingIntent
                = PendingIntent.getBroadcast(getBaseContext(),
                i, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);


        Log.d("Bundle has name", prayerName + "  " + calendar.getTime());

    }

    //Gets the values which namaz checkbox is checked
    private void gettingReminderPrefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = preferences.edit();
        fajr = preferences.getBoolean("fajr", false);
        try {
            fajrMin = Integer.parseInt(preferences.getString("fajrTime", "00"));
        } catch (NumberFormatException e) {
            fajrMin = 0;
            editor.putString("fajrTime", "0");

        }
        zhr = preferences.getBoolean("zhr", false);
        try {
            zhrMin = Integer.parseInt(preferences.getString("zhrTime", "00"));
        } catch (NumberFormatException e) {
            zhrMin = 0;
            editor.putString("zhrTime", "0");
            editor.apply();

        }
        asr = preferences.getBoolean("asr", false);
        try {
            asrMin = Integer.parseInt(preferences.getString("asrTime", "00"));
        } catch (NumberFormatException e) {
            asrMin = 0;
            editor.putString("asrTime", "0");
            editor.apply();

        }
        maghrib = preferences.getBoolean("maghrib", false);
        try {
            maghribMin = Integer.parseInt(preferences.getString("maghribTime", "00"));
        } catch (NumberFormatException e) {
            maghribMin = 0;
            editor.putString("maghribTime", "0");
            editor.apply();

        }
        isha = preferences.getBoolean("isha", false);
        try {
            ishaMin = Integer.parseInt(preferences.getString("ishaTime", "00"));
        } catch (NumberFormatException e) {
            ishaMin = 0;
            editor.putString("ishaTime", "0");
            editor.apply();

        }
    }


    //Gets the requested namaz time 0 fajr,2 zhr,3 asr,5 maghrib,6 isha
    private String getReqTime(int i) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        boolean timeFormat = preferences.getBoolean("TimeFormat", false);
        String calcMethod = preferences.getString("CalMethod", "0");
        String juriMethod = preferences.getString("JuriMethod", "0");
        String latitudeMethod = preferences.getString("latitudeMethod", "3");

        double latitude = lat;
        double longitude = lon;
        Calendar calendar = Calendar.getInstance();
        double timezone = (Calendar.getInstance().getTimeZone()
                .getOffset(Calendar.getInstance().getTimeInMillis()))
                / (1000 * 60 * 60);
        PrayerCalculator prayers = new PrayerCalculator();


        //Time format from Shared Preferences
        if (!timeFormat)
            prayers.setTimeFormat(prayers.Time12);
        else {
            prayers.setTimeFormat(prayers.Time24);

        }


        //Calculation Method form the Shared Preferences
        switch (calcMethod) {
            case "0":
                prayers.setCalcMethod(prayers.Karachi);
                break;

            case "1":
                prayers.setCalcMethod(prayers.ISNA);
                break;

            case "2":
                prayers.setCalcMethod(prayers.MWL);
                break;

            case "3":
                prayers.setCalcMethod(prayers.Makkah);
                break;

            case "4":
                prayers.setCalcMethod(prayers.Jafari);
                break;

            case "5":
                prayers.setCalcMethod(prayers.Egypt);
                break;

            case "6":
                prayers.setCalcMethod(prayers.Tehran);
                break;
        }

        //Juristic Method for Asr Time calculation
        switch (juriMethod) {
            case "0":
                prayers.setAsrJuristic(prayers.Shafii);
                break;
            case "1":
                prayers.setAsrJuristic(prayers.Hanafi);
                break;
        }


        switch (latitudeMethod) {
            case "0":
                prayers.setAdjustHighLats(prayers.None);
                break;

            case "1":
                prayers.setAdjustHighLats(prayers.MidNight);
                break;

            case "2":
                prayers.setAdjustHighLats(prayers.OneSeventh);
                break;

            case "3":
                prayers.setAdjustHighLats(prayers.AngleBased);
                break;
        }

        int[] offsets = {0, 0, 0, 0, 0, 0, 0};
        prayers.tune(offsets);


        ArrayList prayerTimes = prayers.getPrayerTimes(calendar, latitude,
                longitude, timezone);

        return prayerTimes.get(i).toString();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent("com.mba.tabtry.PREFS");
            startActivity(i);
        } else if (id == R.id.action_reminders) {
            Intent i = new Intent("com.mba.tabtry.REMINDERPREFS");
            startActivity(i);
        } else if (id == R.id.action_location) {
            Intent i = new Intent(MainActivity.this, LocationTracker.class);
            startActivity(i);
        } else if (id == R.id.action_exits) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    //Passes the Request code to cancelingAlarm Method//
    private void cancelAlarm() {
        if (!fajr) {
            cancelingAlarm(0);
        }
        if (!zhr) {
            cancelingAlarm(2);
        }
        if (!asr) {
            cancelingAlarm(3);
        }
        if (!maghrib) {
            cancelingAlarm(5);
        }
        if (!isha) {
            cancelingAlarm(6);
        }
    }


    //Cancel the alarm on the basis of recieved request code
    private void cancelingAlarm(int reqCode) {

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReciever.class);
        PendingIntent pendingIntent
                = PendingIntent.getBroadcast(this,
                reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pendingIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedprefs = getSharedPreferences("dirPref", 0);
        if ((sharedprefs.getString("latitude", "").equals("") && sharedprefs.getString("longitude", "").equals("")) || (sharedprefs.getString("latitude", "0").equals("0") && sharedprefs.getString("longitude", "").equals("0"))) {
            Intent i = new Intent(MainActivity.this, LocationTracker.class);
            startActivity(i);

        } else {
            lat = Double.parseDouble(sharedprefs.getString("latitude", ""));
            lon = Double.parseDouble(sharedprefs.getString("longitude", ""));
        }
        gettingReminderPrefs();
        prayerReminders();
        cancelAlarm();
    }
}
