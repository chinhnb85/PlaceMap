package com.example.chinhnb.placemap.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
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
import com.example.chinhnb.placemap.Utils.Const;
import com.example.chinhnb.placemap.Utils.Utils;

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
    Context context;
    Uri mFileUri;
    private String avatar;

    private static final int TAKE_PICTURE =1;
    private static final int UPLOAD_PICTURE=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkedlocaltion);

        context=this;

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
                avatar="";
                //open camera checkin
                selectCheckinLoc();
            }
        });
    }

    private void selectCheckinLoc() {

        final CharSequence[] items = { "Checkin có ảnh", "Checkin không ảnh" };

        TextView title = new TextView(context);
        title.setText("Check in địa điểm");
        title.setBackgroundColor(Color.GRAY);
        title.setPadding(10, 15, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                CheckedActivity.this);

        builder.setCustomTitle(title);

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Checkin có ảnh")) {
                    if(checkCameraFront(context)) {
                        String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        if(!hasPermissions(CheckedActivity.this, PERMISSIONS)){
                            ActivityCompat.requestPermissions(CheckedActivity.this, PERMISSIONS, Const.PERMISSION_ALL);
                        }else {
                            captureImage();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),
                                "Thiết bị của bạn không hỗ trợ camera.", Toast.LENGTH_LONG).show();
                    }
                } else if (items[item].equals("Checkin không ảnh")) {
                    dialog.dismiss();
                    //kiem tra vị trí
                    prepareCheckedLocaltion(localtion);
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", mFileUri);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void captureImage() {
        mFileUri = Utils.getOutputMediaFileUri(Utils.MEDIA_TYPE_IMAGE);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    private boolean checkCameraFront(Context context) {
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case Const.PERMISSION_ALL:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    captureImage();
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
                    previewCapturedImage();
                }
                break;
            case UPLOAD_PICTURE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        try {
                            avatar = data.getStringExtra("urlAvatar");
                            String uri=AppConfig.URL_ROOT+avatar;
                            //kiem tra vị trí
                            prepareCheckedLocaltion(localtion);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    private void previewCapturedImage() {
        if(mFileUri!=null) {
            try {

                Intent intent = new Intent(CheckedActivity.this, UploadActivity.class);
                intent.putExtra("filePath", mFileUri.getPath());
                startActivityForResult(intent,UPLOAD_PICTURE);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
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
                        showAlert(jObj.getString("message"),"sucsses");
                    } else {
                        showAlert(jObj.getString("message"),"error");
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
                params.put("AccountId", String.valueOf(loc.getAccountId()));
                params.put("Id", String.valueOf(loc.getId()));
                params.put("Lag", loc.getLag().toString());
                params.put("Lng", loc.getLng().toString());
                params.put("ImageCheckin", avatar);

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
                            btnChecked.setText("Đã checkin");
                            btnChecked.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            btnChecked.setTextColor(getResources().getColor(R.color.white));
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
