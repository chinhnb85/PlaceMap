package com.example.chinhnb.placemap.Adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.Other.CircleTransform;
import com.example.chinhnb.placemap.R;

import java.util.List;

/**
 * Created by CHINHNB on 11/15/2016.
 */

public class LocaltionAdapter extends RecyclerView.Adapter<LocaltionAdapter.ViewHolder> {
    private static final String TAG = "LocaltionAdapter";

    private List<Localtion> localtionList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, address, laglng, ischeck;
        public ImageView avatar;
        public ViewHolder(View view) {
            super(view);

            avatar=(ImageView) view.findViewById(R.id.avatar);
            name = (TextView) view.findViewById(R.id.name);
            address = (TextView) view.findViewById(R.id.address);
            laglng = (TextView) view.findViewById(R.id.laglng);
            ischeck = (TextView) view.findViewById(R.id.ischeck);
        }
    }

    public LocaltionAdapter(List<Localtion> localtionList) {
        this.localtionList = localtionList;
    }

    @Override
    public LocaltionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.localtion_list_row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "Element " + position + " set.");

        Localtion localtion = localtionList.get(position);

        Uri uri=Uri.parse(localtion.getAvatar());
        Context context=holder.avatar.getContext();
        Glide.with(context).load(uri)
                .crossFade()
                .thumbnail(0.5f)
                //.bitmapTransform(new CircleTransform(context))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.avatar);
        //holder.avatar.setImageResource(R.drawable.badge_sa);
        holder.name.setText(localtion.getName());
        holder.address.setText(localtion.getAddress());
        holder.laglng.setText(localtion.getLag()+" , "+localtion.getLng());
        if(localtion.getIsCheck()) {
            holder.ischeck.setText("Đã checked");
        }else{
            holder.ischeck.setText("Chưa checked");
        }
    }

    @Override
    public int getItemCount() {
        return localtionList.size();
    }
}
