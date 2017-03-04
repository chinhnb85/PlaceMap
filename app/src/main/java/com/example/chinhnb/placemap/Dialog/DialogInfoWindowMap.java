package com.example.chinhnb.placemap.Dialog;

import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.Other.CircleTransform;
import com.example.chinhnb.placemap.R;
import com.example.chinhnb.placemap.Utils.AppConfig;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by CHINHNB on 11/13/2016.
 */

public class DialogInfoWindowMap implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = DialogInfoWindowMap.class.getSimpleName();

    private final View mWindow;

    private final Activity activity;

    public DialogInfoWindowMap(Activity activity) {
        this.activity=activity;
        mWindow = activity.getLayoutInflater().inflate(R.layout.custom_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private void render(Marker marker, View view) {

        ImageView imgavatar=(ImageView) view.findViewById(R.id.imgavatar);
        //imgavatar.setBackgroundResource(R.drawable.ic_noavatar);
        String[] arraySnippet = marker.getSnippet().split("#@#");
        String avatar = "";
        if(arraySnippet.length>0){
            avatar = AppConfig.URL_ROOT + arraySnippet[0];
        }

        Log.d(TAG, "DialogInfoWindowMap: " + avatar);
        Glide.with(activity).load(avatar)
                .crossFade()
                .thumbnail(0.5f)
                //.bitmapTransform(new CircleTransform(activity))
                .placeholder(R.drawable.ic_loading)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgavatar);

        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        if (title != null) {
            titleUi.setText(title);
        } else {
            titleUi.setText("");
        }

        LatLng latLng = marker.getPosition();
        TextView txtlatLng = ((TextView) view.findViewById(R.id.latLng));
        if (latLng != null) {
            txtlatLng.setText("Vị trí: "+latLng.latitude+" , "+latLng.longitude);
        } else {
            txtlatLng.setText("");
        }

        String address = "";
        if(arraySnippet.length>1){
            address = arraySnippet[1];
        }
        TextView txtAddress = ((TextView) view.findViewById(R.id.address));
        if (address != null) {
            txtAddress.setText("Đ/c: "+address);
        } else {
            txtAddress.setText("");
        }

        String phone = "";
        if(arraySnippet.length>2){
            phone = (arraySnippet[2].toLowerCase().equals("null"))?"":arraySnippet[2];
        }
        TextView txtPhone = ((TextView) view.findViewById(R.id.phone));
        if (phone != null) {
            txtPhone.setText("Điện thoại: "+phone);
        } else {
            txtPhone.setText("");
        }
    }
}
