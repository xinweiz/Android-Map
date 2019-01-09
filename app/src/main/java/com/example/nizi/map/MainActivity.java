package com.example.nizi.map;

import android.Manifest;
import android.app.Activity;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {
    private TextView long_latitude;
    private TextView main_location;
    private Button btn_check_in;
    private Button btn_map;
    private ListView listview;
    private Context context = this;
    private DatabaseHelper dbHelper;
    //    private Location mLastLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private String locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this, "Location", null, 3);
//        dbHelper.getWritableDatabase();

        long_latitude = (TextView) findViewById(R.id.long_latitude);
        main_location = (TextView) findViewById(R.id.location);
        btn_check_in = (Button) findViewById(R.id.btn_check_in);
        btn_map = (Button) findViewById(R.id.btn_map);
        listview = (ListView) findViewById(R.id.listview);

        long starttime = System.nanoTime();
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
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        try{
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null)
                showLocation(location);

        }catch (Exception e){
            e.printStackTrace();
        }

        long consumingTime = System.nanoTime() - starttime;
        Toast.makeText(this, "Use " + consumingTime + " ns", Toast.LENGTH_SHORT).show();

        locationManager.requestLocationUpdates(locationProvider, 180000, 100, locationListener);

        showList();

        btn_check_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CheckInActivity.class);
                intent.putExtra("long_latitude", long_latitude.getText().toString().trim());
                intent.putExtra("main_location", main_location.getText().toString().trim());
                startActivity(intent);
                Intent i = new Intent(context,MyIntentService.class);
                i.putExtra("long_latitude", long_latitude.getText().toString().trim());
                i.putExtra("main_location", main_location.getText().toString().trim());
                startService(i);
                Toast.makeText(context, "Auto Check In Started", Toast.LENGTH_SHORT).show();
            }
        });

        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showList() {
        LocationRepo repo = new LocationRepo(this);
        List<com.example.nizi.map.Location> myList = repo.getList();
        if (myList != null) {
            MyAdapter myAdapter = new MyAdapter(this, myList);
            listview.setAdapter(myAdapter);
            myAdapter.notifyDataSetChanged();
            Toast.makeText(this, "SHOW", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        showList();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        locationManager.requestLocationUpdates(locationProvider, 180, 100, locationListener);
    }


    private void showLocation(Location location){
        String s = "Longitude: " + location.getLongitude() + "\n" + "Latitude: " + location.getLatitude();
        long_latitude.setText(s);
        String titleStr = getAddress(location);
        main_location.setText(titleStr);
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
            showLocation(location);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationManager!=null){
            locationManager.removeUpdates(locationListener);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private String getAddress(Location location) {
        Geocoder geocoder = new Geocoder( this );
        String addressText = "";
        List<Address> addresses = null;
        Address address = null;
        try {
            addresses = geocoder.getFromLocation( location.getLatitude(), location.getLongitude(), 1 );
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
