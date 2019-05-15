package com.java.yechong;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class RecommendActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private DataBaseOperator dbOperator;
    private ListView lv;
    private NewsItemAdapter adapter;
    private ArrayList<NewsItem> data;
    private ArrayList<NewsItem> recommendData;
    private ArrayList<Integer> scores;
    private int update_r;
    private boolean updating = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);
        Toolbar toolbar = (Toolbar)findViewById(R.id.recommend_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        data = new ArrayList<>();
        recommendData = new ArrayList<>();
        scores = new ArrayList<>();
        dbOperator = new DataBaseOperator(this);
        dbOperator.initDataBase();
        data = dbOperator.queryAllNews();
        adapter = new NewsItemAdapter(this, recommendData);
        lv = (ListView)findViewById(R.id.recommend_listView);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);

        HashMap<String, Integer> mydict;
        mydict = dbOperator.queryAllWords();
        if(mydict.isEmpty()) {
            int size = 20;
            if(data.size() < 20)
                size = data.size();
            for(int i = 0; i < size; i++){
                recommendData.add(data.get(i));
            }
            adapter.notifyDataSetChanged();
            ImgReader reader = new ImgReader();
            update_r = size;
//            reader.execute(0);
//            updating = true;
            return;
        }
        for(NewsItem item : data){
            int score = TextAnalyzer.evaluate(mydict, item.getDescription());
            scores.add(score);
        }
        int size = 20;
        if(data.size() < size)
            size = data.size();
        for(int i = 0; i < size; i++){
            int index = 0;
            int highestScore = 0;
            int data_size = data.size();
            for(int j = 0; j < data_size; j++){
                if(highestScore < scores.get(j)){
                    highestScore = scores.get(j);
                    index = j;
                }
            }
            recommendData.add(data.remove(index));
            scores.remove(index);
        }
        ImgReader reader = new ImgReader();
        update_r = size;
//        reader.execute(0);
//        updating = true;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(updating){
            Toast.makeText(RecommendActivity.this, "网络繁忙，请稍后", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.setClass(RecommendActivity.this, NewsDetailsActivity.class);
        intent.putExtra("url", recommendData.get(position).getLink());
        intent.putExtra("pos", position);
        intent.putExtra("label", "");
        intent.putExtra("title", recommendData.get(position).getTitle());
        intent.putExtra("description", recommendData.get(position).getDescription());
        startActivityForResult(intent, 666);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        int position = data.getIntExtra("pos", -1);
        recommendData.get(position).setVisited(true);
        adapter.notifyDataSetChanged();
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class ImgReader extends AsyncTask<Integer, Void , Pair<Bitmap, Integer>> {
        @Override
        protected Pair<Bitmap, Integer> doInBackground(Integer... params) {
            if(!updating)
                return null;
            Bitmap bitmap = null;
            int pos = -1;
            RssHelper helper = new RssHelper();
            if(params.length > 0){
                pos = params[0];
                bitmap = helper.getImg(recommendData.get(pos).getLink());
            }
            return new Pair<>(bitmap, pos);
        }

        @Override
        protected void onPostExecute(Pair<Bitmap, Integer> bitmapIntegerPair) {
            if(bitmapIntegerPair == null)
                return;
            int pos = bitmapIntegerPair.second;
            if(pos != -1) {
                recommendData.get(pos).setBitmap(bitmapIntegerPair.first);
                adapter.notifyDataSetChanged();
                if(pos < update_r){
                    ImgReader reader = new ImgReader();
                    reader.execute(pos+1);
                }
                else{
                    updating = false;
                }
            }
            super.onPostExecute(bitmapIntegerPair);
        }
    }

    @Override
    public void onBackPressed() {
        updating = false;
        super.onBackPressed();
    }
}
