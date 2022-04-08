package com.example.team_projects_mulebotapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

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
    ListView mDeviceList;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Set <BluetoothDevice> mPairedDevices;

    public static String EXTRA_ADDRESS = "device_address";

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
        mDeviceList = (ListView)findViewById(R.id.bluetooth_list_view);

        //---------------------------------------
        //New Bluetooth Code for ListView
        //---------------------------------------

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                //listPairedDevices();
            }
            else {
                //Ask to the user turn the bluetooth on
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
            }
        }
        // Follow Button
        f_button.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String request = "http://10.16.127.6:8080/";
                try {
                    sendRequest(request);
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
                //startBluetooth();
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

        // Confirms permissions
        //requestLocationPermission();
        //Creates a location request so that we can get this info, sets the parameters
        createLocationRequest();
        //Sends location data to updateUI
        updateGPS();



    }
    void sendRequest(String request) throws IOException {

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                String test = "123";
                //request = "http://localhost:8080";
                URL url = null;
                try {
                    url = new URL(request);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "text/plain");
                connection.setRequestProperty("charset", "utf-8");
                connection.connect();
                connection.getOutputStream().write(test.getBytes("UTF-8"));
                connection.getOutputStream().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();


        //Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

        //for (int c; (c = in.read()) >= 0;)
            //System.out.print((char)c);
    }

    //Bluetooth code
/*
    private void startBluetooth() {

        //Check for support

        // Use this check to determine whether Bluetooth classic is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        //The BluetoothAdapter is required for any and all Bluetooth activity.
        // The BluetoothAdapter represents the device's own Bluetooth adapter (the Bluetooth radio).
        // There's one Bluetooth adapter for the entire system, and your app can interact with it using this object.
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Simple checks
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (!bluetoothAdapter.isEnabled()) {
            //Bluetooth is enabled!
            //
            // The following code snippet sets the device to be discoverable for two minutes:
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            //Next


            //Before performing device discovery, it's worth querying the set of paired devices to see if the desired device is already known.
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                }
            }
        }
    }

    // Register for broadcasts when a device is discovered.
    //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    //registerReceiver(receiver, filter);

    // Unsure what this code is doing
    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);

    }
 */
    //New Bluetooth code for ListView

    /*
    private void listPairedDevices() {
        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice bt : mPairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        mDeviceList.setAdapter(adapter);
        mDeviceList.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
    }
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            // Make an intent to start next activity.
            //Intent i = new Intent(DeviceListActivity.this, MyCommunicationsActivity.class);
            //Change the activity.
            //i.putExtra(EXTRA_ADDRESS, address); //this will be received at CommunicationsActivity
            //startActivity(i);
        }
    };

  */
    //Next



    //Next




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

