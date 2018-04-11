package com.example.kim.imageloading;

import android.graphics.Bitmap;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by KIM on 2018-04-12.
 */

public class MemoryCache {
    private long MAX_MEMORY_CACHE_SIZE = 1024 * 1024 * 10; // 기본 10MB
    private Map<String, Bitmap> memoryCache; // 접근 빈도가 낮은 순으로 정렬
    private long currentMemorySize;

    public MemoryCache() {
        currentMemorySize = 0;
        memoryCache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 0.75f, true));
    }

    public void putIntoMemoryCache(String key, Bitmap bitmap) {
        if(memoryCache.containsKey(key))
            currentMemorySize -= bitmap.getByteCount();

        memoryCache.put(key, bitmap);
        currentMemorySize += bitmap.getByteCount();

        Iterator<Map.Entry<String, Bitmap>> iter = memoryCache.entrySet().iterator();
        while(currentMemorySize > MAX_MEMORY_CACHE_SIZE) {
            currentMemorySize -= iter.next().getValue().getByteCount();
            iter.remove(); // 접근빈도가 가장 낮은 객체 삭제
        }
    }

    public Bitmap getFromMemoryCache(String key) {
        if(!memoryCache.containsKey(key))
            return null;

        return memoryCache.get(key);
    }

    public void clear() {
        memoryCache.clear();
        currentMemorySize = 0;
    }
}
