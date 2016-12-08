package com.example.chinhnb.placemap.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.example.chinhnb.placemap.Adapter.DividerItemDecoration;
import com.example.chinhnb.placemap.Adapter.LocaltionAdapter;
import com.example.chinhnb.placemap.App.AppController;
import com.example.chinhnb.placemap.App.SQLiteHandler;
import com.example.chinhnb.placemap.Entity.Localtion;
import com.example.chinhnb.placemap.Event.RecyclerTouchListener;
import com.example.chinhnb.placemap.R;
import com.example.chinhnb.placemap.Utils.AppConfig;
import com.frosquivel.magicaltakephoto.MagicalTakePhoto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CHINHNB on 11/15/2016.
 */

public class AddNewActivity extends AppCompatActivity {

    private static final String TAG = AddNewActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private String lag,lng;

    static int TAKE_PIC =1;
    Uri outPutfileUri;
    ImageView imageView;

    private static final int ANY_INTEGER_0_TO_4000_FOR_QUALITY=72;
    MagicalTakePhoto magicalTakePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewlocaltion);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        Bundle b = getIntent().getExtras();

        if(b!=null)
        {
            lag =b.getString("Lag");
            lng =b.getString("Lng");
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        imageView = (ImageView) findViewById(R.id.txtAvatar);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCameraFront(view.getContext())) {
                    Calendar cal = Calendar.getInstance();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = new File(Environment.getExternalStorageDirectory(), (cal.getTimeInMillis() + ".jpg"));

                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        file.delete();
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    outPutfileUri = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutfileUri);
                    startActivityForResult(intent, TAKE_PIC);
                }else{
                    Toast.makeText(view.getContext(), "Thiết bị của bạn không hỗ trợ camera.", Toast.LENGTH_LONG).show();
                    //select device
                    magicalTakePhoto.selectedPicture("my_header_name");
                }

                //cach 2
                //magicalTakePhoto.takePhoto("my_photo_name");
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
                String uid=db.getUserDetails().get("uid");
                Localtion loc=new Localtion();
                loc.setAccountId(Integer.parseInt(uid));
                loc.setAvatar(outPutfileUri.toString());
                loc.setName(txtname.getText().toString());
                loc.setAddress(txtaddress.getText().toString());
                loc.setEmail(txtemail.getText().toString());
                loc.setPhone(txtphone.getText().toString());
                loc.setLag(lag);
                loc.setLng(lng);
                loc.setIscheck(true);

                prepareLocaltionData(loc);
            }
        });


        //cach 2 su dung thu vien
        magicalTakePhoto =  new MagicalTakePhoto(this,ANY_INTEGER_0_TO_4000_FOR_QUALITY);
    }

    public static boolean checkCameraFront(Context context) {
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data)
    {
        if (requestCode == TAKE_PIC && resultCode==RESULT_OK) {
            Toast.makeText(this, outPutfileUri.toString(), Toast.LENGTH_LONG).show();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), outPutfileUri);
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //cách 2
            //magicalTakePhoto.resultPhoto(requestCode, resultCode, data);
            //imageView.setImageBitmap(magicalTakePhoto.getMyPhoto());
        }
    }

    private void prepareLocaltionData(final Localtion loc) {
        Log.d(TAG, "Data: " + loc.getAvatar());
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
                params.put("AccountId", String.valueOf(loc.getAccountId()));
                params.put("Name", loc.getName());
                params.put("Address", loc.getAddress());
                params.put("Email", loc.getEmail());
                params.put("Phone", loc.getPhone());
                params.put("Avatar", loc.getAvatar());
                params.put("Lag", loc.getLag());
                params.put("Lng", loc.getLng());
                params.put("IsCheck", String.valueOf(loc.getIsCheck()));

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
