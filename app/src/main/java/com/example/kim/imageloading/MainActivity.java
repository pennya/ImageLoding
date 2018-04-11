package com.example.kim.imageloading;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 999;
    private final int GRID_SPAN_COUNT = 2;
    public static final String WEBSITE_DOMAIN = "http://www.gettyimagesgallery.com";
    public static final String WEBSITE_SUB = "/collections/archive/slim-aarons.aspx";

    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private Button clearDiskCache;
    private ArrayList<ImageItem> imageItems = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkPermissions() && Build.VERSION.SDK_INT >= 23) {
            requestPermissions();
        } else {
            initLayout();
            if(savedInstanceState != null) { // 화면 회전 시 다시 표시
                imageItems = (ArrayList<ImageItem>)savedInstanceState.getSerializable("imageItem");
                displayImage(imageItems);
            } else {
                loadImageListFromWebsite(); // 웹사이트에서 이미지 리스트 가져오기
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("imageItem", imageItems); // 화면 전환 시 저장
    }

    @Override
    protected void onDestroy() {
        recyclerView.setAdapter(null);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_activity_clear_cache:
                adapter.clearCache();   // 메모리, 디스크 캐시 삭제
                break;
        }
    }

    private void initLayout() {
        recyclerView = (RecyclerView) findViewById(R.id.rv_main_activity);
        clearDiskCache = (Button) findViewById(R.id.btn_main_activity_clear_cache);
        clearDiskCache.setOnClickListener(this);

        RecyclerView.LayoutManager manager = new GridLayoutManager(this, GRID_SPAN_COUNT);
        adapter = new RecyclerViewAdapter(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.image_list_loading));
        progressDialog.setCancelable(false);
    }

    private void loadImageListFromWebsite() {
        progressDialog.show();
        new ImageListLoader().execute(WEBSITE_DOMAIN + WEBSITE_SUB);
    }

    private void displayImage(ArrayList<ImageItem> items) {
        adapter.setItems(items);
        adapter.notifyDataSetChanged();
    }

    protected class ImageListLoader extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                for (String url : urls) {
                    Document doc = Jsoup.connect(url).get();
                    Elements images = doc.select("img[class=picture]");
                    for(Element element : images) {
                        Attributes attributes = element.attributes();
                        String src = attributes.get("src");
                        imageItems.add(ImageItem.builder().setUrl(WEBSITE_DOMAIN + src));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();

            if(!result) {
                Toast.makeText(MainActivity.this, "Image lists loading fail.", Toast.LENGTH_SHORT).show();
                return;
            }

            displayImage(imageItems);

            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            progressDialog.dismiss();
            super.onCancelled();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_PERMISSIONS_REQUEST_CODE:
                if( grantResults.length > 0) {
                    boolean fileReadPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean fileWritePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(fileReadPermission && fileWritePermission) {
                        initLayout();
                        loadImageListFromWebsite();
                        displayImage(imageItems);
                    }

                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.info))
                            .setMessage(getString(R.string.need_read_storage_permission))
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package",
                                            BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                            .create()
                            .show();
                }
        }
    }

    private void startPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE } , REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) +
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProviceRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if( shouldProviceRationale ) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.info))
                    .setMessage(getString(R.string.need_read_storage_permission))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startPermissionRequest();
                        }
                    })
                    .create()
                    .show();
        } else {
            startPermissionRequest();
        }
    }
}
