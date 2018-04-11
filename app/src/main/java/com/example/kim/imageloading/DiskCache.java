package com.example.kim.imageloading;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Created by kim on 2018. 4. 12..
 */

public class DiskCache {

    private File cacheDir;

    public DiskCache(Context context){
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(),"ImageLoadingCache");
        else
            cacheDir = context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url){
        url = url.substring(url.indexOf("/Images/Thumbnails/"));
        url = url.replace("/", "_");
        String filename = String.valueOf(url);
        Log.d("TEST10", "filename = " + filename);
        File file = new File(cacheDir, filename);
        return file;

    }

    public void clear(){
        File[] files = cacheDir.listFiles();
        if(files == null)
            return;
        for(File file : files)
            file.delete();
    }

}
