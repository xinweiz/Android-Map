package com.example.nizi.map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.example.nizi.map.Location;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_TABLE_LOCATION = "create table " +
            Location.TABLE + "("+
            Location.KEY_ID + " integer primary key AUTOINCREMENT," +
            Location.KEY_name + " text," +
            Location.KEY_long_latitude + " real," +
            Location.KEY_time + " text," +
            Location.KEY_location + " text)";
    private Context mContext;

    public DatabaseHelper(Context context){
        super(context, Location.TABLE, null, 3);
        mContext = context;
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOCATION);
        Toast.makeText(mContext,"CREATED",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion>oldVersion){

        }
    }

}
