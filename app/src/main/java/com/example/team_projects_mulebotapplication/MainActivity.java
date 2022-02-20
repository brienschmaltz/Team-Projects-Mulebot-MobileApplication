package com.example.team_projects_mulebotapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    TextView tv_lat, tv_long;
    Button d_button, f_button, d_gps_perm_button;
    Switch sw_locationupdates;
    String display_null = "Not live tracking";


    boolean requestingLocationUpdates = false;

    //GPS info var creation
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest = LocationRequest.create();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Essentially required code to instantiate app and its code
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Attaching GUI to code
        d_button = findViewById(R.id.d_button);
        d_gps_perm_button = findViewById(R.id.d_gps_perm_button);
        tv_lat = findViewById(R.id.tv_lat);
        tv_long = findViewById(R.id.tv_long);
        sw_locationupdates = findViewById(R.id.sw_locationupdates);

        //Button to get GPS permissions
        d_gps_perm_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                requestLocationPermission();
            }
        });

        //Button to get edit textView
        d_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //Do something to text view
            }
        });

        //Switch button listener
        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationupdates.isChecked()) {
                    requestingLocationUpdates = true;
                    //turn on location tracking
                    startLocationUpdates();
                } else {
                    requestingLocationUpdates = false;
                    stopLocationUpdates();
                }
            }
        });

        //Define the location update callback. Unsure how this works
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                super.onLocationResult(locationResult);
                updateUI(locationResult.getLastLocation());
            }
        };

        // Confirms permissions
        requestLocationPermission();
        //Creates a location request so that we can get this info, sets the parameters
        createLocationRequest();
        //
        updateGPS();
    }
    //-----------------------------------
    //GPS data retrieval functions in order of how they run.
    //-----------------------------------
    private void createLocationRequest() {
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    private void updateGPS() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                updateUI(location);
                            } else {
                                tv_lat.setText("0.0");
                                tv_long.setText("0.0");
                            }
                        }
                    });
        }
    }
    private void updateUI(Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_long.setText(String.valueOf(location.getLongitude()));
    }

    //-----------------------------------
    // Toggle tracking switch functionality.
    //-----------------------------------
    private void stopLocationUpdates() {
        tv_lat.setText(display_null);
        tv_long.setText(display_null);
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();
    }

    //-----------------------------------
    //Code to request GPS permissions.

    // Pulled from https://stackoverflow.com/questions/40142331/how-to-request-location-permission-at-runtime

    //Not sure how it works but it does!

    //Github doc: https://github.com/googlesamples/easypermissions
    //-----------------------------------
    private final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }
}
