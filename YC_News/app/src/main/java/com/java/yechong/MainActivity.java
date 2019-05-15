package com.java.yechong;

import android.app.Activity;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, Serializable{

    private MainActivity myMainActivity;
    private String myPath;
    private Button checkNetWorkBtn;
    private NetConnectBroadcastReceiver myNetConnectBroadcastReceiver;
    private boolean first;
    private ArrayList<NewsItem> data;
    private ArrayList<NewsItemAdapter> newsItemAdapters;
    private DataBaseOperator dbOperator;
    private ArrayList<Label> allChannels;
    private ArrayList<Label> removedChannels;
    private ArrayList<TabFragment> fragments;
    private ArrayList<TabFragment> removed_fragments;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MyFragmentPagerAdapter fragmentAdapter;
    private boolean updating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myMainActivity = this;
        dbOperator = new DataBaseOperator(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
//        checkNetWorkBtn = (Button)findViewById(R.id.btn2);
//        myListView = (ListView)findViewById(R.id.myListView);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        viewPager = (ViewPager)findViewById(R.id.viewPager);

        //设置toolBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.inflateMenu(R.menu.menu);

        //查看、监听网络情况
        first = true;
        isConnected();
        registerBroadcastReceiver();

//        //初始化数据库操作者（用于操作标签）
        dbOperator.initDataBase();
//        //获取数据库数据
//        data = dbOperator.queryAllNews();
//        if(data == null){
//            if(isConnected()) {
//                RssReader reader = new RssReader();
//                reader.execute();
//            }
//        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_view_drawer_open, R.string.navigation_view_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //初始化适配器
        newsItemAdapters = new ArrayList<>();

        initLabels();
        initUI();
        initFragment();
        fragmentAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchMenu = menu.findItem(R.id.search_menu);
        searchMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent();
                intent.setClass(myMainActivity, SearchActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.labels:
                Intent intent = new Intent();
                intent.setClass(myMainActivity, EditLabelsActivity.class);
                intent.putExtra("data", allChannels);
                intent.putExtra("data_removed", removedChannels);
                startActivityForResult(intent, 0);
                break;
            case R.id.favorite:
                Intent intent1 = new Intent();
                intent1.setClass(myMainActivity, FavoriteActivity.class);
                startActivity(intent1);
                break;
            case R.id.information:
                Intent intent2 = new Intent();
                intent2.setClass(myMainActivity, AboutActivity.class);
                startActivity(intent2);
                break;
            case R.id.commend:
                if(isUpdating())
                    return false;
                Intent intent3 = new Intent();
                intent3.setClass(myMainActivity, RecommendActivity.class);
                startActivity(intent3);
                break;
        }
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

//    @Override
//    public void onRefresh() {
//        if(isConnected()) {
//            RssReader reader = new RssReader();
//            reader.execute();
////            fragments.get(viewPager.getCurrentItem()).stopRefresh();
//        }
//        else{
//            swipeRefreshLayout.setRefreshing(false);
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == RESULT_OK){
            ArrayList<Label> removedLabels = (ArrayList<Label>)data.getSerializableExtra("data_removed");
            ArrayList<Label> labels = (ArrayList<Label>)data.getSerializableExtra("data");
            String a = "";
            for(Label label : removedLabels)
                a += label.getTitle() + " ";
//            Toast.makeText(this, a, Toast.LENGTH_LONG).show();
            for(Label label : removedLabels){
                fragmentAdapter.deleteItem(label.getTitle());
            }
            for(Label label : labels){
                fragmentAdapter.insertItem(label);
            }

            removedChannels = removedLabels;
            fragmentAdapter.notifyDataSetChanged();
//            fragmentStateAdapter.deleteItem(0);
        }
        else if(requestCode == 666){
            String title = data.getStringExtra("label");
            int position = data.getIntExtra("pos", -1);
            int index = 0;
            for(Label i : allChannels){
                if(i.getTitle().equals(title)){
                    index = allChannels.indexOf(i);
                    break;
                }
            }
            fragments.get(index).setVisited(position);
        }
    }

    private void initUI(){
//        Button mybtn = (Button)findViewById(R.id.btn);
//        mybtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RssReader reader = new RssReader();
//                reader.execute();
//            }
//        });

        //滑动更新
//        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
//        swipeRefreshLayout.setColorSchemeResources(R.color.refresh_start, R.color.refresh_end);
//        swipeRefreshLayout.setOnRefreshListener(this);

//        Button showBtn = (Button)findViewById(R.id.btn2);
//        showBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String msg = "";
//                for(int i = 0; i < allChannels.size(); i++){
//                    msg += allChannels.get(i).getTitle() + " ";
//                }
//                Toast.makeText(myMainActivity, msg, Toast.LENGTH_LONG).show();
//            }
//        });
    }

    //初始化标签
    private void initLabels(){
        ArrayList<Label> labels = dbOperator.queryAllLabels();
        allChannels = dbOperator.queryAllLabels(0);
        removedChannels = dbOperator.queryAllLabels(1);
        if(labels.size() == 0){
            LabelsFetcher fetcher = new LabelsFetcher();
            fetcher.execute();
        }
        else {
            initFragment();
        }
//        allChannels = dbOperator.queryAllLabels();
//        removedChannels = new ArrayList<>();
//        if(allChannels.size() == 0){
//            LabelsFetcher fetcher = new LabelsFetcher();
//            fetcher.execute();
//        }
//        else{
//            initFragment();
//        }
    }

    private void initFragment(){

        for(Label i : allChannels){
            //设置适配器
            NewsItemAdapter _adapter = new NewsItemAdapter(myMainActivity, data);
            newsItemAdapters.add(_adapter);
        }
        fragments = new ArrayList<>();
        for(Label i : allChannels){
            fragments.add(TabFragment.getInstance(i, myMainActivity));
        }
        removed_fragments = new ArrayList<>();
        for(Label i : removedChannels){
            removed_fragments.add(TabFragment.getInstance(i, myMainActivity));
        }
//        for(Label i : allChannels){
//            fragments.add(TabFragment.getInstance(i.getTitle(), newsItemAdapters.get(k)));
//            k++;
//        }
        fragmentAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentAdapter);
