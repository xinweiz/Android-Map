package com.example.nizi.map;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class MyIntentService extends IntentService {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private String locationProvider;
    private DatabaseHelper dbHelper;
    private Context context;
    private Date dt;
    private int _location_ID = 0;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MyIntentService(String name) {
        super(name);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try{
            dbHelper = new DatabaseHelper(this, "Location", null, 3);

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);
            if (providers.contains(LocationManager.GPS_PROVIDER)) {
                locationProvider = LocationManager.GPS_PROVIDER;
            } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                locationProvider = LocationManager.NETWORK_PROVIDER;
            } else {
                Toast.makeText(this, "No method to obtain location", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            Location location = locationManager.getLastKnownLocation(locationProvider);
            locationManager.requestLocationUpdates(locationProvider, 180000, 100, locationListener);

            Thread.sleep(5000);
            System.out.println("");
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }
    LocationListener locationListener =  new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            LocationRepo repo = new LocationRepo(context);
            com.example.nizi.map.Location_Auto location_auto = new com.example.nizi.map.Location_Auto();
            location_auto.current_name = "";
            location_auto.long_latitude = "Longitude: " + location.getLongitude() + "\n" + "Latitude: " + location.getLatitude();
            dt = new Date();
            location_auto.current_time = dt.toLocaleString();
            location_auto.current_location = getAddress(location_auto);
            location_auto.location_ID = _location_ID;

            if(_location_ID == 0){
                _location_ID = repo.insert_auto(location_auto);
                Toast.makeText(context, "Inserted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "Wrong", Toast.LENGTH_SHORT).show();
            }

        }
    };

    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }
    @Override
    public void onDestroy() {
        System.out.println("Service onDestroy");
        super.onDestroy();
    }

    private String getAddress(Location_Auto location) {
        Geocoder geocoder = new Geocoder( this );
        String addressText = "";
        List<Address> addresses = null;
        Address address = null;
        try {
            addresses = geocoder.getFromLocation( location.location_ID, location.location_ID, 1 );
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses.get(0);
                String countryCode = address.getCountryCode();
                String adminArea = address.getAdminArea();
                String locality = address.getLocality();
                String featureName = address.getFeatureName();
                addressText = featureName + ", " + locality + ", " + adminArea + ", " + countryCode;
            }
        } catch (IOException e ) {
        }
        return addressText;
    }

}
