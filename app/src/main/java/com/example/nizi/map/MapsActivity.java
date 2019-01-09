package com.example.nizi.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, LocationListener, GoogleMap.OnMapLongClickListener {
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Location mLastLocation;
    private Date dt;
    private Context context = this;
    private DatabaseHelper dbHelper = new DatabaseHelper(this);
    private int _location_ID = 0;
    private LocationManager locationManager;
    private String locationProvider;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
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
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        long consumingTime = System.nanoTime() - starttime;
        Toast.makeText(this, "Use "+consumingTime+" ns", Toast.LENGTH_SHORT).show();

        _location_ID = 0;
        Intent intent = this.getIntent();
        _location_ID = intent.getIntExtra("location_ID",0);

    }

    private void setUpMap() {
        long starttime = System.nanoTime();
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);

        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        if (null != locationAvailability && locationAvailability.isLocationAvailable()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                long consumingTime = System.nanoTime() - starttime;
//                Toast.makeText(this, "Use "+consumingTime+" ns", Toast.LENGTH_SHORT).show();

                //add pin at user's location
                placeMarkerOnMap(currentLocation);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String selectQuery="SELECT "+
                        com.example.nizi.map.Location.KEY_ID+","+
                        com.example.nizi.map.Location.KEY_name+","+
                        com.example.nizi.map.Location.KEY_long_latitude+","+
                        com.example.nizi.map.Location.KEY_time+","+
                        com.example.nizi.map.Location.KEY_location+" FROM "+
                        com.example.nizi.map.Location.TABLE;
                Cursor cursor=db.rawQuery(selectQuery,null);

                while(cursor.moveToNext()){}
                if(cursor.moveToLast()){
                    do{
                        String old = cursor.getString(cursor.getColumnIndex(com.example.nizi.map.Location.KEY_long_latitude));
                        String[] str = old.split("a");
//                        String[] str1 = str[0].split(".");
//                        String[] str2 = str[1].split(".");
                        Double longitude = getNumFromString(str[0]);
                        Double latitude = getNumFromString(str[1]);

                        LatLng start = new LatLng(latitude,longitude);
                        LatLng end = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()*(-1));

                        LayoutInflater inflater = LayoutInflater.from(this);
                        View view = inflater.inflate(R.layout.activity_dialog,null);
                        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
                        alertDialogBuilderUserInput.setView(view);
                        TextView dialog_show_name = (TextView)view.findViewById(R.id.dialog_show_name);
                        TextView dialog_show_time = (TextView)view.findViewById(R.id.dialog_show_time);
                        dialog_show_name.setText("Name: " + cursor.getString(cursor.getColumnIndex(com.example.nizi.map.Location.KEY_name)));
                        dialog_show_time.setText("Last Check In Time: "+ cursor.getString(cursor.getColumnIndex(com.example.nizi.map.Location.KEY_time)));
//                        alertDialogBuilderUserInput.setCancelable(false);
                        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
//                        Toast.makeText(this, old, Toast.LENGTH_SHORT).show();

                        if(getDistance(start, end) <= 30){
                            alertDialogAndroid.show();
                            break;
//                            Toast.makeText(this, ""+getDistance(start,end), Toast.LENGTH_SHORT).show();
                        }else{
                            alertDialogAndroid.dismiss();
                        }
                    }while(cursor.moveToPrevious());
                }
                cursor.close();
                db.close();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        setUpMap();
//    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMarkerClickListener(this);

    }

    protected void placeMarkerOnMap(LatLng location) {
        MarkerOptions markerOptions = new MarkerOptions().position(location);

        String s = "Longtitude: " + location.longitude + ", " + "Latitude: " + location.latitude;
        String titleStr = getAddress(location);  // add these two lines
        markerOptions.title(titleStr);
        mMap.addMarker(markerOptions);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        setUpMap();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        setUpMap();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private String getAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);
        String addressText = "";
        List<Address> addresses = null;
        Address address = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses.get(0);
                String countryCode = address.getCountryCode();
                String subAdminArea = address.getSubAdminArea();
                String adminArea = address.getAdminArea();
                String locality = address.getLocality();
                String featureName = address.getFeatureName();
                addressText = featureName +" "+ subAdminArea +", " + locality + ", " + adminArea + ", " + countryCode;
            }
        } catch (IOException e) {
        }
        return addressText;
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
//        mMap.clear();
        placeMarkerOnMap(latLng);
        String snippet = String.format(Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude);
        String name;
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.activity_input_dialog,null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(view);

        final EditText dialog_name = (EditText)view.findViewById(R.id.dialog_name);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocationRepo repo = new LocationRepo(context);
                        com.example.nizi.map.Location location = new com.example.nizi.map.Location();
                        location.current_name = dialog_name.getText().toString();
                        location.long_latitude = "Longitude: " + latLng.longitude + "\n" + "Latitude: " + latLng.latitude;
                        dt = new Date();
                        location.current_time = dt.toLocaleString();
                        location.current_location = getAddress(latLng);
                        location.location_ID = _location_ID;

                        if(_location_ID == 0){
                            _location_ID = repo.insert(location);
                            Toast.makeText(context, location.current_time, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, "Wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();

        if(mMap != null){
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

    }

    public Double getNumFromString(String s){
        s = s.trim();
        String temp = "";
        if(s != null && !"".equals(s))
            for (int i = 0; i < s.length(); i++)
                if (s.charAt(i)==46||(s.charAt(i) >= 48 && s.charAt(i) <= 57))
                    temp += s.charAt(i);
        Double num = Double.valueOf(temp).doubleValue();
        return num;
    }

    public double getDistance(LatLng start, LatLng end){
        double lat1 = (Math.PI/180)*start.latitude;
        double lat2 = (Math.PI/180)*end.latitude;

        double lon1 = (Math.PI/180)*start.longitude;
        double lon2 = (Math.PI/180)*end.longitude;

        double R = 6371;
        double d =  Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1))*R;

        return d*1000;
    }

}
