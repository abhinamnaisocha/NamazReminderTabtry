package com.mba.tabtry;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class QiblaDirectionFragment extends Fragment implements SensorEventListener {


    Location currentLocation = new Location(""), MeccaLocation = new Location("");
    private ImageView image, arrowIv;
    private float currentDegree = 0f;
    double arrowStarting;
    private SensorManager mSensorManager;
    TextView tvHeading;
    SharedPreferences sharedprefs;
    SharedPreferences.Editor editor;
    double longitude, latitude, altitude;

    public QiblaDirectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(com.mba.tabtry.R.layout.fragment_qibla_direction, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedprefs = getActivity().getSharedPreferences("dirPref", 0);

        getLocation();


        MeccaLocation.setLatitude(21.427378);
        MeccaLocation.setLongitude(39.814838);
        MeccaLocation.setAltitude(302.00);

        image = (ImageView) view.findViewById(R.id.imageViewCompass);
        arrowIv = (ImageView) view.findViewById(R.id.arrow_IV);
        // TextView that will tell the user what degree is he heading

        tvHeading = (TextView) view.findViewById(R.id.tvHeading);


        // initialize your android device sensor capabilities

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    }

    float[] mGravity;
    float[] mGeomagnetic;

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);

        tvHeading.setText(Float.toString(degree));


        // create a rotation animation (reverse turn degree degrees)

        RotateAnimation ra = new RotateAnimation(

                currentDegree,

                -degree,

                Animation.RELATIVE_TO_SELF, 0.5f,

                Animation.RELATIVE_TO_SELF,

                0.5f);

        // If we don't have a Location, we break out


        float azimuth = event.values[0];
        float baseAzimuth = azimuth;

        GeomagneticField geoField = new GeomagneticField(Double
                .valueOf(currentLocation.getLatitude()).floatValue(), Double
                .valueOf(currentLocation.getLongitude()).floatValue(),
                Double.valueOf(currentLocation.getAltitude()).floatValue(),
                System.currentTimeMillis());

        azimuth -= geoField.getDeclination(); // converts magnetic north into true north

        // Store the bearingTo in the bearTo variable
        float bearTo = currentLocation.bearingTo(MeccaLocation);

        // If the bearTo is smaller than 0, add 360 to get the rotation clockwise.
        if (bearTo < 0) {
            bearTo = bearTo + 360;
        }

        //This is where we choose to point it
        float direction = bearTo - azimuth;

        // If the direction is smaller than 0, add 360 to get the rotation clockwise.
        if (direction < 0) {
            direction = direction + 360;
        }

        rotateImageView(arrowIv, R.drawable.arroww, direction);
    }

    private void rotateImageView(ImageView imageView, int drawable, float rotate) {

        // Decode the drawable into a bitmap
        Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(),
                drawable);

        // Get the width/height of the drawable
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) this.getActivity().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        int width = bitmapOrg.getWidth(), height = bitmapOrg.getHeight();

        // Initialize a new Matrix
        Matrix matrix = new Matrix();

        // Decide on how much to rotate
        rotate = rotate % 360;

        // Actually rotate the image
        matrix.postRotate(rotate, width, height);

        // recreate the new Bitmap via a couple conditions
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
        //BitmapDrawable bmd = new BitmapDrawable( rotatedBitmap );

        //imageView.setImageBitmap( rotatedBitmap );
        imageView.setImageDrawable(new BitmapDrawable(getResources(), rotatedBitmap));
        imageView.setScaleType(ImageView.ScaleType.CENTER);
    }


    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),

                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    void getLocation() {
        if (sharedprefs.getString("latitude", "") != "" || sharedprefs.getString("latitude", "") != "0") {

            latitude = Double.parseDouble(sharedprefs.getString("latitude", "0"));
            longitude = Double.parseDouble(sharedprefs.getString("longitude", "0"));
            altitude = Double.parseDouble(sharedprefs.getString("altitude", "0"));
            Toast.makeText(getContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            // thhese four line to calculate angle from user to Mecca.
            float lonDelta = (float) (longitude - 39.814838);
            float y = (float) (Math.sin(lonDelta) * Math.cos(21.427378));
            float x = (float) (Math.cos(latitude) * Math.sin(21.427378) - Math.sin(latitude) * Math.cos(21.427378) * Math.cos(lonDelta));
            float bearing = (float) Math.toDegrees(Math.atan2(y, x));
            ;
            arrowStarting = bearing;

            Log.e("====" + bearing + "====",
                    "Your Location is Lat: " + latitude +
                            "\nLong: " + longitude +
                            "\nAlt: " + altitude +
                            "\n lat: " + latitude * (Math.PI / 180) +
                            "\nlog: " + longitude * (Math.PI / 180) +
                            "\n lat: " + sharedprefs.getString("latitude", "0") + "" +
                            " \nlog: " + sharedprefs.getString("longitude", "0"));
            currentLocation.setLatitude(latitude);
            currentLocation.setLongitude(longitude);
            currentLocation.setAltitude(altitude);
        } else {
            Toast.makeText(getContext(), "If your location has changed press get location button", Toast.LENGTH_LONG).show();

        }
    }
}