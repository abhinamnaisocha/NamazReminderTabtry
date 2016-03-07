package com.mba.tabtry;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import net.alhazmy13.hijridatepicker.HijriCalendarDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {


    double lat, lon;
    SharedPreferences.Editor editor;
    SharedPreferences sharedprefs;
    Boolean fajr, zhr, asr, maghrib, isha;
    String reqNamazTime, hour, remaining, mins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mba.tabtry.R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(com.mba.tabtry.R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedprefs = getSharedPreferences("dirPref", 0);
        editor = sharedprefs.edit();
        if ((sharedprefs.getString("latitude", "") == "" && sharedprefs.getString("longitude", "") == "") || (sharedprefs.getString("latitude", "0") == "" && sharedprefs.getString("longitude", "") == "0")) {
            Intent i = new Intent(MainActivity.this, LocationTracker.class);
            startActivity(i);

        } else {
            Toast.makeText(MainActivity.this, "LOOOOOOOOOOOL", Toast.LENGTH_SHORT).show();
            lat = Double.parseDouble(sharedprefs.getString("latitude", ""));
            lon = Double.parseDouble(sharedprefs.getString("longitude", ""));
        }


        Toast.makeText(MainActivity.this, "Lat at main:" + lat + " Lon:" + lon, Toast.LENGTH_SHORT).show();


        TabLayout tabLayout = (TabLayout) findViewById(com.mba.tabtry.R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Namaz"));
        tabLayout.addTab(tabLayout.newTab().setText("Calendar"));
        tabLayout.addTab(tabLayout.newTab().setText("Qibla"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        final ViewPager viewPager = (ViewPager) findViewById(com.mba.tabtry.R.id.pager);
        final PageAdapter adapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 1) {
                    new HijriCalendarDialog.Builder(MainActivity.this).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    new HijriCalendarDialog.Builder(MainActivity.this).show();
                }
            }
        });
    }

    private void prayerReminders() {
        String prayerName;
        if (fajr) {
            prayerName = "fajr";
            gettingRequestedTime(0, prayerName);
        }
        if (zhr) {
            prayerName = "zhr";
            gettingRequestedTime(2, prayerName);

        }

        if (asr) {
            prayerName = "asr";
            gettingRequestedTime(3, prayerName);

        }

        if (maghrib) {
            prayerName = "maghrib";
            gettingRequestedTime(5, prayerName);

        }

        if (isha) {
            prayerName = "isha";
            gettingRequestedTime(6, prayerName);

        }

    }

    private void gettingRequestedTime(int i, String prayerName) {
        reqNamazTime = getReqTime(i);
        if (reqNamazTime.contains("am")) {

            StringTokenizer tokens = new StringTokenizer(reqNamazTime, ":");
            hour = tokens.nextToken();
            if (hour == "12") {
                hour = "00";
            }
            remaining = tokens.nextToken();
            tokens = new StringTokenizer(remaining, " ");
            mins = tokens.nextToken();
            Toast.makeText(MainActivity.this, hour + ":" + mins, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MainActivity.this, hour + ":" + mins, Toast.LENGTH_SHORT).show();
        } else {
            StringTokenizer tokens = new StringTokenizer(reqNamazTime, ":");
            hour = tokens.nextToken();
            mins = tokens.nextToken();
            Toast.makeText(MainActivity.this, hour + ":" + mins, Toast.LENGTH_SHORT).show();
        }

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(mins));
        calendar.set(Calendar.SECOND, 00);
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

        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Log.d("Bundle has name", prayerName + "  " + calendar.getTime());

    }

    //Gets the values which namaz checkbox is checked
    private void gettingReminderPrefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        fajr = preferences.getBoolean("fajr", false);
        zhr = preferences.getBoolean("zhr", false);
        asr = preferences.getBoolean("asr", false);
        maghrib = preferences.getBoolean("maghrib", false);
        isha = preferences.getBoolean("isha", false);

    }


    //gets the location of the user by using GPS tracker Class

    //Gets the requested namaz time 0 fajr,2 zhr,3 asr,5 maghrib,6 isha
    private String getReqTime(int i) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        boolean timeFormat = preferences.getBoolean("TimeFormat", false);
        String calcMethod = preferences.getString("CalMethod", "0");
        String juriMethod = preferences.getString("JuriMethod", "0");
        String latitudeMethod = preferences.getString("latitudeMethod", "3");

        double latitude = lat;
        double longitude = lon;
        Toast.makeText(MainActivity.this, "latitude: " + latitude + " Longitude: " + longitude, Toast.LENGTH_LONG).show();
        Calendar calendar = Calendar.getInstance();
        double timezone = (Calendar.getInstance().getTimeZone()
                .getOffset(Calendar.getInstance().getTimeInMillis()))
                / (1000 * 60 * 60);
        PrayerCalculator prayers = new PrayerCalculator();


        //Timeformat from Shared Preferences
        if (timeFormat == false)
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

        String reqTime = prayerTimes.get(i).toString();
        return reqTime;
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
        Toast.makeText(MainActivity.this, "Canceled REQ CODE " + reqCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedprefs = getSharedPreferences("dirPref", 0);
        editor = sharedprefs.edit();
        if ((sharedprefs.getString("latitude", "") == "" && sharedprefs.getString("longitude", "") == "") || (sharedprefs.getString("latitude", "0") == "" && sharedprefs.getString("longitude", "") == "0")) {
            Intent i = new Intent(MainActivity.this, LocationTracker.class);
            startActivity(i);

        } else {
            Toast.makeText(MainActivity.this, "LOOOOOOOOOOOL", Toast.LENGTH_SHORT).show();
            lat = Double.parseDouble(sharedprefs.getString("latitude", ""));
            lon = Double.parseDouble(sharedprefs.getString("longitude", ""));
        }
        gettingReminderPrefs();
        prayerReminders();
        cancelAlarm();
    }
}
