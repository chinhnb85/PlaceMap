package com.example.chinhnb.placemap.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.chinhnb.placemap.App.AppController;
import com.example.chinhnb.placemap.App.SQLiteHandler;
import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.R;
import com.example.chinhnb.placemap.Utils.AppConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CHINHNB on 11/15/2016.
 */

public class CheckedActivity extends AppCompatActivity {

    private static final String TAG = CheckedActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private Localtion loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkedlocaltion);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        String uid=db.getUserDetails().get("uid");
        loc=new Localtion();
        prepareLocaltionData(loc);

        Button btnAddNew = (Button) findViewById(R.id.btnAddNew);
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareCheckedLocaltion(loc);
            }
        });
    }

    private void prepareCheckedLocaltion(final Localtion loc) {

        // Tag used to cancel the request
        String tag_string_req = "req_checked";

        pDialog.setMessage("Đang xử lý dữ liệu...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CHECK_LOCALTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean status = jObj.getBoolean("status");
                    if (status) {
                        String msg = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
                                msg, Toast.LENGTH_LONG).show();
                        Intent intent=new Intent();
                        intent.putExtra("message",msg);
                        setResult(RESULT_OK,intent);
                        finish();
                    } else {
                        String errorMsg = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                params.put("AccountId", loc.getTitle());
                params.put("Name", loc.getGenre());
                params.put("Address", loc.getTitle());
                params.put("Email", loc.getGenre());
                params.put("Phone", loc.getTitle());
                params.put("Avatar", loc.getGenre());

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void prepareLocaltionData(final Localtion loc) {

        // Tag used to cancel the request
        String tag_string_req = "req_checked";

        pDialog.setMessage("Đang tải dữ liệu...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_GET_LOCALTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean status = jObj.getBoolean("status");
                    if (status) {
                        //view

                    } else {
                        String errorMsg = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                params.put("AccountId", loc.getTitle());
                params.put("Name", loc.getGenre());
                params.put("Address", loc.getTitle());
                params.put("Email", loc.getGenre());
                params.put("Phone", loc.getTitle());
                params.put("Avatar", loc.getGenre());

                return params;
            }

        };

        // Adding request to request queue
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
