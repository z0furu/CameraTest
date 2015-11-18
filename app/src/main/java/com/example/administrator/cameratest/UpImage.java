package com.example.administrator.cameratest;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class UpImage extends AppCompatActivity {

    private static final String TAG = UpImage.class.getSimpleName();

    private ImageView imageView;
    private Button btnOk;

    String account;

    private String filePath = null;

    private Uri uri;




    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_up_image);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        imageView = (ImageView) findViewById(R.id.Up_imageview);
        btnOk = (Button) findViewById(R.id.Up_btnupimage);

        Intent i = getIntent();
        filePath = i.getStringExtra("filePath");
        boolean isImage = i.getBooleanExtra("isImage", false);
        boolean isPhote = i.getBooleanExtra("isPhote", false);

        if (filePath != null) {
            if (isImage==true){
                Logger.d("相機");
                previewMedia(isImage);
            }else {
                Logger.d("相簿");
                prePhote(isPhote);
            }
        } else {
            Toast.makeText(getApplicationContext(), "檔案遺失", Toast.LENGTH_SHORT).show();
        }

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account = "z0furu";
                upImage(account);
            }
        });
    }

    private void previewMedia(boolean isImage) {
        if (isImage) {
            imageView.setVisibility(View.VISIBLE);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Log.e("Image", filePath.toString());
            Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
            imageView.setImageBitmap(bitmap);
        }
    }

    private void prePhote(boolean isPhote) {
        if (isPhote) {

            imageView.setVisibility(View.VISIBLE);
            try {
                BitmapFactory.Options mOptions = new BitmapFactory.Options();

                mOptions.inSampleSize = 10;
                ContentResolver cr = this.getContentResolver();
                uri = Uri.parse(filePath);
                Logger.d("Phote", uri.getAuthority());

                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri), null, mOptions);
                imageView.setImageBitmap(bitmap);
                if (Build.VERSION.SDK_INT < 19) {
                    phote18();
                    Logger.d("4.4以下");
                } else {
                    Logger.d("4.4以上");
                    phote18();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void phote19() {
        Uri selectedImage = uri;
        String wholeID = DocumentsContract.getDocumentId(selectedImage);
        Logger.d(wholeID);
        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];
        Logger.d(id);

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{id}, null);

        filePath = "";

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);

        }
        cursor.close();

    }

    private void phote18() {
        Uri selectedImage = uri;
        Log.e("phote18", selectedImage.toString());
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver()
                .query(selectedImage, filePathColumn, null,
                        null, null);
        cursor.moveToFirst();

        int columnIndex = cursor
                .getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);


        System.out.println("圖片=" + filePath);
        cursor.close();
        if (filePath==null){
            phote19();
        }
    }


    private void upImage(final String account) {
        showpDialog();
        pDialog.setMessage("上傳中");
        File file = new File(filePath);
        Logger.d(file.getAbsolutePath());
        String tag = "uploadImage";
        Map<String, String> params = new HashMap<>();
        params.put("account", account);


        MultipartRequest multipartRequest = new MultipartRequest(Config.Up_Image, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Logger.e(error.toString());
                upImage(account);
            }
        }, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Logger.json(response);
                hidepDialog();
                Toast.makeText(UpImage.this, "上傳成功", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UpImage.this, MainActivity.class));
                finish();
            }
        }, tag, file, params);
        Volley.newRequestQueue(this).add(multipartRequest);
    }

    private void showpDialog(){
        if (!pDialog.isShowing()){
            pDialog.show();
        }
    }
    private void hidepDialog(){
        if (pDialog.isShowing()){
            pDialog.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}