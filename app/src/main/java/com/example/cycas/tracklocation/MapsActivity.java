package com.example.cycas.tracklocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private TextView latitude_label;
    private TextView longitude_label;
    private TextView latitude_value;
    private TextView longitude_value;
    private TextView updateTime_label;
    private TextView updateTime_value;
    private Switch locationSwitch;
    private ListView listView;

    protected GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private LatLng mLatLng;
    private String mLastUpdateTime;
    private Polyline line;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    private ArrayList<String> latLngArrList = new ArrayList<String>();
    private ArrayList<LatLng> latLngPolyArrList = new ArrayList<LatLng>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        googleApiClient();
        locationSettings();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        latitude_label = (TextView) findViewById(R.id.latitude_label);
        longitude_label = (TextView) findViewById(R.id.longitude_label);
        latitude_value = (TextView) findViewById(R.id.latitude);
        longitude_value = (TextView) findViewById(R.id.longitude);
        updateTime_label = (TextView) findViewById(R.id.updateTime_label);
        updateTime_value = (TextView) findViewById(R.id.updateTime);
        locationSwitch = (Switch) findViewById(R.id.locationSwitch);
        listView = (ListView) findViewById(R.id.listView);

        locationSwitch.setChecked(false);
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true) {
                    startLocationUpdates();

                } else {

                }
            }
        });


    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && locationSwitch.isChecked() == true) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getApplicationContext(), "You need to grant location service permission for current loc", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        handleLocationUpdate();
        saveLatLng();
        drawOnMap();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getApplicationContext(), "1 You need to grant location service permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLocation != null) {
            handleLocationUpdate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],int[] grantResults){
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Bundle bundle = null;
                    onConnected(bundle);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    latitude_value.setText("Location service unavailable");
                    longitude_value.setText("Location service unavailable");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void handleLocationUpdate() {
        mLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        latitude_value.setText(String.valueOf(mLatLng.latitude));
        longitude_value.setText(String.valueOf(mLatLng.longitude));
        updateTime_value.setText(mLastUpdateTime);

    }

    public void saveLatLng() {
        // add LatLng to arraylist
        mLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        latLngPolyArrList.add(mLatLng);

        // add LatLng String to arraylist
        String latLng = mLastUpdateTime + " Lat: " + String.valueOf(mLatLng.latitude) + " Lng: " + String.valueOf(mLatLng.longitude);
        latLngArrList.add(latLng);

        //add LatLng to polyline
       // if(latLngPolyArrList.size() >1)
            line = mMap.addPolyline(new PolylineOptions().addAll(latLngPolyArrList).width(10).color(Color.BLUE));


        // add to listview
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.activity_list_item, android.R.id.text1, latLngArrList);

        listView.setAdapter(adapter);
        listView.deferNotifyDataSetChanged();
    }

    public void drawOnMap() {
        // Add a marker in Sydney and move the camera
        //mMap.addMarker(new MarkerOptions().position(mLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name)).title("I'm here"));
        mMap.addMarker(new MarkerOptions().position(mLatLng).title("I'm here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }


    protected void locationSettings() {

        // create location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // location settings builder
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        // check location setting and notify user to change location setting
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        builder.setAlwaysShow(true);

//        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//            @Override
//            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
//
//
//                @Override
//                public void onResult(LocationSettingsResult result){
//                    final Status status = result.getStatus();
//                    final LocationSettingsStates=result.getLocationSettingsStates();
//                    switch (status.getStatusCode()) {
//                        case LocationSettingsStatusCodes.SUCCESS:
//                            // All location settings are satisfied. The client can
//                            // initialize location requests here.
//
//                            break;
//                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                            // Location settings are not satisfied, but this can be fixed
//                            // by showing the user a dialog.
//                            try {
//                                // Show the dialog by calling startResolutionForResult(),
//                                // and check the result in onActivityResult().
////                                status.startResolutionForResult(
////                                        OuterClass.this,
////                                        REQUEST_CHECK_SETTINGS);
//                                status.startResolutionForResult(
//                                        getCallingActivity(),
//                                        1000);
//
//                            } catch (IntentSender.SendIntentException e) {
//                                // Ignore the error.
//                            }
//                            break;
//                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                            // Location settings are not satisfied. However, we have no way
//                            // to fix the settings so we won't show the dialog.
//
//                            break;
//                    }
//
//                }
//
//
//            }
//        });
    }



    protected void googleApiClient() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


}
