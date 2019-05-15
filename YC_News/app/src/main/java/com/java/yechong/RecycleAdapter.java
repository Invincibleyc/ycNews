package com.java.yechong;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pchmn.materialchips.ChipView;

import java.util.ArrayList;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.Holder> {
    private Context context;
    private ArrayList<Label> list;
    private ArrayList<Label> removedList;
    private int icon;
    private OnItemClickListener clickListener;

    public RecycleAdapter(Context context, ArrayList<Label> list, int icon){
        this.context = context;
        this.list = list;
        removedList = new ArrayList<>();
        this.icon = icon;
    }

    class Holder extends RecyclerView.ViewHolder{
        ChipView chipView;
        public Holder(View item){
            super(item);
            chipView = (ChipView)item.findViewById(R.id.chip_view);
            chipView.setOnDeleteClicked(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickListener != null)
                        clickListener.onClick(Holder.this.getAdapterPosition());

                }
            });
        }
    }

    public static interface OnItemClickListener {
        void onClick(int position);
    }

    public void setClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        String str = list.get(i).getTitle();
        holder.chipView.setLabel(str);
        if(icon != -1){
            Resources resources = context.getResources();
            Drawable drawable = resources.getDrawable(R.drawable.add);
            holder.chipView.setDeleteIcon(drawable);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chip_item, viewGroup, false);
        Holder holder = new Holder(view);
        return holder;
    }
}

