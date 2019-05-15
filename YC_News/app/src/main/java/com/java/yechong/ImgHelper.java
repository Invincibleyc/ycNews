package com.java.yechong;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

public class ImgHelper {
    public ImgHelper(){
    }
    public static byte[] BitmapToBytes(Bitmap bitmap){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        return os.toByteArray();
    }
    public static Bitmap BytesToBitmap(byte[] bytes){
        if(bytes.length != 0)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        else
            return null;
    }
    public static Bitmap ZoomBitmap(Bitmap bitmap, int width, int height) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / bitmapWidth);
        float scaleHeight = ((float) height / bitmapHeight);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
        return newbmp;
    }
}
