package com.example.nizi.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Location> mlist;

    public MyAdapter(Context context, List<Location> list){
        mInflater = LayoutInflater.from(context);
        mlist = list;
    }

    @Override
    public int getCount() {
        return mlist.size();
//        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.activity_list_item, parent, false);
            holder = new ViewHolder();

            holder.nameTv = (TextView) convertView.findViewById(R.id.item_name);
            holder.long_latitudeTv = (TextView) convertView.findViewById(R.id.item_long_latitude);
            holder.timeTv = (TextView) convertView.findViewById(R.id.item_time);
            holder.locationTv = (TextView) convertView.findViewById(R.id.item_location);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        Location location = mlist.get(position);
        holder.nameTv.setText(location.current_name);
        holder.long_latitudeTv.setText(location.long_latitude);
        holder.timeTv.setText(location.current_time);
        holder.locationTv.setText(location.current_location);

        return convertView;

    }

    private class ViewHolder{
        TextView nameTv;
        TextView long_latitudeTv;
        TextView timeTv;
        TextView locationTv;
    }

}
