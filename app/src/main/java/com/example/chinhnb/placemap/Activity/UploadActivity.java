package com.example.chinhnb.placemap.Activity;

import com.example.chinhnb.placemap.Entity.AndroidMultiPartEntity;
import com.example.chinhnb.placemap.Entity.AndroidMultiPartEntity.ProgressListener;
import com.example.chinhnb.placemap.R;
import com.example.chinhnb.placemap.Utils.AppConfig;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
/**
 * Created by CHINHNB on 12/11/2016.
 */

public class UploadActivity extends Activity {
    // LogCat tag
    private static final String TAG = UploadActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private String filePath = null;
    private TextView txtPercentage;
    private ImageView imgPreview;
    private VideoView vidPreview;
    private Button btnUpload;
    long totalSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        vidPreview = (VideoView) findViewById(R.id.videoPreview);

        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");

        if (filePath != null) {
            previewMedia(true);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Lỗi: Không tìm thấy file ảnh.", Toast.LENGTH_LONG).show();
        }

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new UploadFileToServer().execute();
            }
        });

    }

    private void previewMedia(boolean isImage) {
        // Checking whether captured media is image or video
        if (isImage) {
            imgPreview.setVisibility(View.VISIBLE);
            vidPreview.setVisibility(View.GONE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            imgPreview.setImageBitmap(bitmap);
        } else {
            imgPreview.setVisibility(View.GONE);
            vidPreview.setVisibility(View.VISIBLE);
            vidPreview.setVideoPath(filePath);
            // start playing
            vidPreview.start();
        }
    }

    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(AppConfig.URL_UPLOAD_IMAGE);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(filePath);

                // Adding file data to http body
                entity.addPart("file", new FileBody(sourceFile));

                // Extra parameters if you want to pass to server
                entity.addPart("folder", new StringBody("Localtion"));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = "{\"status\":\"true\",\"message\":\"Upload ảnh thành công.\",\"data\":\""+EntityUtils.toString(r_entity)+"\"}";
                } else {
                    responseString = "{\"status\":\"false\",\"message\": \"Đã có lỗi, mã lỗi: "+ statusCode+"\",\"data\": \"null\"}";
                }
            } catch (ClientProtocolException e) {
                responseString = "{\"status\":\"false\",\"message\": \"Đã có lỗi, mã lỗi: "+ e.toString()+"\",\"data\": \"null\"}";
            } catch (IOException e) {
                responseString = "{\"status\":\"false\",\"message\": \"Đã có lỗi, mã lỗi: "+ e.toString()+"\",\"data\": \"null\"}";
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);

            try {
                JSONObject jObj = new JSONObject(result);
                boolean status=jObj.getBoolean("status");
                String message=jObj.getString("message");
                String data=jObj.getString("data");
                if(status) {
                    showAlert(message, data, "sucssec");
                }else{
                    showAlert(message, data, "error");
                }
            } catch (JSONException e) {
                showAlert("Lỗi paser json: "+e.toString(), "null", "error");
            }

            super.onPostExecute(result);
        }
    }

    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message,final String url,final String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Thông báo")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                        if(type=="sucssec") {
                            Intent intent = new Intent();
                            intent.putExtra("urlAvatar", url);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
