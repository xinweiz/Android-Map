package com.example.nizi.map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.*;

public class CheckInActivity extends Activity implements View.OnClickListener {
    private EditText check_in_name;
    private Button check_in_submit_btn;
    private TextView check_in_location;
    private TextView check_in_long_latitude;
    private Date dt;
    private DatabaseHelper dbHelper = new DatabaseHelper(this);
    private int _location_ID = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);

        String getLong_latitude = getIntent().getStringExtra("long_latitude");
        String getLocation = getIntent().getStringExtra("main_location");

        check_in_name = (EditText)findViewById(R.id.check_in_name);
        check_in_long_latitude = (TextView)findViewById(R.id.check_in_long_latitude);
        check_in_location = (TextView)findViewById(R.id.check_in_location);
        check_in_submit_btn = (Button)findViewById(R.id.check_in_submit_btn);

        _location_ID = 0;
        Intent intent = this.getIntent();
        _location_ID = intent.getIntExtra("location_ID",0);

        check_in_long_latitude.setText(getLong_latitude);
        check_in_location.setText(getLocation);
        check_in_submit_btn.setOnClickListener(this);

        String current = check_in_long_latitude.getText().toString();
        String[] s = current.split("a");
        Double current_longitude = getNumFromString(s[0]);
        Double current_latitude = getNumFromString(s[1]);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                com.example.nizi.map.Location.KEY_ID+","+
                com.example.nizi.map.Location.KEY_name+","+
                com.example.nizi.map.Location.KEY_long_latitude+","+
                com.example.nizi.map.Location.KEY_time+","+
                com.example.nizi.map.Location.KEY_location+" FROM "+
                com.example.nizi.map.Location.TABLE;
        Cursor cursor=db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{
                String old = cursor.getString(cursor.getColumnIndex(Location.KEY_long_latitude));
                String[] str = old.split("a");
                Double longitude = getNumFromString(str[0]);
                Double latitude = getNumFromString(str[1]);

                LatLng start = new LatLng(latitude,longitude);
                LatLng end = new LatLng(current_latitude,current_longitude);

                if(getDistance(start, end)<=30){
//                    location.current_name = cursor.getString(cursor.getColumnIndex(Location.KEY_name));
                    check_in_name.setText(cursor.getString(cursor.getColumnIndex(Location.KEY_name)));
                    check_in_location.setText(cursor.getString(cursor.getColumnIndex(Location.KEY_location)));
                    Toast.makeText(this, "Distance less than 30.", Toast.LENGTH_SHORT).show();
                }
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();

    }

    @Override
    public void onClick(View v) {
        LocationRepo repo = new LocationRepo(this);
        Location location = new Location();
        location.current_name = check_in_name.getText().toString();
        location.long_latitude = check_in_long_latitude.getText().toString();
        dt = new Date();
        location.current_time = dt.toLocaleString();
        location.current_location = check_in_location.getText().toString();
        location.location_ID = _location_ID;

        if(_location_ID == 0){
            _location_ID = repo.insert(location);
            Toast.makeText(this, "Inserted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Wrong", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(CheckInActivity.this,MainActivity.class);
        startActivity(intent);

    }

    public double getNumFromString(String s){
        s = s.trim();
        String temp = "";
        if(s != null && !"".equals(s))
            for (int i = 0; i < s.length(); i++)
                if (s.charAt(i) >= 48 && s.charAt(i) <= 57)
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
