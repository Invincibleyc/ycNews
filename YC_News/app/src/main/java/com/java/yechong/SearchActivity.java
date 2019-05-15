package com.java.yechong;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        AdapterView.OnItemClickListener{
    private DataBaseOperator dbOperator;
    private NewsItemAdapter adapter;
    private ArrayList<NewsItem> data;
    private ArrayList<NewsItem> searched_data;
    private ListView lv;
    private SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar)findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.inflateMenu(R.menu.menu_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        data = new ArrayList<>();
        searched_data = new ArrayList<>();
        dbOperator = new DataBaseOperator(this);
        dbOperator.initDataBase();
        data = dbOperator.queryAllNews();
        adapter = new NewsItemAdapter(this, searched_data);
        lv = (ListView)findViewById(R.id.search_listView);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchMenu = menu.findItem(R.id.search_menu);
        searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
        searchView.setIconified(false);//设置searchView处于展开状态
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextChange(String s) {
        int size = data.size();
        int searched_size = searched_data.size();
        for(int i = 0; i < searched_size; i++)
            searched_data.remove(0);
        for(NewsItem item : data){
            if(item.getTitle().contains(s) || item.getDescription().contains(s)){
                searched_data.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        int size = data.size();
        int searched_size = searched_data.size();
        for(int i = 0; i < searched_size; i++)
            searched_data.remove(0);
        for(NewsItem item : data){
            if(item.getTitle().contains(s) || item.getDescription().contains(s)){
                searched_data.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        searchView.clearFocus();
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this, NewsDetailsActivity.class);
        intent.putExtra("url", searched_data.get(position).getLink());
        intent.putExtra("pos", position);
        intent.putExtra("label", "");
        intent.putExtra("title", searched_data.get(position).getTitle());
        intent.putExtra("description", searched_data.get(position).getDescription());
        this.startActivityForResult(intent, 666);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        int position = data.getIntExtra("pos", -1);
        searched_data.get(position).setVisited(true);
        adapter.notifyDataSetChanged();
        super.onActivityResult(requestCode, resultCode, data);
    }
}
