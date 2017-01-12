package com.example.chinhnb.placemap.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CHINHNB on 1/11/2017.
 */

public class AutoCompleteAdapter extends ArrayAdapter<Localtion> {

    Context context;
    int resource, textViewResourceId;
    List<Localtion> items, tempItems, suggestions;

    public AutoCompleteAdapter(Context context, int resource, int textViewResourceId, List<Localtion> items) {
        super(context, resource, textViewResourceId, items);
        this.context = context;
        this.resource = resource;
        this.textViewResourceId = textViewResourceId;
        this.items = items;
        tempItems = new ArrayList<>(items); // this makes the difference.
        suggestions = new ArrayList<Localtion>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row_autocomplete, parent, false);
        }
        Localtion people = items.get(position);
        if (people != null) {
            TextView lblName = (TextView) view.findViewById(R.id.lbl_name);
            if (lblName != null)
                lblName.setText(people.getName());
        }
        return view;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String str = ((Localtion) resultValue).getName();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (Localtion people : tempItems) {
                    if (people.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(people);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<Localtion> filterList = (ArrayList<Localtion>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (Localtion people : filterList) {
                    add(people);
                    notifyDataSetChanged();
                }
            }
        }
    };
}
