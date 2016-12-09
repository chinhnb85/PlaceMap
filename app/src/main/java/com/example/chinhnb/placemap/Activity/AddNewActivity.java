package com.example.chinhnb.placemap.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.example.chinhnb.placemap.App.AppController;
import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.R;
import com.example.chinhnb.placemap.Utils.AppConfig;
import com.example.chinhnb.placemap.Utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by CHINHNB on 11/15/2016.
 */

public class AddNewActivity extends AppCompatActivity {

    private static final String TAG = AddNewActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private Double lag,lng;
    private int accountId;

    private static final int TAKE_PICTURE =1;
    private static final int SELECT_PICTURE=2;
    Uri fileUri;
    ImageView imageView;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewlocaltion);

        context=this;
        Bundle b = getIntent().getExtras();
        if(b!=null)
        {
            lag =b.getDouble("Lag");
            lng =b.getDouble("Lng");
            accountId =b.getInt("AccountId");
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        imageView = (ImageView) findViewById(R.id.txtAvatar);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectImage();
            }
        });

        final EditText txtname = (EditText) findViewById(R.id.txtName);
        final EditText txtaddress = (EditText) findViewById(R.id.txtAddress);
        final EditText txtemail = (EditText) findViewById(R.id.txtEmail);
        final EditText txtphone = (EditText) findViewById(R.id.txtPhone);

        Button btnAddNew = (Button) findViewById(R.id.btnAddNew);
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Localtion localtion=new Localtion(
                        0,
                        accountId,
                        true,
                        txtname.getText().toString(),
                        txtaddress.getText().toString(),
                        txtemail.getText().toString(),
                        txtphone.getText().toString(),
                        "",
                        lag,
                        lng
                );

                prepareLocaltionData(localtion);
            }
        });
    }

    public static boolean checkCameraFront(Context context) {
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
        title.setBackgroundColor(Color.BLACK);
        title.setPadding(10, 15, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                AddNewActivity.this);

        builder.setCustomTitle(title);

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Camera")) {

                    Intent intents = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    fileUri = Utils.getOutputMediaFileUri(Utils.MEDIA_TYPE_IMAGE);

                    intents.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                    // start the image capture Intent
                    startActivityForResult(intents, TAKE_PICTURE);

                } else if (items[item].equals("Chọn từ điện thoại")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Chọn ảnh"),
                            SELECT_PICTURE);
                } else if (items[item].equals("Hủy")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SELECT_PICTURE:
                Bitmap bitmap = null;
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        try {
                            Uri selectedImage = data.getData();
                            String[] filePath = { MediaStore.Images.Media.DATA };
                            Cursor c = context.getContentResolver().query(
                                    selectedImage, filePath, null, null, null);
                            c.moveToFirst();
                            int columnIndex = c.getColumnIndex(filePath[0]);
                            String picturePath = c.getString(columnIndex);
                            c.close();
                            imageView.setVisibility(View.VISIBLE);
                            Bitmap thumbnail = Utils.decodeSampledBitmapFromResource(picturePath, 500, 500);
                            // rotated
                            Bitmap thumbnail_r = Utils.imageOreintationValidator(thumbnail, picturePath);
                            imageView.setImageBitmap(thumbnail_r);
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
        }
    }

    private void previewCapturedImage() {
        try {
            imageView.setVisibility(View.VISIBLE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();
            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500,false);

            // rotated
            Bitmap thumbnail_r = Utils.imageOreintationValidator(resizedBitmap, fileUri.getPath());

            imageView.setImageBitmap(thumbnail_r);

            Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_LONG)
                    .show();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void prepareLocaltionData(final Localtion loc) {
        Log.d(TAG, "Data: " + loc.getAvatar()+" ; "+loc.getLag()+" ; "+loc.getLng());
        // Tag used to cancel the request
        String tag_string_req = "req_addnew";

        pDialog.setMessage("Đang thêm dữ liệu...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_ADD_LOCALTION, new Response.Listener<String>() {

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
                params.put("Name", loc.getName());
                params.put("Address", loc.getAddress());
                params.put("Email", loc.getEmail());
                params.put("Phone", loc.getPhone());
                //params.put("Avatar", loc.getAvatar());
                params.put("Lag", loc.getLag().toString());
                params.put("Lng", loc.getLng().toString());
                params.put("IsCheck", String.valueOf(loc.getIsCheck()));
                params.put("Status", "true");

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

}
