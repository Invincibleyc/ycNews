package com.java.yechong;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;

import cn.sharesdk.onekeyshare.OnekeyShare;

public class NewsDetailsActivity extends Activity {
    private boolean like;
    private View.OnClickListener listener;
    private int position;
    private String label;
    private String title;
    private DataBaseOperator dbOperator;
    private String url;
    private String description;
    private String imgLink = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        like = false;
        setContentView(R.layout.news_details_activity);
        WebView view = (WebView)findViewById(R.id.webView);
        WebSettings settings = view.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        view.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        position = intent.getIntExtra("pos", -1);
        label = intent.getStringExtra("label");
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");

        dbOperator = new DataBaseOperator(this);
        dbOperator.initDataBase();

        view.loadUrl(url);
//        view.loadUrl("http://ent.qq.com/a/20180907/080077.htm");
//        view.loadUrl("http://www.baidu.com");
        FloatingActionMenu floatingActionMenu = (FloatingActionMenu)findViewById(R.id.fab_menu);
        FloatingActionButton likeButton = (FloatingActionButton)findViewById(R.id.likeButton);
        FloatingActionButton shareButton = (FloatingActionButton)findViewById(R.id.shareButton);

        int id = dbOperator.queryFavoriteNews(title);
        if(id == -1){
            likeButton.setImageResource(R.mipmap.like_gray);
            like = false;
        }
        else{
            likeButton.setImageResource(R.mipmap.like_red);
            like = true;
        }
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.likeButton:
                        FloatingActionButton btn = (FloatingActionButton)v;
                        if(like){
                            btn.setImageResource(R.mipmap.like_gray);
                            like = false;
                            Toast.makeText(NewsDetailsActivity.this, "已取消收藏", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            btn.setImageResource(R.mipmap.like_red);
                            like = true;
                            Toast.makeText(NewsDetailsActivity.this, "已经收藏", Toast.LENGTH_SHORT).show();
                        }
                        dbOperator.updateData(title, like);
                        break;
                    case R.id.shareButton:
                        AlertDialog.Builder builder = new AlertDialog.Builder(NewsDetailsActivity.this,
                                android.R.style.Theme_Material_Light_Dialog_Alert);
                        builder.setTitle("请选择分享方式");
                        builder.setIcon(R.mipmap.ic_launcher);
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        final String[] cities = {"仅分享文字", "分享完整信息"};
                        builder.setItems(cities, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which == 0){
                                    Intent sendIntent = new Intent();
                                    sendIntent.setAction(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, title+"\n"+description+"……\n"+url);
                                    sendIntent.setType("text/plain");
                                    startActivity(Intent.createChooser(sendIntent, "分享至"));
                                }
                                else{
                                    shareHelper helper = new shareHelper();
                                    helper.execute();
                                }
                            }
                        });
                        builder.create();
                        builder.show();
                        break;
                }
            }
        };
        likeButton.setOnClickListener(listener);
        shareButton.setOnClickListener(listener);

        TextAnalyzer.analysis(description, dbOperator);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("pos", position);
        intent.putExtra("label", label);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    public class shareHelper extends AsyncTask<Void, Void , String> {
        @Override
        protected String doInBackground(Void... voids) {
            RssHelper helper = new RssHelper();
            imgLink = helper.getImgLink(url);
            return imgLink;
        }

        @Override
        protected void onPostExecute(String s) {
            share();
        }
    }

    private void share() {

        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // title标题，微信、QQ和QQ空间等平台使用
        oks.setTitle(title);
        // titleUrl QQ和QQ空间跳转链接
        oks.setTitleUrl(url);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(description);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        if(!imgLink.equals("")){
            oks.setImageUrl(imgLink);
//            if(!imgLink.contains(".jpg") && !imgLink.contains(".png")){
//                imgLink = imgLink + ".png";
//            }
//            oks.setImageUrl(imgLink);
        }
        else
            oks.setImageData(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
//        oks.setImageUrl("https://sumile.cn/wp-content/uploads/2017/05/sharesdk1.png");
//        oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url在微信、微博，Facebook等平台中使用
        oks.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网使用
        oks.setComment("评论文本测试");
        oks.setSite("YC_News");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(url);
        // 启动分享GUI
        oks.show(this);
    }
}
