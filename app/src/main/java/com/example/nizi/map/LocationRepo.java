package com.example.nizi.map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class LocationRepo {
    private DatabaseHelper dbHelper;
    private List<Location> locationList;

    public LocationRepo(Context context){
        dbHelper = new DatabaseHelper(context);
    }

    public int insert(Location location){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(Location.KEY_name,location.current_name);
        values.put(Location.KEY_long_latitude,location.long_latitude);
        values.put(Location.KEY_time,location.current_time);
        values.put(Location.KEY_location,location.current_location);

        long location_Id = db.insert(Location.TABLE,null,values);
        db.close();
        return (int)location_Id;
    }
    public int insert_auto(Location_Auto location){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(Location_Auto.KEY_name,location.current_name);
        values.put(Location_Auto.KEY_long_latitude,location.long_latitude);
        values.put(Location_Auto.KEY_time,location.current_time);
        values.put(Location_Auto.KEY_location,location.current_location);

        long location_Id = db.insert(Location_Auto.TABLE,null,values);
        db.close();
        return (int)location_Id;
    }

    public List<Location> getList(){
        locationList = new ArrayList<Location>();
        SQLiteDatabase db=dbHelper.getReadableDatabase();
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
                Location location = new Location(
                        cursor.getString(cursor.getColumnIndex(Location.KEY_name)),
                        cursor.getString(cursor.getColumnIndex(Location.KEY_long_latitude)),
                        cursor.getString(cursor.getColumnIndex(Location.KEY_time)),
                        cursor.getString(cursor.getColumnIndex(Location.KEY_location)));
                locationList.add(location);

            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return locationList;
    }

}
