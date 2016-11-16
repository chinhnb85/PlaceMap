package com.example.chinhnb.placemap.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.example.chinhnb.placemap.Dialog.DialogInfoWindowMap;
import com.example.chinhnb.placemap.Dialog.DialogSignin;
import com.example.chinhnb.placemap.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

/**
 * Created by CHINHNB on 11/17/2016.
 */

public class MapActivity extends AppCompatActivity implements GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerDragListener,OnMapReadyCallback {

    private GoogleMap mMap;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng HANOI = new LatLng(21.0227002, 105.801944);
        Marker hanoi = mMap.addMarker(new MarkerOptions().position(HANOI).title("Marker in Hà Nội"));
        hanoi.setTag(0);
        hanoi.setDraggable(true);

        mMap.setInfoWindowAdapter(new DialogInfoWindowMap(this));

        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HANOI, 12));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Marker hanoi= mMap.addMarker(new MarkerOptions().position(latLng).title("Thêm địa chỉ"));
        hanoi.setTag(0);
        hanoi.setDraggable(true);
        hanoi.showInfoWindow();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        //makeText(this,marker.getTitle(), LENGTH_SHORT).show();

        return false;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        DialogFragment newFragment = new DialogSignin();
        newFragment.show(getSupportFragmentManager(), "missiles");
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        makeText(this, "DragStart"+marker.getPosition().toString(),
                LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        makeText(this, "Drag"+marker.getPosition().toString(),
                LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        makeText(this, "DragEnd"+marker.getPosition().toString(),
                LENGTH_SHORT).show();
    }
}
