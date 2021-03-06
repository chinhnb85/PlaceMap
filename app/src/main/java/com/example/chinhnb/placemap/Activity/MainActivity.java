package com.example.chinhnb.placemap.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.chinhnb.placemap.App.AppController;
import com.example.chinhnb.placemap.Dialog.DialogInfoWindowMap;
import com.example.chinhnb.placemap.Entity.AccountPlace;
import com.example.chinhnb.placemap.Fragment.*;
import com.example.chinhnb.placemap.Other.CircleTransform;
import com.example.chinhnb.placemap.App.SQLiteHandler;
import com.example.chinhnb.placemap.Other.SessionManager;
import com.example.chinhnb.placemap.Services.AlarmReceiver;
import com.example.chinhnb.placemap.Services.GPSTracker;
import com.example.chinhnb.placemap.Utils.*;
import com.example.chinhnb.placemap.R;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static int navItemIndex = 0;
    public static String CURRENT_TAG = Const.TAG_MAP;

    private Handler mHandler;

    private DrawerLayout drawer;
    private FloatingActionButton fab,fabCheckIn;

    private GoogleMap mMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    SupportMapFragment supportMapFragment;

    private SQLiteHandler db;
    private SessionManager session;

    private CoordinatorLayout coordinatorLayout;
    private ProgressDialog pDialog;
    private String uid;
    Context context;
    Activity activity;

    private String[] activityTitles;
    GPSTracker gps;
    boolean flag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;
        activity=this;

        db = new SQLiteHandler(getApplicationContext());
        uid=db.getUserDetails().get("uid");

        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {
            logoutUser();
            return;
        }

        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddNewActivity.class);
                if(mLastLocation!=null) {
                    intent.putExtra("Lag", mLastLocation.getLatitude());
                    intent.putExtra("Lng", mLastLocation.getLongitude());
                    intent.putExtra("AccountId", Integer.parseInt(uid));
                }else{
                    intent.putExtra("Lag", Double.valueOf("21.0277645"));
                    intent.putExtra("Lng", Double.valueOf("105.8341581"));
                    intent.putExtra("AccountId", Integer.parseInt(uid));
                }
                startActivityForResult(intent,2);
            }
        });
        fabCheckIn = (FloatingActionButton) findViewById(R.id.fabCheckIn);
        fabCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check in
                if(mLastLocation!=null && flag) {
                    flag=false;
                    AccountPlace loc = new AccountPlace(0, Integer.parseInt(uid), mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    InsertLocaltionUser(loc);
                    Toast.makeText(getApplicationContext(), "Checkin thành công.", Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            flag=true;
                            Log.e(TAG, "Hết 5 phút");
                        }
                    }, 1000*60*5);
                }else if(mLastLocation!=null && !flag) {
                    Toast.makeText(getApplicationContext(), "Sau 5 phút mới checkin lại.", Toast.LENGTH_LONG).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeader = navigationView.getHeaderView(0);
        ImageView imgProfile = (ImageView) navHeader.findViewById(R.id.imageView);
        // Loading profile image
        Glide.with(context).load(R.drawable.ic_noavatar)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(context))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile);
        TextView txtName = (TextView) navHeader.findViewById(R.id.textName);
        TextView txtEmail = (TextView) navHeader.findViewById(R.id.textEmail);
        HashMap<String, String> user = db.getUserDetails();
        txtName.setText(user.get("name"));
        txtEmail.setText(user.get("email"));

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = Const.TAG_MAP;
            loadHomeFragment();

            //check in open app
            gps = new GPSTracker(context);
            // check if GPS enabled
            if(gps.canGetLocation()){
                AccountPlace loc = new AccountPlace(0, Integer.parseInt(uid), gps.getLatitude(), gps.getLongitude());
                InsertLocaltionUser(loc);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            ListLocaltionByUserId(uid);
        }
    }

    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadHomeFragment() {

        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // set toolbar title
                setToolbarTitle();

                if(navItemIndex!=0) {
                    supportMapFragment.getView().setVisibility(View.INVISIBLE);
                    FrameLayout frameLayout=(FrameLayout)findViewById(R.id.frame);
                    frameLayout.setVisibility(View.VISIBLE);

                    Fragment fragment = getHomeFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                    fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                    fragmentTransaction.commitAllowingStateLoss();
                }else{
                    supportMapFragment.getView().setVisibility(View.VISIBLE);
                    FrameLayout frameLayout=(FrameLayout)findViewById(R.id.frame);
                    frameLayout.setVisibility(View.INVISIBLE);
                }
            }
        };

        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        toggleFab();
        drawer.closeDrawers();

        invalidateOptionsMenu();
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                MapFragment mapFragment = new MapFragment();
                return mapFragment;
            case 1:
                LocaltionFragment localtionFragment = new LocaltionFragment();
                return localtionFragment;
            case 2:
                SchedulerFragment schedulerFragment = new SchedulerFragment();
                return schedulerFragment;
            case 3:
                AboutFragment aboutFragment = new AboutFragment();
                return aboutFragment;
            default:
                return new MapFragment();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (navItemIndex != 0) {
            navItemIndex = 0;
            CURRENT_TAG = Const.TAG_MAP;
            loadHomeFragment();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            navItemIndex = 0;
            CURRENT_TAG = Const.TAG_MAP;
        } else if (id == R.id.nav_photos) {
            navItemIndex = 1;
            CURRENT_TAG = Const.TAG_LOCALTION;
        } else if (id == R.id.nav_scheduler) {
            navItemIndex = 2;
            CURRENT_TAG = Const.TAG_SCHEDULER;
        }else if (id == R.id.nav_about_us) {
            navItemIndex = 3;
            CURRENT_TAG = Const.TAG_ABOUT;
        } else if (id == R.id.nav_logout) {
            navItemIndex = 4;
            CURRENT_TAG = Const.TAG_LOGOUT;
            logoutUser();
        }

        loadHomeFragment();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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
                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(item.getString("Id"));
                                marker.setSnippet(item.getString("Avatar")+"#@#"+item.getString("Address")+"#@#"+item.getString("Phone"));
                                //marker.setDraggable(true);
                            }
                        }

                    } else {
                        String errorMsg = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng latLngDefault = new LatLng(21.0277645, 105.8341581);
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngDefault));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        mMap.setInfoWindowAdapter(new DialogInfoWindowMap(activity));

        ListLocaltionByUserId(uid);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                Snackbar.make(coordinatorLayout, "Permission was denied. Display an error message", Snackbar.LENGTH_LONG).show();
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
        rlp.setMargins(0, 180, 180, 0);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick()
            {
                /*if(mLastLocation!=null) {
                    AccountPlace loc = new AccountPlace(0, Integer.parseInt(uid), mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    Log.e(TAG, "onMyLocationButtonClick: " + loc.toString());

                    InsertLocaltionUser(loc);
                }*/
                //LoginActivity.startAt30(context);
                return false;
            }
        });

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
                //mMap.setInfoWindowAdapter(new DialogInfoWindowMap(activity));
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
                Intent intent = new Intent(MainActivity.this, CheckedActivity.class);
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
        mGoogleApiClient = new GoogleApiClient.Builder(this)
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
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if(mapFragment!=null) {
                mapFragment.getMapAsync(this);
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

    private void toggleFab() {
        if (navItemIndex == 0) {
            fab.show();
            fabCheckIn.show();
        }else {
            fab.hide();
            fabCheckIn.hide();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
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
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
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
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void InsertLocaltionUser(final AccountPlace loc) {
        Log.d(TAG, "Data: " +loc.getLag()+" ; "+loc.getLng());
        // Tag used to cancel the request
        String tag_string_req = "req_autolocaltion";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_AUTO_LOCALTION_USER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean status = jObj.getBoolean("status");
                    if (status) {
                        String msg = jObj.getString("message");
                        Log.d(TAG, "Success: " + msg);
                    } else {
                        String errorMsg = jObj.getString("message");
                        Log.d(TAG, "errorMsg: " + errorMsg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                params.put("AccountId", String.valueOf(loc.getAccountId()));
                params.put("Lag", loc.getLag().toString());
                params.put("Lng", loc.getLng().toString());

                Log.d(TAG, "params: " +params);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
