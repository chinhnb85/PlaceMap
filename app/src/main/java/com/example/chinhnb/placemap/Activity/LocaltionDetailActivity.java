package com.example.chinhnb.placemap.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class LocaltionDetailActivity extends AppCompatActivity {

    private static final String TAG = LocaltionDetailActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    Localtion localtion;
    private int id,accountId;
    private Double lag,lng;
    TextView textViewName,textViewAddress,textViewEmail,textViewPhone,textViewLagLng,txtChecked;
    ImageView imageViewAvatar;
    Button btnViewMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detaillocaltion);

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
        txtChecked = (TextView) findViewById(R.id.txtChecked);

        localtion=new Localtion(
                id,
                accountId,
                lag,
                lng
        );
        prepareLocaltionData(localtion);

        btnViewMap = (Button) findViewById(R.id.btnViewMap);
        btnViewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LocaltionDetailActivity.this, MapActivity.class);
                intent.putExtra("Lag", localtion.getLag());
                intent.putExtra("Lng", localtion.getLng());
                startActivity(intent);
            }
        });
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
                                obj.getDouble("Lng"),
                                obj.getString("Code"),
                                obj.getString("RepresentActive"),
                                0,
                                obj.getInt("MinCheckin"),
                                obj.getBoolean("StatusEdit")
                        );

                        Uri uri=Uri.parse(localtion.getAvatar());
                        Context context=imageViewAvatar.getContext();
                        Glide.with(context).load(uri)
                                .crossFade()
                                .thumbnail(0.5f)
                                //.bitmapTransform(new CircleTransform(context))
                                .placeholder(R.drawable.ic_loading)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageViewAvatar);
                        textViewName.setText("Địa điểm: "+localtion.getName());
                        textViewAddress.setText("Địa chỉ: "+localtion.getAddress());
                        textViewEmail.setText("Email: "+(localtion.getEmail().toLowerCase().equals("null")?"":localtion.getEmail()));
                        textViewPhone.setText("Điện thoại: "+(localtion.getPhone().toLowerCase().equals("null")?"":localtion.getPhone()));
                        textViewLagLng.setText("Vị trí: "+localtion.getLag()+" , "+localtion.getLng());

                        if(localtion.getIsCheck()){
                            txtChecked.setText("Trạng thái: Đã checkin");
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
                        getResources().getString(R.string.not_network), Toast.LENGTH_LONG).show();
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

    private void showAlert(final String message,final String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Thông báo")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(type=="sucsses"){
                            Intent intent=new Intent();
                            intent.putExtra("message",message);
                            setResult(2,intent);
                            finish();
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
