package com.example.chinhnb.placemap.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.chinhnb.placemap.App.AppController;
import com.example.chinhnb.placemap.App.SQLiteHandler;
import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.Other.CircleTransform;
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
    Localtion localtion;
    private int id,accountId;
    private Double lag,lng;
    TextView textViewName,textViewAddress,textViewEmail,textViewPhone,textViewLagLng;
    ImageView imageViewAvatar;
    Button btnChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkedlocaltion);

        Bundle b = getIntent().getExtras();

        if(b!=null)
        {
            id =b.getInt("Id");
            accountId =b.getInt("AccountId");
            lag =b.getDouble("Lag");
            lng =b.getDouble("Lng");
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        imageViewAvatar = (ImageView) findViewById(R.id.txtAvatar);
        textViewName = (TextView) findViewById(R.id.txtName);
        textViewAddress = (TextView) findViewById(R.id.txtAddress);
        textViewEmail = (TextView) findViewById(R.id.txtEmail);
        textViewPhone = (TextView) findViewById(R.id.txtPhone);
        textViewLagLng = (TextView) findViewById(R.id.txtLagLng);

        localtion=new Localtion(
                id,
                accountId,
                lag,
                lng
        );
        prepareLocaltionData(localtion);

        btnChecked = (Button) findViewById(R.id.btnChecked);
        btnChecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareCheckedLocaltion(localtion);
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
                        setResult(2,intent);
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
                params.put("AccountId", String.valueOf(loc.getAccountId()));
                params.put("Id", String.valueOf(loc.getId()));
                params.put("Lag", loc.getLag().toString());
                params.put("Lng", loc.getLng().toString());

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
                        JSONObject obj = jObj.getJSONObject("Data");
                        String ischeck=obj.getString("IsCheck");
                        if(ischeck==null){
                            ischeck="false";
                        }
                        Localtion localtion=new Localtion(
                                obj.getInt("Id"),
                                obj.getInt("AccountId"),
                                Boolean.valueOf(ischeck),
                                obj.getString("Name"),
                                obj.getString("Address"),
                                obj.getString("Email"),
                                obj.getString("Phone"),
                                obj.getString("Avatar"),
                                obj.getDouble("Lag"),
                                obj.getDouble("Lng")
                        );

                        Uri uri=Uri.parse(localtion.getAvatar());
                        Context context=imageViewAvatar.getContext();
                        Glide.with(context).load(uri)
                                .crossFade()
                                .thumbnail(0.5f)
                                .bitmapTransform(new CircleTransform(context))
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageViewAvatar);
                        textViewName.setText(localtion.getName());
                        textViewAddress.setText(localtion.getAddress());
                        textViewEmail.setText(localtion.getEmail());
                        textViewPhone.setText(localtion.getPhone());
                        textViewLagLng.setText(localtion.getLag()+" , "+localtion.getLng());
                        if(localtion.getIsCheck()){
                            btnChecked.setText("Đã checked");
                            btnChecked.setTextColor(getResources().getColor(R.color.bg_main));
                            btnChecked.setOnClickListener(null);
                        }
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
                params.put("Id", String.valueOf(loc.getId()));

                Log.d(TAG, "params: " + params.toString());

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
