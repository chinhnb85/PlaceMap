package com.example.chinhnb.placemap.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.chinhnb.placemap.App.AppController;
import com.example.chinhnb.placemap.App.SQLiteHandler;
import com.example.chinhnb.placemap.Dialog.DialogInfoWindowMap;
import com.example.chinhnb.placemap.Dialog.DialogSignin;
import com.example.chinhnb.placemap.Fragment.MapFragment;
import com.example.chinhnb.placemap.R;
import com.example.chinhnb.placemap.Utils.AppConfig;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

/**
 * Created by CHINHNB on 11/17/2016.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = MapFragment.class.getSimpleName();

    GoogleMap mMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;

    ProgressDialog pDialog;
    private String uid;
    private Double lag,lng;

    SupportMapFragment supportMapFragment;
    private SQLiteHandler db;
    Context context;
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        context=this;
        activity=this;

        db = new SQLiteHandler(context);
        uid=db.getUserDetails().get("uid");

        Bundle b = getIntent().getExtras();
        if(b!=null)
        {
            lag =b.getDouble("Lag");
            lng =b.getDouble("Lng");
        }

        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;

        LatLng latLngDefault = new LatLng((lag==null)?21.0277645:lag, (lng==null)?105.8341581:lng);
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngDefault));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        mMap.setInfoWindowAdapter(new DialogInfoWindowMap(activity));

        ListLocaltionByUserId(uid);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(context, "Permission was denied. Display an error message", Toast.LENGTH_LONG).show();
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        View locationButton = ((View) supportMapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 0, 0);

        /*mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Marker hanoi= mMap.addMarker(new MarkerOptions().position(latLng).title("Thêm địa chỉ"));
                hanoi.setTag(0);
                hanoi.setDraggable(true);
                hanoi.showInfoWindow();
            }
        });*/
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                marker.showInfoWindow();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        marker.showInfoWindow();
                    }
                }, 300);

                return true;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                Intent intent = new Intent(MapActivity.this, CheckedActivity.class);
                if(mLastLocation!=null) {
                    intent.putExtra("Id", Integer.parseInt(marker.getTag().toString()));
                    intent.putExtra("AccountId", Integer.parseInt(uid));
                    intent.putExtra("Lag", mLastLocation.getLatitude());
                    intent.putExtra("Lng", mLastLocation.getLongitude());
                }else{
                    intent.putExtra("Id", Integer.parseInt(marker.getTag().toString()));
                    intent.putExtra("AccountId", Integer.parseInt(uid));
                    intent.putExtra("Lag", Double.valueOf("21.0277645"));
                    intent.putExtra("Lng", Double.valueOf("105.8341581"));
                }
                startActivityForResult(intent,2);

            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void setUpMapIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        if (mMap == null) {
            supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if(supportMapFragment!=null) {
                supportMapFragment.getMapAsync(this);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Hiện tại");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
//        mCurrLocationMarker = mMap.addMarker(markerOptions);
//        mCurrLocationMarker.setTag("currentlocaltion");

        //move map camera
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(context, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void ListLocaltionByUserId(final String userId) {
        String tag_string_req = "req_localtion";

        pDialog.setMessage("Đang tải dữ liệu...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LIST_LOCALTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response.toString());
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean status = jObj.getBoolean("status");
                    if (status) {
                        JSONArray array=jObj.getJSONArray("Data");
                        if(array.length()>0){
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject item = array.getJSONObject(i);
                                LatLng local = new LatLng(item.getDouble("Lag"), item.getDouble("Lng"));
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(local);
                                markerOptions.title(item.getString("Name"));
                                String ischeck=item.getString("IsCheck");
                                if(ischeck!="null" && item.getBoolean("IsCheck")){
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                }else{
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                }
                                final Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(item.getString("Id"));
                                marker.setSnippet(item.getString("Avatar")+"#@#"+item.getString("Address")+"#@#"+item.getString("Phone"));
                                //marker.setDraggable(true);
                                if(lag==item.getDouble("Lag") && lng==item.getDouble("Lng")){
                                    marker.showInfoWindow();

                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            marker.showInfoWindow();
                                        }
                                    }, 300);
                                }
                            }
                        }

                    } else {
                        String errorMsg = jObj.getString("message");
                        Toast.makeText(context,
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Json error", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(context,
                        getResources().getString(R.string.not_network), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("Id", userId);

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
