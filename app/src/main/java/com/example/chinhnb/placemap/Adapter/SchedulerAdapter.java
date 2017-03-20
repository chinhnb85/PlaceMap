package com.example.chinhnb.placemap.Adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.chinhnb.placemap.Entity.HeaderItem;
import com.example.chinhnb.placemap.Entity.ListItem;
import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.Entity.LocaltionItem;
import com.example.chinhnb.placemap.Other.CircleTransform;
import com.example.chinhnb.placemap.R;
import com.example.chinhnb.placemap.Utils.AppConfig;
import com.example.chinhnb.placemap.Utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by CHINHNB on 11/15/2016.
 */

public class SchedulerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "SchedulerAdapter";

    @NonNull
    private List<ListItem> items = Collections.emptyList();

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txt_header;
        public HeaderViewHolder(View itemView) {
            super(itemView);

            txt_header = (TextView) itemView.findViewById(R.id.txt_header);
        }
    }

    private static class LocaltionViewHolder extends RecyclerView.ViewHolder {
        public TextView code, name, address, ischeck,statusname,note;
        public ImageView avatar;
        public LocaltionViewHolder(View view) {
            super(view);

            avatar=(ImageView) view.findViewById(R.id.avatar);
            code = (TextView) view.findViewById(R.id.code);
            name = (TextView) view.findViewById(R.id.name);
            address = (TextView) view.findViewById(R.id.address);
            ischeck = (TextView) view.findViewById(R.id.ischeck);
            statusname = (TextView) view.findViewById(R.id.statusname);
            note = (TextView) view.findViewById(R.id.note);
        }
    }

    public SchedulerAdapter(@NonNull List<ListItem> items) {

        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ListItem.TYPE_HEADER: {
                View itemView = inflater.inflate(R.layout.scheduler_header_row, parent, false);
                return new HeaderViewHolder(itemView);
            }
            case ListItem.TYPE_LOCALTION: {
                View itemView = inflater.inflate(R.layout.scheduler_list_row, parent, false);
                return new LocaltionViewHolder(itemView);
            }
            default:
                throw new IllegalStateException("unsupported item type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ListItem.TYPE_HEADER: {
                HeaderItem header = (HeaderItem) items.get(position);
                HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
                // your logic here
                holder.txt_header.setText(DateUtils.formatDate(header.getDate()));
                break;
            }
            case ListItem.TYPE_LOCALTION: {
                LocaltionItem localtion = (LocaltionItem) items.get(position);
                LocaltionViewHolder holder = (LocaltionViewHolder) viewHolder;

                Uri uri=Uri.parse(AppConfig.URL_ROOT + localtion.getLocaltion().getAvatar());
                Context context=holder.avatar.getContext();
                Glide.with(context).load(uri)
                        .crossFade()
                        .thumbnail(0.5f)
                        .bitmapTransform(new CircleTransform(context))
                        .placeholder(R.drawable.ic_loading)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.avatar);
                holder.code.setText(localtion.getLocaltion().getName());
                holder.name.setText(localtion.getLocaltion().getPhone());
                holder.address.setText(localtion.getLocaltion().getAddress());
                holder.statusname.setText(localtion.getLocaltion().getStatusName());
                String countStr="Số lần viếng thăm trong tháng: "+localtion.getLocaltion().getCountCheckIn()+" / "+localtion.getLocaltion().getMinCheckin();
                holder.ischeck.setText(countStr);
                if(localtion.getLocaltion().getCountCheckIn()==0) {
                    holder.ischeck.setTextColor(context.getResources().getColor(R.color.red));
                }else if(localtion.getLocaltion().getCountCheckIn()< localtion.getLocaltion().getMinCheckin()){
                    holder.ischeck.setTextColor(context.getResources().getColor(R.color.yelow));
                }else{
                    holder.ischeck.setTextColor(context.getResources().getColor(R.color.green));
                }
                holder.note.setText(localtion.getLocaltion().getNote());

                break;
            }
            default:
                throw new IllegalStateException("unsupported item type");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }
}
