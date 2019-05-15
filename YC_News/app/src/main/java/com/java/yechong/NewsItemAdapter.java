package com.java.yechong;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class NewsItemAdapter extends BaseAdapter implements Serializable{

    private ArrayList<NewsItem> myData;
//    private Context context;

    public  NewsItemAdapter(Context context, ArrayList<NewsItem> myData){
//        this.context = context;
        this.myData = myData;
    }

    @Override
    public int getCount() {
        return myData.size();
    }

    @Override
    public Object getItem(int position) {
        return myData.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, null);
        ImageView newsPicture = (ImageView)convertView.findViewById(R.id.newsPicture);
        TextView titleView = (TextView)convertView.findViewById(R.id.titleView);
        TextView dateView = (TextView)convertView.findViewById(R.id.dateView);
        if(myData.get(position).getBitmap() == null)
            newsPicture.setImageBitmap(BitmapFactory.decodeResource(convertView.getContext().getResources(), R.mipmap.ic_launcher));
        else
            newsPicture.setImageBitmap(myData.get(position).getBitmap());
//        newsPicture.setImageResource((Integer) myData.get(position).getBitmap());
        titleView.setText((String) myData.get(position).getTitle());
        dateView.setText((String)myData.get(position).getDate());
        NewsItem item = (NewsItem)getItem(position);
        boolean isVisited = item.isVisited();
        if(isVisited){
            convertView.setBackgroundColor(parent.getContext().getResources().getColor(R.color.visited));
        }
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
