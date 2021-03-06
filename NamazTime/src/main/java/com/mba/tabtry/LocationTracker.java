package com.mba.tabtry;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LocationTracker extends Activity implements LocationListener {

    String provider;
    Button refreshBnt;
    LocationManager lm;
    double lat, lon, alt;
    TextView late, longi;
    SharedPreferences sharedprefs;
    SharedPreferences.Editor editor;
    Location location;
     ProgressDialog dial,dialog;

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Exit Application?");
        alertDialogBuilder
                .setMessage("Click yes to exit!")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker_activity);

        refreshBnt = (Button) findViewById(R.id.Location);
        late = (TextView) findViewById(R.id.latitude);
        longi = (TextView) findViewById(R.id.longitude);
         dialog = ProgressDialog.show(LocationTracker.this,
                "Please Wait... ", "Getting Location... ", false, true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        new CountDownTimer(5000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {

                dialog.dismiss();
            }
        }.start();

        sharedprefs = getSharedPreferences("dirPref", 0);
        editor = sharedprefs.edit();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        } else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this.getBaseContext(), "PROVIDE GPS", Toast.LENGTH_SHORT).show();
            return;
        }


        if (location != null) {


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {

                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    alt = location.getAltitude();
                    late.setText("Latitude: " + String.valueOf(location.getLatitude()));
                    longi.setText("Longitude:" + String.valueOf(location.getAltitude()));
                    editor.putString("latitude", "" + lat);
                    editor.putString("longitude", "" + lon);
                    editor.putString("altitude", "" + alt);
                    editor.apply();
                    finish();
                }
            }, 5000);

        } else {

            if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && !lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) && !lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                showSettingsAlert();


            dial = ProgressDialog.show(LocationTracker.this,
                    "Please Wait... ", "Getting Location... ", false, true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            new CountDownTimer(5000, 5000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    if (location == null) {
                        Toast.makeText(LocationTracker.this, "Unable to find Location Please check settings", Toast.LENGTH_LONG).show();
                    } else {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        alt = location.getAltitude();
                        late.setText("Latitude: " + String.valueOf(location.getLatitude()));
                        longi.setText("Longitude:" + String.valueOf(location.getLongitude()));
                    }
                    dial.dismiss();
                }
            }.start();


        }

        refreshBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent i = new Intent(LocationTracker.this, LocationTracker.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dial != null)
            dial.dismiss();
        if (dialog != null)
            dialog.dismiss();

        finish();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 1, this);
        } else if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 500, 1, this);
        } else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {

        late.setText("Latitude: " + String.valueOf(location.getLatitude()));
        longi.setText("Longitude:" + String.valueOf(location.getLongitude()));
        editor.putString("latitude", String.valueOf(location.getLatitude()));
        editor.putString("longitude", String.valueOf(location.getLongitude()));
        editor.putString("altitude", String.valueOf(location.getAltitude()));
        editor.apply();


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(LocationTracker.this, "Unable to set your location", Toast.LENGTH_SHORT)
                        .show();
                dialog.cancel();
                finish();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}

