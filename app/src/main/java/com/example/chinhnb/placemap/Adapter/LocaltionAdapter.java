package com.example.chinhnb.placemap.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.R;

import java.util.List;

/**
 * Created by CHINHNB on 11/15/2016.
 */

public class LocaltionAdapter extends RecyclerView.Adapter<LocaltionAdapter.ViewHolder> {
    private List<Localtion> localtionList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, year, genre;
        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            genre = (TextView) view.findViewById(R.id.genre);
            year = (TextView) view.findViewById(R.id.year);
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
        Localtion localtion = localtionList.get(position);
        holder.title.setText(localtion.getTitle());
        holder.genre.setText(localtion.getGenre());
        holder.year.setText(localtion.getYear());

    }

    @Override
    public int getItemCount() {
        return localtionList.size();
    }
}