//        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabTextColors(ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.blue));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.design_default_color_primary));
        ViewCompat.setElevation(tabLayout, 10);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                viewPager.setCurrentItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
//        Log.e("tips", String.valueOf(newsItemAdapters.size()));
    }

    //初始判断是否连接
    public boolean isConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();
        if (networkinfo != null && networkinfo.isAvailable()) {
            String intentName = networkinfo.getTypeName();
            Toast.makeText(this,"已经连接"+intentName, Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(this, "连接失败", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    //注册广播
    private void registerBroadcastReceiver(){
        myNetConnectBroadcastReceiver = new NetConnectBroadcastReceiver();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(myNetConnectBroadcastReceiver, filter);
    }

    //监听网络情况
    public class NetConnectBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();
            if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
                if(first){
                    first = false;
                    return;
                }
                if (networkinfo != null && networkinfo.isAvailable()) {
                    String intentName = networkinfo.getTypeName();
                    Toast.makeText(myMainActivity,"连接发生变化，已经连接"+intentName, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(myMainActivity,"已经断开连接", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //获取全部标签
    public class LabelsFetcher extends AsyncTask<String, Void, ArrayList<Label>>{
        @Override
        protected ArrayList<Label> doInBackground(String... strings) {
            RssHelper helper = new RssHelper();
            ArrayList<Label> labels = helper.getAllChannels();
            return labels;
        }

        @Override
        protected void onPostExecute(ArrayList<Label> labels) {
            int size = labels.size();
            for(int i = 0; i < size; i++){
                dbOperator.addData(labels.get(i), 0);
                allChannels.add(labels.get(i));
            }
            initFragment();
        }
    }

    //获取新闻


    class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
//            return temp.length;
            return allChannels.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            return temp[position];
            return allChannels.get(position).getTitle();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            TabFragment fragment = (TabFragment)super.instantiateItem(container, position);

            Log.e("tips", "instantiateItem"+position);
            return fragment;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        }

        public void deleteItem(String title){
            int position = -1;
            for(int i = 0; i < allChannels.size(); i++){
                if(allChannels.get(i).getTitle().equals(title)){
                    position = i;
                    break;
                }
            }
            if(position != -1){
                Label label = allChannels.remove(position);
                TabFragment fragment = fragments.remove(position);
                removed_fragments.add(fragment);
                notifyDataSetChanged();
                dbOperator.deleteLabel(label.getTitle());
                dbOperator.addData(label, 1);
            }
        }

        public void insertItem(Label label){
            int position = -1;
            for(int i = 0; i < allChannels.size(); i++){
                if(allChannels.get(i).getTitle().equals(label.getTitle())){
                    position = i;
                    break;
                }
            }
            if(position == -1) {
                allChannels.add(label);
                for(TabFragment fragment : removed_fragments){
                    if(fragment.getLabel().equals(label.getTitle())){
                        fragments.add(fragment);
                        removed_fragments.remove(fragment);
                    }
                }
                notifyDataSetChanged();
                dbOperator.deleteLabel(label.getTitle());
                dbOperator.addData(label, 0);
            }
        }
    }

    public boolean isUpdating(){
        updating = false;
        for(TabFragment fragment : fragments){
            if(fragment == null)
                continue;
            if(fragment.isUpdating())
                updating = true;
        }
        if(updating)
            Toast.makeText(myMainActivity, "正在为您加载图片，请稍后点击", Toast.LENGTH_SHORT).show();
        return updating;
    }
}

//class MyWebViewClient extends WebViewClient{
//    private String myPath;
//    private Activity myMainActivity;
//    MyWebViewClient(String myPath, Activity myMainActivity){
//        this.myPath = myPath;
//        this.myMainActivity = myMainActivity;
//    }
//    @Override
//    public void onPageStarted(WebView view, String url, Bitmap favicon) {
//        super.onPageStarted(view, url, favicon);
//    }
//
//    @Override
//    public void onPageFinished(WebView view, String url) {
//        super.onPageFinished(view, url);
//        view.saveWebArchive(myPath+File.separator+"1.xml", false,
//                new ValueCallback<String>() {
//                    @Override
//                    public void onReceiveValue(String value) {
//                        if(value == null){
//                            Toast.makeText(myMainActivity,"保存失败", Toast.LENGTH_LONG).show();
//                        }
//                        else{
//                            Toast.makeText(myMainActivity,"保存为"+myPath+ File.separator+"1.xml", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
//    }
//}