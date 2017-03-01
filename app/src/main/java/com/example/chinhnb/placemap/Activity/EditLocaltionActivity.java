package com.example.chinhnb.placemap.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.R;
import com.example.chinhnb.placemap.Services.GPSTracker;
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

public class EditLocaltionActivity extends AppCompatActivity {

    private static final String TAG = EditLocaltionActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private Double lag,lng;
    private int accountId,localtionId;
    private String avatar,name,address,phone,code;

    private static final int TAKE_PICTURE =1;
    private static final int SELECT_PICTURE=2;
    private static final int UPLOAD_PICTURE=3;

    Uri mFileUri;
    ImageView imageView;

    Context context;
    Bitmap thumbnail_r;
    GPSTracker gps;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editlocaltion);

        context=this;
        avatar = "assets/img/avatars/no-avatar.gif";

        Bundle b = getIntent().getExtras();
        if(b!=null)
        {
            lag =b.getDouble("Lag");
            lng =b.getDouble("Lng");
            accountId =b.getInt("AccountId");
            localtionId =b.getInt("LocaltionId");
            name =b.getString("Name");
            address =b.getString("Address");
            phone =b.getString("Phone");
            code =b.getString("Code");
            avatar=b.getString("Image");
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        imageView = (ImageView) findViewById(R.id.txtAvatar);
        Uri uri=Uri.parse(avatar);
        Glide.with(context).load(uri)
                .crossFade()
                .thumbnail(0.5f)
                //.bitmapTransform(new CircleTransform(context))
                .placeholder(R.drawable.ic_loading)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        final TextView txtlaglng = (TextView) findViewById(R.id.txtLagLng);
        txtlaglng.setText("Vị trí hiện tại: "+lag+" , "+lng);
        final EditText txtname = (EditText) findViewById(R.id.txtName);
        txtname.setText(name);
        final EditText txtaddress = (EditText) findViewById(R.id.txtAddress);
        txtaddress.setText(address);
        final EditText txtcode = (EditText) findViewById(R.id.txtCode);
        txtcode.setText(code);
        final EditText txtphone = (EditText) findViewById(R.id.txtPhone);
        txtphone.setText(phone);

        Button btnEdit = (Button) findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                gps = new GPSTracker(context);
                // check if GPS enabled
                if(gps.canGetLocation()){
                    lag = gps.getLatitude();
                    lng = gps.getLongitude();
                }
                if(txtname.getText().toString().length()==0){
                    txtname.setError("Nhập tên vị trí");
                }else if(txtaddress.getText().toString().length()==0) {
                    txtaddress.setError("Nhập địa chỉ vị trí");
                }
                else{
                    Localtion localtion = new Localtion(
                            localtionId,
                            accountId,
                            true,
                            txtname.getText().toString(),
                            txtaddress.getText().toString(),
                            "",
                            txtphone.getText().toString(),
                            avatar,
                            lag,
                            lng,
                            txtcode.getText().toString(),
                            "",
                            0,
                            30,
                            true
                    );

                    prepareLocaltionData(localtion);
                }
            }
        });
    }


    private boolean checkCameraFront(Context context) {
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return true;
        } else {
            return false;
        }
    }

    private void selectImage() {

        final CharSequence[] items = { "Camera", "Chọn từ điện thoại", "Hủy" };

        TextView title = new TextView(context);
        title.setText("Chọn ảnh");
        title.setBackgroundColor(Color.GRAY);
        title.setPadding(10, 15, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                EditLocaltionActivity.this);

        builder.setCustomTitle(title);

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Camera")) {
                    if(checkCameraFront(context)) {
                        String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        if(!hasPermissions(EditLocaltionActivity.this, PERMISSIONS)){
                            ActivityCompat.requestPermissions(EditLocaltionActivity.this, PERMISSIONS, Const.PERMISSION_ALL);
                        }else {
                            captureImage();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),
                                "Thiết bị của bạn không hỗ trợ camera.", Toast.LENGTH_LONG).show();
                    }
                } else if (items[item].equals("Chọn từ điện thoại")) {
                    int permissionCheck = ContextCompat.checkSelfPermission(EditLocaltionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                EditLocaltionActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Const.READ_EXTERNAL_STORAGE);
                    } else {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_PICK);
                        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"),SELECT_PICTURE);
                    }
                } else if (items[item].equals("Hủy")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", mFileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // get the file url
        mFileUri = savedInstanceState.getParcelable("file_uri");
    }

   @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       super.onRequestPermissionsResult(requestCode, permissions, grantResults);

       switch (requestCode) {

            case Const.READ_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);
                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"),SELECT_PICTURE);
                }
                break;
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
            case SELECT_PICTURE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        try {
                            Uri selectedImage = data.getData();
                            String[] filePath = { MediaStore.Images.Media.DATA };
                            Cursor c = context.getContentResolver().query(
                                    selectedImage, filePath, null, null, null);
                            if(c!=null) {
                                c.moveToFirst();
                                int columnIndex = c.getColumnIndex(filePath[0]);
                                String picturePath = c.getString(columnIndex);
                                c.close();

                                previewSelectedImage(picturePath);

                            }else{
                                Toast.makeText(getApplicationContext(),"Không tìm thấy file ảnh. Thử lại ảnh khác!", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
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
                            Glide.with(context).load(uri)
                                    .crossFade()
                                    .thumbnail(0.5f)
                                    //.bitmapTransform(new CircleTransform(context))
                                    .placeholder(R.drawable.ic_loading)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(imageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    private void previewSelectedImage(String picturePath) {
        if(picturePath!=null) {
            try {

                Intent intent = new Intent(EditLocaltionActivity.this, UploadActivity.class);
                intent.putExtra("filePath", picturePath);
                startActivityForResult(intent, UPLOAD_PICTURE);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void previewCapturedImage() {
        if(mFileUri!=null) {
            try {

                Intent intent = new Intent(EditLocaltionActivity.this, UploadActivity.class);
                intent.putExtra("filePath", mFileUri.getPath());
                startActivityForResult(intent,UPLOAD_PICTURE);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareLocaltionData(final Localtion loc) {
        Log.d(TAG, "Update: " + loc.getAvatar()+" ; "+loc.getLag()+" ; "+loc.getLng());
        // Tag used to cancel the request
        String tag_string_req = "req_edit";

        pDialog.setMessage("Đang xử lý dữ liệu...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_EDIT_LOCALTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response edit: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean status = jObj.getBoolean("status");
                    if (status) {
                        String msg = jObj.getString("message");
                        /*Toast.makeText(getApplicationContext(),
                                msg, Toast.LENGTH_LONG).show();*/
                        Intent intent=new Intent();
                        intent.putExtra("message",msg);
                        setResult(RESULT_OK,intent);
                        finish();
                    } else {
                        String errorMsg = jObj.getString("message");
                        showAlert(errorMsg);
                        /*Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();*/
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
                params.put("AccountId", String.valueOf(loc.getAccountId()));
                params.put("Name", loc.getName());
                params.put("Address", loc.getAddress());
                params.put("Email", loc.getEmail());
                params.put("Phone", loc.getPhone());
                params.put("Avatar", loc.getAvatar());
                params.put("Lag", loc.getLag().toString());
                params.put("Lng", loc.getLng().toString());
                params.put("IsCheck", String.valueOf(loc.getIsCheck()));
                params.put("Code", loc.getCode());
                params.put("StatusEdit", "true");

                Log.d(TAG, "params: " +params);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void showAlert(final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Thông báo")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
