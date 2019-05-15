package com.java.yechong;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

public class AboutActivity extends AppCompatActivity {
    private String about="YC News:\n清华计算机系叶翀开发的新闻阅读APP\n";
    private String connect="QQ: 1305465643\n微信: 15044494950\n";
    private String version="YC News V1.0.0";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView tv = (TextView)findViewById(R.id.about_tv);
        tv.setText(about);
        TextView tv1 = (TextView)findViewById(R.id.connect_tv);
        tv1.setText(connect);
        TextView tv2 = (TextView)findViewById(R.id.version_tv);
        tv2.setText(version);

//        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
//        iv = (ImageView) findViewById(R.id.iv);
        Toolbar toolbar = (Toolbar)findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
//        toolbar.setNavigationIcon(R.mipmap.ic_drawer_home);
//        collapsingToolbarLayout.setTitle("DesignLibrarySample");
//        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
//        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
    }
}
