package com.java.yechong;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ImageReader;
import android.net.ConnectivityManager;
import android.net.sip.SipSession;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;

import java.util.ArrayList;

public class TabFragment extends Fragment implements BaseRefreshListener,
        AdapterView.OnItemClickListener{
    private ViewGroup container;
    private NewsItemAdapter adapter;
    private static MainActivity myActivity;
    private PullToRefreshLayout pullToRefreshLayout;
    private ArrayList<NewsItem> data;
    private DataBaseOperator dbOperator;
    private ListView lv;
    private Label label;
    private int update_l;
    private int update_r;
    private boolean updating = false;

    public static TabFragment getInstance(Label label, MainActivity _myActivity){
        TabFragment tabFragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("label", label);
//        bundle.putSerializable("activity", _myActivity);
        myActivity = _myActivity;
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            label = (Label)getArguments().getSerializable("label");
//            myActivity = getActivity();
//            myActivity = (MainActivity)getArguments().getSerializable("activity");
            Log.e("in onCreate", "in onCreate "+label.getTitle());
        }
        data = new ArrayList<>();
        dbOperator = new DataBaseOperator(myActivity);
        dbOperator.initDataBase();
        data = dbOperator.queryAllNews(label.getTitle());
        adapter = new NewsItemAdapter(myActivity, data);
        ImgReader imgReader = new ImgReader();
        update_l = 0;
        update_r = data.size()-1;
        if(data.size() != 0)
            imgReader.execute(update_l);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_content, container, false);
        lv = (ListView)view.findViewById(R.id.myListView);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);

        pullToRefreshLayout = (PullToRefreshLayout)view.findViewById(R.id.pullToRefreshLayout);
        pullToRefreshLayout.setRefreshListener(this);
//        pullToRefreshLayout.setRefreshListener(new BaseRefreshListener() {
//            @Override
//            public void refresh() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        pullToRefreshLayout.finishRefresh();
//                    }
//                },2000);
//            }
//
//            @Override
//            public void loadMore() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        pullToRefreshLayout.finishLoadMore();
//                    }
//                },2000);
//            }
//        });
        this.container = container;
        if(data.size() == 0){
            if(isConnected()){
                pullToRefreshLayout.finishRefresh();
                RssReader reader = new RssReader();
                reader.execute(label, new Label("1", "1"));
            }
        }
        Log.e("in onCreateView", "in onCreateView "+label.getTitle());
        return view;
    }

    @Override
    public void refresh() {
        if(updating){
            Toast.makeText(myActivity, "刷新过于频繁，请稍后尝试", Toast.LENGTH_LONG).show();
            pullToRefreshLayout.finishRefresh();
            return;
        }
        if(isConnected()){
            RssReader reader = new RssReader();
            reader.execute(label, new Label("1", "1"));
        }
        else{
            Toast.makeText(myActivity, "网络未连接，更新失败", Toast.LENGTH_LONG).show();
            pullToRefreshLayout.finishRefresh();
        }
    }

    @Override
    public void loadMore() {
        if(updating){
            Toast.makeText(myActivity, "刷新过于频繁，请稍后尝试", Toast.LENGTH_LONG).show();
            pullToRefreshLayout.finishLoadMore();
            return;
        }
        if(isConnected()){
            RssReader reader = new RssReader();
            reader.execute(label, new Label("0", "0"));
        }
        else{
            Toast.makeText(myActivity, "网络未连接，更新失败", Toast.LENGTH_LONG).show();
            pullToRefreshLayout.finishLoadMore();
        }
    }

//    @Override
//    public void onRefresh() {
//        if(isConnected()){
//            RssReader reader = new RssReader();
//            reader.execute(label);
//        }
//        else{
//            Toast.makeText(myActivity, "网络未连接，更新失败", Toast.LENGTH_LONG).show();
//            stopRefresh();
//        }
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(myActivity, NewsDetailsActivity.class);
        intent.putExtra("url", data.get(position).getLink());
        intent.putExtra("pos", position);
        intent.putExtra("label", label.getTitle());
        intent.putExtra("title", data.get(position).getTitle());
        intent.putExtra("description", data.get(position).getDescription());
        myActivity.startActivityForResult(intent, 666);
    }


    //    public void stopRefresh(){
