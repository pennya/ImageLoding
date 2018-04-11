package com.example.kim.imageloading;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<ImageItem> imageItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();

        // 화면 회전시 데이터 가져오기
        if(savedInstanceState != null) {
            imageItems = (ArrayList<ImageItem>)savedInstanceState.getSerializable("imageItem");
        } else {
            loadHtmlBody();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 화면 전환 시 저장
        outState.putSerializable("imageItem", imageItems);
    }

    @Override
    protected void onDestroy() {
        // 화면 회전시 실행
        super.onDestroy();
    }

    private void initLayout() {
        recyclerView = (RecyclerView) findViewById(R.id.rv_main_activity);
    }

    private void loadHtmlBody() {
        try {
            String htmlBody = new DownloadPage().execute("http://www.gettyimagesgallery.com/collections/archive/slim-aarons.aspx").get();
            imageItems = imgTagParser(htmlBody);
            for(ImageItem item : imageItems) {
                Log.d("TEST", item.getSrc());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected class DownloadPage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder responseStr = new StringBuilder();
            try {
                for (String url : urls) {
                    URL u = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection)u.openConnection();
                    conn.connect();
                    BufferedReader bis = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while(bis.readLine() != null) {
                        responseStr.append(bis.readLine());
                    }
                }
            } catch (Exception e) {

            }
            return responseStr.toString();
        }
    }

    public static ArrayList<ImageItem> imgTagParser(String str) {
        // jsoup 또는 정규식 사용
        Pattern pattern = Pattern
                .compile("<img[^>]*src=[\"']?([^>\"']+)[\"']?[^>]*>");

        ArrayList<ImageItem> result = new ArrayList<>();
        Matcher matcher = pattern.matcher(str);
        // find로 찾아내고
        while (matcher.find()) {
            // group으로 가져온다
            String src = matcher.group(1);
            if(src.contains("/Images/Thumbnails/"))
                result.add(ImageItem.builder().setSrc(src));
        }
        return result;
    }
}
