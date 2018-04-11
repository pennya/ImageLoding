package com.example.kim.imageloading;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by KIM on 2018-04-11.
 */

public class ImageItem implements Serializable {
    private static final long serialVersionUID = 5675306602784533605L;

    transient private Bitmap bitmap;
    private String url;
    private int type;

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

    public String getUrl() {
        return url;
    }

    public ImageItem setUrl(String url) {
        this.url = url;
        return this;
    }

    public int getType() {
        return type;
    }

    public ImageItem setType(int type) {
        this.type = type;
        return this;
    }
}