//        swipeRefreshLayout.setRefreshing(false);
//    }

    public static boolean isConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) myActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();
        if (networkinfo != null && networkinfo.isAvailable()) {
            String intentName = networkinfo.getTypeName();
            return true;
        } else {
            return false;
        }
    }


    public class RssReader extends AsyncTask<Label, Void , ArrayList<NewsItem>> {
        @Override
        protected ArrayList<NewsItem> doInBackground(Label... param) {
            RssHelper helper = new RssHelper();
            ArrayList<NewsItem> newsItems = new ArrayList<>();
            if(param.length != 0) {
                ArrayList<String> allXML = helper.getXML(param[0].getLink());

                NewsItem statusItem = new NewsItem();
                statusItem.setLink(param[1].getLink());
                newsItems.add(statusItem);

                int size = allXML.size();
                int count = 0;
                int count1 = 0;
                for(int i = 0; i < size; i++){
                    ArrayList<NewsItem> items = helper.getItems(allXML.get(i));
                    if(items.size() == 0)
                        count1++;
                    if(count1 == 2)
                        break;
                    for(NewsItem item: items){
                        if(dbOperator.queryNews(item.getTitle())==-1) {
                            newsItems.add(item);
                            count++;
                        }
                        if(count == 10)
                            return newsItems;
                    }
                }
//                if(allXML.size() > 1) {
//                    newsItems = helper.getItems("http://news.qq.com/newsgj/rss_newswj.xml");
//                }
            }
            return newsItems;
        }

        @Override
        protected void onPostExecute(ArrayList<NewsItem> newsItems) {
            pullToRefreshLayout.finishRefresh();
            pullToRefreshLayout.finishLoadMore();
            int index = data.size();
//            fragments.get(viewPager.getCurrentItem()).stopRefresh();
            if(newsItems.size()==1) {
                Toast.makeText(myActivity, "当前没有更多新闻哦", Toast.LENGTH_SHORT).show();
                return;
            }
            int temp = Integer.parseInt(newsItems.get(0).getLink());
            boolean status = false;
            if(temp == 1)
                status = true;

//            Button btn  = (Button)findViewById(R.id.btn1);
//            btn.setText(String.valueOf(newsItems.size()));
            int size = newsItems.size();
            if(status){
                update_l = 0;
                update_r = size-2;
                for(int i = size-1; i > 0; i--){
                    data.add(0, newsItems.get(i));
                    dbOperator.addData(newsItems.get(i), label.getTitle());
                }
            }
            else{
                for(int i = 1; i < size; i++){
                    data.add(newsItems.get(i));
                    dbOperator.addData(newsItems.get(i), label.getTitle());
                }
                update_l = index;
                update_r = data.size()-1;
            }

//            for(int i = 1; i < size; i++){
////                NewsItem item =
////                Map<String, Object> map = new HashMap<String, Object>();
////                map.put("title", newsItems.get(i).getTitle());
////                map.put("date", newsItems.get(i).getDate());
//                if(status)
//                    data.add(0, newsItems.get(i));
//                else
//                    data.add(newsItems.get(i));
//                dbOperator.addData(newsItems.get(i), label.getTitle());
////                if(dbOperator.queryNews(newsItems.get(i).getTitle())==-1) {
////                    data.add(newsItems.get(i));
////                    dbOperator.addData(newsItems.get(i), label.getTitle());
////                    count++;
////                }
//            }
            adapter.notifyDataSetChanged();
            Toast.makeText(myActivity, "已为您更新"+(size-1)+"条新闻", Toast.LENGTH_SHORT).show();
            ImgReader imgReader = new ImgReader();
            imgReader.execute(update_l);

//            imgReader.execute(index);
//            fragments.get(viewPager.getCurrentItem()).stopRefresh();
//            swipeRefreshLayout.setRefreshing(false);
//            newsItemAdapter.notifyDataSetChanged();
        }
    }

    public class ImgReader extends AsyncTask<Integer, Void , Pair<Bitmap, Integer>> {
        @Override
        protected Pair<Bitmap, Integer> doInBackground(Integer... params) {
            updating = true;
            Bitmap bitmap = null;
            int pos = -1;
            RssHelper helper = new RssHelper();
            if(params.length > 0){
                pos = params[0];
                bitmap = helper.getImg(data.get(pos).getLink());
            }
            return new Pair<>(bitmap, pos);
        }

        @Override
        protected void onPostExecute(Pair<Bitmap, Integer> bitmapIntegerPair) {
            int pos = bitmapIntegerPair.second;
            if(pos != -1) {
                data.get(pos).setBitmap(bitmapIntegerPair.first);
                adapter.notifyDataSetChanged();
//                if(pos < data.size()-1){
                if(pos < update_r){
                    ImgReader reader = new ImgReader();
                    reader.execute(pos+1);
                }
                else {
                    updating = false;
                }
            }
            super.onPostExecute(bitmapIntegerPair);
        }
    }

    public Label getLabel() {
        return label;
    }

    public boolean isUpdating(){
        return updating;
    }

    public void setVisited(int position){
        data.get(position).setVisited(true);
        adapter.notifyDataSetChanged();
    }
}


