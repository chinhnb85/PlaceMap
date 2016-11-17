package com.example.chinhnb.placemap.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chinhnb.placemap.R;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by CHINHNB on 11/17/2016.
 */

public class MapFragment extends SupportMapFragment {

    public static final String TAG = "map";

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public MapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_map, container,
                false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
