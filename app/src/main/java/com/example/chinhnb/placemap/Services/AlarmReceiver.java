package com.example.chinhnb.placemap.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.chinhnb.placemap.Activity.AddNewActivity;
import com.example.chinhnb.placemap.App.AppController;
import com.example.chinhnb.placemap.App.SQLiteHandler;
import com.example.chinhnb.placemap.Entity.AccountPlace;
import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.R;
import com.example.chinhnb.placemap.Utils.AppConfig;
import com.google.android.gms.location.LocationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CHINHNB on 12/30/2016.
 */

public class AlarmReceiver extends BroadcastReceiver implements LocationListener {

    private static final String TAG = AlarmReceiver.class.getSimpleName();
    Location mLastLocation;
    private SQLiteHandler db;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "running...");

        if(mLastLocation!=null){
            db = new SQLiteHandler(context);
            String uid=db.getUserDetails().get("uid");
            int accountId=Integer.parseInt(uid);
            Double lag=Double.valueOf(mLastLocation.getLatitude());
            Double lng=Double.valueOf(mLastLocation.getLongitude());
            String device="";

            AccountPlace loc=new AccountPlace(0,accountId,lag,lng,device);
            autoInsertLocaltionUser(loc);
        }else{
            Log.d(TAG, "not get location user...");
        }
    }

    private void autoInsertLocaltionUser(final AccountPlace loc) {
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

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }
}
