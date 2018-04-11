package com.example.kim.imageloading;

import android.graphics.Bitmap;

/**
 * Created by KIM on 2018-04-11.
 */

public class ImageItem {
    private Bitmap bitmap;
    private String src;

    public static ImageItem builder() {
        return new ImageItem();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public ImageItem setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    public String getSrc() {
        return src;
    }

    public ImageItem setSrc(String src) {
        this.src = src;
        return this;
    }
}
