package com.example.kim.imageloading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kim on 2018. 4. 12..
 */

public class ImageLoader {

    public static final int MAX_REDIRECT_COUNT = 3;
    public static final int HTTP_CONNECT_TIMEOUT = 3 * 1000;
    public static final int HTTP_READ_TIMEOUT = 10 * 1000;
    public static final int BITMAP_FROM_MEMORY = 1;
    public static final int BITMAP_FROM_DISK = 2;
    public static final int BITMAP_FROM_URL = 3;
    private final int PLACEHOLDER = R.drawable.loading;
    private ExecutorService executorService;
    private Map<ImageView, String> imageReused = Collections.synchronizedMap(new HashMap<ImageView, String>());
    private DiskCache diskCache;
    private MemoryCache memoryCache;
    private Handler handler = new Handler();

    public ImageLoader(Context context) {
        executorService = Executors.newFixedThreadPool(5);
        diskCache = new DiskCache(context);
        memoryCache = new MemoryCache();
    }

    public void displayImage(ImageItem imageItem, ImageView imageView, TextView type) {

        // 뷰홀더에 어떤 이미지가 저장될 것 인지 저장
        // 현재 보여질 이미지가 아니라면 화면에 표시하지 않는다
        // 스크롤 시 이전 이미지가 보이는 것을 방지하기 위함
        if(imageReused.containsKey(imageView)) {
            imageReused.put(imageView, imageItem.getUrl());
        } else {
            imageReused.remove(imageView);
            imageReused.put(imageView, imageItem.getUrl());
        }

        // 메모리 캐시에서 비트맵 가져오기
        Bitmap bitmap = memoryCache.getFromMemoryCache(imageItem.getUrl());
        if(bitmap != null) {
            imageItem.setBitmap(bitmap);
            imageItem.setType(BITMAP_FROM_MEMORY);
            ImageDisplayTask imageDisplayTask
                    = new ImageDisplayTask(imageItem, imageView, type);
            handler.post(imageDisplayTask);
        } else {
            // 이미지를 가져오고 화면에 보여주는 Task
            taskQueue(imageItem, imageView, type);
            // 이미지가 로드되기전까지 로딩중 이미지 표시
            imageView.setImageResource(PLACEHOLDER);
        }
    }

    private void taskQueue(ImageItem imageItem, ImageView imageView, TextView type) {
        executorService.submit(new ImageLoaderTask(imageItem, imageView, type));
    }

    private HttpURLConnection connect(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
            conn.setReadTimeout(HTTP_READ_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
            return conn;
        }
        return conn;
    }

    private ImageItem getBitmap(ImageItem imageItem) {
        String url = imageItem.getUrl();

        // 디스크 캐시에서 이미지 가져오기
        File file = diskCache.getFile(url);
        if(file == null)
            return null;

        Bitmap bitmap = decodeFile(file);
        if(bitmap != null) {
            imageItem.setBitmap(bitmap);
            imageItem.setType(BITMAP_FROM_DISK);
            return imageItem;
        }

        // 웹사이트에서 이미지 다운로드
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = connect(imageUrl);

            int redirectCount = 0;
            while(conn.getResponseCode() / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
                conn = connect(imageUrl);
                redirectCount++;
            }

            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            copyData(bis, bos);
            bos.close();
            bis.close();
            conn.disconnect();

            bitmap = decodeFile(file);
            imageItem.setBitmap(bitmap);
            imageItem.setType(BITMAP_FROM_URL);
            return imageItem;
        } catch (Throwable ex){
            ex.printStackTrace();
            return null;
        }
    }

    private Bitmap decodeFile(File file){
        try {
            if(!file.canRead()) {
                return null;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            FileInputStream stream = new FileInputStream(file);

            // 해상도 1/2로 감소
            int scaleFactor = 2;
            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleFactor;
            options.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();
            return bitmap;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearCache() {
        memoryCache.clear();
        diskCache.clear();
    }

    /**
     * 메모리 캐시, 디스크 캐시, 웹사이트 다운로드를 통해 이미지를 저장 및 로드하는 Task
     */
    class ImageLoaderTask implements Runnable {

        private ImageItem imageItem;
        private ImageView imageView;
        private TextView textView;

        public ImageLoaderTask(ImageItem imageItem, ImageView imageView, TextView textView) {
            this.imageItem = imageItem;
            this.imageView = imageView;
            this.textView = textView;
        }

        @Override
        public void run() {
            ImageItem item = getBitmap(imageItem);
            if(item == null)
                return;

            memoryCache.putIntoMemoryCache(imageItem.getUrl(), imageItem.getBitmap());
            ImageDisplayTask imageDisplayTask
                    = new ImageDisplayTask(imageItem, imageView, textView);
            handler.post(imageDisplayTask);
        }
    }

    /**
     * 이미지 화면에 출력하는 Task
     */
    class ImageDisplayTask implements Runnable {

        private ImageItem imageItem;
        private ImageView imageView;
        private TextView textView;

        public ImageDisplayTask(ImageItem imageItem, ImageView imageView, TextView textView) {
            this.imageItem = imageItem;
            this.imageView = imageView;
            this.textView = textView;
        }

        @Override
        public void run() {
            // 뷰홀더에 마지막으로 참조한 이미지만 화면에 보여준다
            if(!imageReused.containsValue(imageItem.getUrl())) {
                return;
            }

            imageView.setImageBitmap(imageItem.getBitmap());
            switch (imageItem.getType()) {
                case BITMAP_FROM_MEMORY:
                    textView.setText("MEMORY");
                    textView.setBackgroundColor(Color.GREEN);
                    break;
                case BITMAP_FROM_DISK :
                    textView.setText("DISK");
                    textView.setBackgroundColor(Color.BLUE);
                    break;
                case BITMAP_FROM_URL :
                    textView.setText("URL");
                    textView.setBackgroundColor(Color.RED);
                    break;
            }
        }
    }

    private void copyData(BufferedInputStream bis, BufferedOutputStream bos) {
        final int bufferSize = 1024;
        byte[] bytes = new byte[bufferSize];
        try {
            int read = 0;
            while((read = bis.read(bytes, 0, bufferSize)) != -1) {
                bos.write(bytes, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
