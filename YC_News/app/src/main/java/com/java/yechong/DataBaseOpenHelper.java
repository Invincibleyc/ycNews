package com.java.yechong;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseOpenHelper extends SQLiteOpenHelper {
    private Context mContext;
    private String mName;
    private String mPath;
    public DataBaseOpenHelper(Context context, String name){
        super(context, name, null, 1);
        mName = name;
        mContext = context;
        mPath = "/data/data/"+context.getPackageName()+"/databases/"+mName;
    }

    public void createDataBase(){
        boolean isExist = isDataBaseExist();
        if(!isExist){
            this.getReadableDatabase();
            this.close();
//            try{
//                copyAssets();
//            } catch (IOException e){
//                throw new Error("CopyAssetsError");
//            }
        }
    }

    public boolean isDataBaseExist(){
        return new File(mPath).exists();
    }

    public void copyAssets() throws IOException{
        InputStream is = mContext.getAssets().open(mName);
        FileOutputStream fos = new FileOutputStream(mPath);
        byte mBuffer[] = new byte[1024];
        int len = is.read(mBuffer);
        while (len > 0){
            fos.write(mBuffer);
            len = is.read(mBuffer);
        }
        fos.flush();
        fos.close();
        is.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

