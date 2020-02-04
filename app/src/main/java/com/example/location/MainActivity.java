package com.example.location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener , SensorEventListener {
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected LocationProvider locationProvider;
    protected Context context;
    protected Location location;
    TextView txtLat;
    TextView textLongitude;
    TextView textLatitude;
    TextView textAltitude;
    TextView textAddress;
    TextView placeholder;
    String lat;
    String provider;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled, hasAltitude;
    private static final String TAG = "Location";
    private static final long min_distance = 10;
    private static final long min_time = 1000*60*1;

    private SensorManager sensorManager;
    private Sensor sensorLight;
    private TextView textLight;
    private String lightValue;

    ArrayList<String> locations;

    Button save;

    String listAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //txtLat = (TextView) findViewById(R.id.textview1);
        textLongitude = (TextView) findViewById(R.id.label_longitude);
        textLatitude = (TextView) findViewById(R.id.label_latitude);
        textAltitude = (TextView) findViewById(R.id.label_altitude);
        textAddress = (TextView) findViewById(R.id.label_address);
        placeholder = (TextView) findViewById(R.id.label_locations);
        textLight = findViewById(R.id.label_light);

        locations = new ArrayList<>();

        sensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);

        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        String sensor_error = getResources().getString(R.string.error_no_sensor);
        //if (sensorAccelerometer == null) { sensorAccelerometer.setText(sensor_error); }

        //locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }

        if(location ==null){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,min_time, min_distance, this);
            Log.d(TAG,"null1 ");
        }

        if(locationManager!=null){
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d(TAG,"null2 ");

        }


        save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // if (!listAddress.getText().toString().isEmpty()) {
                    File file = new File(MainActivity.this.getFilesDir(), "text");
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    try {
                        File gpxfile = new File(file, "sample");
                        FileWriter writer = new FileWriter(gpxfile);
                        writer.append(listAddress.toString());
                        writer.flush();
                        writer.close();
                        placeholder.setText(readFile());
                        Toast.makeText(MainActivity.this, "Saved your text", Toast.LENGTH_LONG).show();
                    } catch (Exception e) { }
                }
            //}
        });


    }

    private String readFile() {
        File fileEvents = new File(MainActivity.this.getFilesDir() + "/text/sample");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileEvents));
            String line;
            while ((line = br.readLine()) != null) {
                locations.add(line);
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) { }
        String result = Arrays.toString(new ArrayList[]{locations});
        return result;
    }

    @Override
    protected void onStart(){
        super.onStart();
        if (sensorLight != null) {
            sensorManager.registerListener(this, sensorLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();

        // Unregister all sensor listeners in this callback so they don't
        // continue to use resources when the app is paused.
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //txtLat = (TextView) findViewById(R.id.textview1);
        //txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        double altitude = location.getAltitude();
        textLongitude.setText(getResources().getString(R.string.label_longitude, longitude));
        Log.d(TAG,"Longitude: " + longitude);
        textLatitude.setText(getResources().getString(R.string.label_latitude, latitude));
        Log.d(TAG, "Latitude: " + latitude);
        textAltitude.setText((getResources().getString(R.string.label_altitude, altitude)));
        Log.d(TAG, "Altitude: " + altitude);
        getAddress(latitude,longitude);



    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }


    public void getAddress(Double latitude, Double longitude){
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            /*
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();
             */
            textAddress.setText((getResources().getString(R.string.label_address, add)));
            locations.add(add+"\n" + "Light: " + lightValue +" lx\n");
            listAddress = "";

            for (String s  : locations){
                listAddress += s + "\n" ;
            }

            placeholder.setText(listAddress);
            //placeholder.setText((getResources().getString(R.string.label_locations, listAddress)));
            Log.d(TAG, "Address" + locations);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int sensorType = sensorEvent.sensor.getType();

        // The new data value of the sensor.  Both the light and proximity
        // sensors report one value at a time, which is always the first
        // element in the values array.
        float currentValue = sensorEvent.values[0];

        switch (sensorType) {
            case Sensor.TYPE_LIGHT:

                // Set the proximity sensor text view to the light sensor
                // string from the resources, with the placeholder filled in.
                textLight.setText(getResources().getString(
                        R.string.label_light, currentValue ));

                lightValue = String.valueOf(currentValue);

                Log.d(TAG, "Light!!");
                break;
            default:
                // do nothing
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
