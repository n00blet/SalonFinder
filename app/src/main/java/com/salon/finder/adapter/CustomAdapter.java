package com.salon.finder.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.salon.finder.model.SalonObjects;
import com.salon.finder.R;

import java.util.ArrayList;

/**
 * Created by Rakshith on 07-06-2015.
 */
public class CustomAdapter extends BaseAdapter {

    private Activity mActivity;
    private LayoutInflater mInflater = null;
    ArrayList<SalonObjects> mSalons;

    public CustomAdapter(Activity activity,ArrayList<SalonObjects> salons){
        this.mActivity = activity;
        this.mSalons = salons;
        mInflater = LayoutInflater.from(activity);
    }


    @Override
    public int getCount() {
        return mSalons.size();
    }

    @Override
    public Object getItem(int position) {
        return mSalons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null){
            convertView = mInflater.inflate(R.layout.salon_list_item,parent,false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.salon_name);
            holder.location = (TextView) convertView.findViewById(R.id.salon_location);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        holder.name.setText(mSalons.get(position).getName());
        holder.location.setText(mSalons.get(position).getLocation());

        return convertView;
    }

    static class ViewHolder{
        TextView name;
        TextView location;
    }
}
