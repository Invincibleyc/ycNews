package com.java.yechong;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private DataBaseOperator dbOperator;
    private NewsItemAdapter adapter;
    private ArrayList<NewsItem> favorite_data;
    private ListView lv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        Context context = this;
        Toolbar toolbar = (Toolbar)findViewById(R.id.favorite_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        dbOperator = new DataBaseOperator(this);
        dbOperator.initDataBase();
        favorite_data = dbOperator.queryFavoriteNews();
        adapter = new NewsItemAdapter(this, favorite_data);
        lv = (ListView)findViewById(R.id.favorite_listView);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this, NewsDetailsActivity.class);
        intent.putExtra("url", favorite_data.get(position).getLink());
        intent.putExtra("pos", position);
        intent.putExtra("label", "");
        intent.putExtra("title", favorite_data.get(position).getTitle());
        intent.putExtra("description", favorite_data.get(position).getDescription());
        this.startActivityForResult(intent, 666);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        favorite_data = dbOperator.queryFavoriteNews();
        adapter.notifyDataSetChanged();
        super.onActivityResult(requestCode, resultCode, data);
    }
}
