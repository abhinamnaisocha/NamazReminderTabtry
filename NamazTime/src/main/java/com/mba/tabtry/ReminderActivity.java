package com.mba.tabtry;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;


public class ReminderActivity extends Activity {

    TextView namazName, date;
    String hour, mins, reqNamazTime, remaining;
    Double lat, lon;
    Button okay;
    MediaPlayer mediaPlayer;
    SharedPreferences sharedprefs, preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_layout);
        Alarmwakelock.acquire(this);

        final Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mediaPlayer = MediaPlayer.create(this, R.raw.adzanmekkah);
        mediaPlayer.start();

        sharedprefs = getSharedPreferences("dirPref", 0);

        Bundle bundle = getIntent().getExtras();
        namazName = (TextView) findViewById(R.id.namazNametv);
        namazName.setText(bundle.getString("prayer"));


        try {
            lat = Double.parseDouble(sharedprefs.getString("latitude", ""));
            lon = Double.parseDouble(sharedprefs.getString("longitude", ""));
        } catch (NullPointerException n) {
            n.printStackTrace();
        }

        String currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
        date = (TextView) findViewById(R.id.datetv);
        date.setText(String.valueOf(currentDateTime));


        switch (bundle.getString("prayer")) {
            case "Fajr":
                gettingRequestedTime(0, bundle.getString("prayer"));
                break;

            case "Dhuhr":
                gettingRequestedTime(2, bundle.getString("prayer"));
                break;

            case "Asr":
                gettingRequestedTime(3, bundle.getString("prayer"));
                break;

            case "Maghrib":
                gettingRequestedTime(5, bundle.getString("prayer"));
                break;

            case "Isha":
                gettingRequestedTime(6, bundle.getString("prayer"));
                break;
        }

        new CountDownTimer(207000, 207000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Toast.makeText(ReminderActivity.this, "released", Toast.LENGTH_SHORT).show();
                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                win.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                win.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                Alarmwakelock.release();
            }
        }.start();


        okay = (Button) findViewById(R.id.okaybtn);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alarmwakelock.release();
                mediaPlayer.stop();
                finish();
            }
        });
    }

    private void gettingRequestedTime(int i, String prayerName) {
        reqNamazTime = getReqTime(i);
        if (reqNamazTime.contains("am")) {
            StringTokenizer tokens = new StringTokenizer(reqNamazTime, ":");
            hour = tokens.nextToken();
            remaining = tokens.nextToken();
            tokens = new StringTokenizer(remaining, " ");
            mins = tokens.nextToken();
            Toast.makeText(this, hour + ":" + mins, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, hour + ":" + mins, Toast.LENGTH_SHORT).show();
        } else {
            StringTokenizer tokens = new StringTokenizer(reqNamazTime, ":");
            hour = tokens.nextToken();
            mins = tokens.nextToken();
            Toast.makeText(this, hour + ":" + mins, Toast.LENGTH_SHORT).show();
        }

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(mins));
        calendar.set(Calendar.SECOND, 0);

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        switch (prayerName) {
            case "Fajr":
                calendar.add(Calendar.MINUTE, Integer.parseInt(preferences.getString("fajrTime", "0")));
                break;
            case "Dhuhr":
                calendar.add(Calendar.MINUTE, Integer.parseInt(preferences.getString("zhrTime", "0")));
                break;
            case "Asr":
                calendar.add(Calendar.MINUTE, Integer.parseInt(preferences.getString("asrTime", "0")));
                break;
            case "Maghrib":
                calendar.add(Calendar.MINUTE, Integer.parseInt(preferences.getString("maghribTime", "0")));
                break;
            case "Isha":
                calendar.add(Calendar.MINUTE, Integer.parseInt(preferences.getString("ishaTime", "0")));
                break;
        }


        Bundle bundle = new Bundle();
        bundle.putString("prayer", prayerName);
        bundle.putString("hour", hour);
        bundle.putString("mins", mins);
        bundle.putString("lat", String.valueOf(lat));
        bundle.putString("lon", String.valueOf(lon));
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReciever.class);
        intent.putExtras(bundle);
        PendingIntent pendingIntent
                = PendingIntent.getBroadcast(this,
                i, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Log.d("Bundle has name", prayerName + "  " + calendar.getTime());

    }

    public String getReqTime(int i) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        boolean timeFormat = preferences.getBoolean("TimeFormat", false);
        String calcMethod = preferences.getString("CalMethod", "0");
        String juriMethod = preferences.getString("JuriMethod", "0");
        String latitudeMethod = preferences.getString("latitudeMethod", "3");

        double latitude = lat;
        double longitude = lon;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        double timezone = (Calendar.getInstance().getTimeZone()
                .getOffset(Calendar.getInstance().getTimeInMillis()))
                / (1000 * 60 * 60);
        PrayerCalculator prayers = new PrayerCalculator();


        //Timeformat from Shared Preferences
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
    public void onBackPressed() {

    }
}
