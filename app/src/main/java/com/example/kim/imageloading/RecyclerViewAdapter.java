package com.example.kim.imageloading;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by KIM on 2018-04-11.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<ImageItem> items;
    private ImageLoader imageLoader;

    public RecyclerViewAdapter(Context context) {
        this.context = context;
        items = new ArrayList<>();
        imageLoader = new ImageLoader(this.context);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;
        protected TextView type;        // 어떤 방식으로 이미지를 가져왔는지 표시. 메모리, 디스크, 다운로드
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.iv_recycler_view_item_image);
            type = (TextView)itemView.findViewById(R.id.tv_recycler_view_item_type);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;

        ImageItem imageItem = items.get(position);
        imageItem.setUrl(imageItem.getUrl());
        imageLoader.displayImage(imageItem, viewHolder.imageView, viewHolder.type);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.type.setText("");
        viewHolder.type.setBackgroundColor(Color.TRANSPARENT);
    }

    public void setItems(ArrayList<ImageItem> items) {
        this.items = items;
    }

    public void clearCache() {
        imageLoader.clearCache();
    }
}
