package com.example.team_projects_mulebotapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    TextView tv_lat, tv_long;
    Button d_button, d_gps_perm_button,f_button;
    Switch sw_locationupdates;
    String display_null = "Not live tracking";

    boolean requestingLocationUpdates = false;

    //GPS info var creation
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest = LocationRequest.create();


    //Bluetooth var creation
    private final int REQUEST_ENABLE_BT = 4;
    public static String EXTRA_ADDRESS = "device_address";

    //Misc var creation
    public float global_lat, global_long;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Essentially required code to instantiate app and its code
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Attaching GUI to code

        f_button = findViewById(R.id.follow_button);
        d_button = findViewById(R.id.d_button);
        d_gps_perm_button = findViewById(R.id.d_gps_perm_button);

        tv_lat = findViewById(R.id.tv_lat);
        tv_long = findViewById(R.id.tv_long);

        sw_locationupdates = findViewById(R.id.sw_locationupdates);


        // Follow Button
        f_button.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip= "192.168.0.127";
                try {
                    sendData(ip);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        //Button to get GPS permissions
        d_gps_perm_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                requestLocationPermission();
            }
        });

        //Button get bluetooth permissions
        d_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                requestBluetoothPermission();
                requestBluetoothAdminPermission();
            }
        });

        //Switch button to toggle live GPS info to text views
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

        //The fused location provider invokes the LocationCallback.onLocationResult() callback method. (which is this small snippet of code)
        // Which basically gives us a list "location" containing the lat and long.
        //This is where I am not so sure how this works 100%
        // we basically just grab the most current location result and throw it to the UI
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
        //Creates a location request so that we can get this info, sets the parameters
        createLocationRequest();
        //Sends location data to updateUI
        updateGPS();



    }
    void sendData(String request) throws IOException {

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                Socket s = null;
                String final_message = "Lat: " + global_lat + " Long: " + global_long;
                try {
                    //Wright State use this: 10.16.127.6
                    //Home use this: 192.168.0.127"
                    System.out.print(request);
                    s = new Socket(request, 8080);
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                    while (requestingLocationUpdates == true) {
                        dos.writeUTF(final_message);
                        SystemClock.sleep(3000);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }


    //-----------------------------------
    //GPS data retrieval functions in order of how they run.
    //-----------------------------------
    private void createLocationRequest() {
        //Was 100000
        // Fastest  5000
        locationRequest.setInterval(6000);
        locationRequest.setFastestInterval(1000);
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
                                global_lat = (float) location.getLatitude();
                                global_long = (float) location.getLongitude();
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
    //Code to request GPS and Bluetooth permissions.

    // Pulled from https://stackoverflow.com/questions/40142331/how-to-request-location-permission-at-runtime

    //Not sure how it works but it does at least for the GPS permissions

    //Github doc: https://github.com/googlesamples/easypermissions
    //-----------------------------------
    private final int REQUEST_LOCATION_PERMISSION = 1;

    private final int REQUEST_BLUETOOTH_PERMISSION = 2;

    private final int REQUEST_BLUETOOTH_ADMIN_PERMISSION = 3;

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
            Toast.makeText(this, "Location permission already granted", Toast.LENGTH_SHORT).show();
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }
    @AfterPermissionGranted(REQUEST_BLUETOOTH_ADMIN_PERMISSION )
    public void requestBluetoothAdminPermission() {
        String[] perms = {Manifest.permission.BLUETOOTH_ADMIN};
        if(EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Admin Bluetooth permission already granted", Toast.LENGTH_SHORT).show();
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the admin bluetooth permission", REQUEST_BLUETOOTH_ADMIN_PERMISSION , perms);
        }
    }
    @AfterPermissionGranted(REQUEST_BLUETOOTH_PERMISSION)
    public void requestBluetoothPermission() {
        String[] perms = {Manifest.permission.BLUETOOTH};
        if(EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Bluetooth permission already granted", Toast.LENGTH_SHORT).show();
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the bluetooth permission", REQUEST_BLUETOOTH_PERMISSION, perms);
        }
    }
}

