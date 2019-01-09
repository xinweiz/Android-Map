package com.example.nizi.map;

public class Location {
    public static final String TABLE = "Location";

    public static final String KEY_ID = "id";
    public static final String KEY_name = "name";
    public static final String KEY_long_latitude = "long_latitude";
    public static final String KEY_time = "time";
    public static final String KEY_location = "location";

    public int location_ID;
    public String current_name;
    public String long_latitude;
    public String current_time;
    public String current_location;

    public Location(){

    }

    public Location(String current_name, String long_latitude,String current_time, String current_location){
        this.current_name = current_name;
        this.long_latitude = long_latitude;
        this.current_time = current_time;
        this.current_location = current_location;
    }
}
