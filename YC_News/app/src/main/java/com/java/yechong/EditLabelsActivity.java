package com.java.yechong;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

public class EditLabelsActivity extends AppCompatActivity {
    private ArrayList<Label> data;
    private ArrayList<Label> data_removed;
    private RecyclerView recyclerView;
    private RecyclerView recyclerView_removed;
    private RecycleAdapter adapter;
    private RecycleAdapter adapter_removed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_labels);

        Toolbar toolbar = (Toolbar)findViewById(R.id.edit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        data = new ArrayList<>();
        data = (ArrayList<Label>) intent.getSerializableExtra("data");
        data_removed = (ArrayList<Label>) intent.getSerializableExtra("data_removed");

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        ChipsLayoutManager tagsLayoutManager = ChipsLayoutManager.newBuilder(this)
                .setRowStrategy(ChipsLayoutManager.STRATEGY_CENTER)
                .withLastRow(true)
                .build();
        adapter = new RecycleAdapter(this, data, -1);
        recyclerView.setLayoutManager(tagsLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(new RecycleAdapter.OnItemClickListener() {
            @Override
            public void onClick( int position) {
                int size = data_removed.size();
                data_removed.add(data.remove(position));
                adapter.notifyItemRemoved(position);
                adapter_removed.notifyItemInserted(size);
            }
        });

        ChipsLayoutManager tagsLayoutManager_removed = ChipsLayoutManager.newBuilder(this)
                .setRowStrategy(ChipsLayoutManager.STRATEGY_CENTER)
                .withLastRow(true)
                .build();
        recyclerView_removed = (RecyclerView)findViewById(R.id.recycler_view_removed);
        adapter_removed = new RecycleAdapter(this, data_removed, 1);
        recyclerView_removed.setLayoutManager(tagsLayoutManager_removed);
        recyclerView_removed.setItemAnimator(new DefaultItemAnimator());
        recyclerView_removed.setAdapter(adapter_removed);
        adapter_removed.setClickListener(new RecycleAdapter.OnItemClickListener() {
            @Override
            public void onClick( int position) {
                int size = data.size();
                data.add(data_removed.remove(position));
                adapter_removed.notifyItemRemoved(position);
                adapter.notifyItemInserted(size);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("data", data);
        intent.putExtra("data_removed", data_removed);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }


}
