package com.java.yechong;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class DataBaseOperator {
    private DataBaseOpenHelper helper;
    private Context context;
    public static boolean init = false;

    public DataBaseOperator(Context context){
        this.context = context;
    }

    public void initDataBase(){
        helper = new DataBaseOpenHelper(context, "news.db");
        System.out.println(helper.isDataBaseExist());
        helper.createDataBase();
        helper.close();

        if(init)
            return;

//        try {
//            helper.createDataBase();
//        } catch (Exception e){
//            System.out.println(e.toString());
////            e.printStackTrace();
//        }
        System.out.println(helper.isDataBaseExist());
        SQLiteDatabase db = helper.getReadableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS news (newsID integer primary key autoincrement, title varchar(30), date varchar(20), link varchar(30), label varchar(10), description varchar(200), favorite varchar(3))");
        db.execSQL("CREATE TABLE IF NOT EXISTS labels (labelsID integer primary key autoincrement, title vchar(10), link varchar(20), status varchar(10))");
        db.execSQL("CREATE TABLE IF NOT EXISTS words (wordID integer primary key autoincrement, mykey vchar(10), count varchar(10))");
        db.close();
        init = true;
    }

    public void addData(NewsItem newsItem, String label){
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        String temp = "";
        if(newsItem.isFavorite())
            temp = "1";
        else
            temp = "0";
        database.execSQL("insert into news(title, date, link, label, description, favorite) values(?,?,?,?,?,?)", new Object[]{newsItem.getTitle(), newsItem.getDate(),
                newsItem.getLink(), label, newsItem.getDescription(), newsItem.isFavorite()});
        database.close();
    }

    public void addData(Label label, int status){
        String _status;
        if(status == 0)
            _status = "normal";
        else
            _status = "delete";
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        database.execSQL("insert into labels(title, link, status) values(?,?,?)", new Object[]{label.getTitle(), label.getLink(), _status});
        database.close();
    }

    public void addData(String mykey, int count){
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        database.execSQL("insert into words(mykey, count) values(?,?)", new Object[]{mykey, String.valueOf(count)});
        database.close();
    }

    public void updateData(String key, int count){
        helper.createDataBase();
        helper.close();
        ContentValues values = new ContentValues();
        values.put("count", String.valueOf(count));
        int id = queryWords(key);
        SQLiteDatabase database = helper.getReadableDatabase();
        if(id != -1){
            database.update("words", values, "wordID=?", new String[]{String.valueOf(id)});
        }
        else{
            database.execSQL("insert into words(mykey, count) values(?,?)", new Object[]{key, String.valueOf(count)});
        }
        database.close();
    }

    public void updateData(String title, boolean favorite){
        String temp;
        if(favorite)
            temp = "1";
        else
            temp = "0";
        helper.createDataBase();
        helper.close();
        ContentValues values = new ContentValues();
        values.put("favorite", temp);
        int id = queryNews(title);
        SQLiteDatabase database = helper.getReadableDatabase();
        if(id != -1){
            database.update("news", values, "newsID=?", new String[]{String.valueOf(id)});
        }
        database.close();
    }

    public ArrayList<NewsItem> queryAllNews(){
        ArrayList<NewsItem> myInfo = new ArrayList<>();
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from news", null);
        while (cursor.moveToNext()){
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String link = cursor.getString(cursor.getColumnIndex("link"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            String favorite = cursor.getString(cursor.getColumnIndex("favorite"));
            NewsItem item = new NewsItem();
            item.setTitle(title);
            item.setDate(date);
            item.setLink(link);
            item.setDescription(description);
            if(favorite.equals("1"))
                item.setFavorite(true);
            else
                item.setFavorite(false);
            myInfo.add(item);
        }
        cursor.close();
        database.close();
        return myInfo;
    }

    public ArrayList<NewsItem> queryAllNews(String label){
        ArrayList<NewsItem> myInfo = new ArrayList<>();
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from news", null);
        while (cursor.moveToNext()){
            String temp = cursor.getString(cursor.getColumnIndex("label"));
            if(!temp.equals(label))
                continue;
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String link = cursor.getString(cursor.getColumnIndex("link"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            String favorite = cursor.getString(cursor.getColumnIndex("favorite"));
            NewsItem item = new NewsItem();
            item.setTitle(title);
            item.setDate(date);
            item.setLink(link);
            item.setDescription(description);
            if(favorite.equals("1"))
                item.setFavorite(true);
            else
                item.setFavorite(false);
            myInfo.add(0, item);
        }
        cursor.close();
        database.close();
        return myInfo;
    }

    public ArrayList<NewsItem> queryFavoriteNews(){
        ArrayList<NewsItem> myInfo = new ArrayList<>();
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from news", null);
        while (cursor.moveToNext()){
            String temp = cursor.getString(cursor.getColumnIndex("favorite"));
            if(!temp.equals("1"))
                continue;
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String link = cursor.getString(cursor.getColumnIndex("link"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            String favorite = cursor.getString(cursor.getColumnIndex("favorite"));
            NewsItem item = new NewsItem();
            item.setTitle(title);
            item.setDate(date);
            item.setLink(link);
            item.setDescription(description);
            item.setFavorite(true);
            myInfo.add(0, item);
        }
        cursor.close();
        database.close();
        return myInfo;
    }

    public HashMap<String, Integer> queryAllWords(){
        HashMap<String, Integer> mydict = new HashMap<>();
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from words", null);
        while (cursor.moveToNext()){
            String mykey = cursor.getString(cursor.getColumnIndex("mykey"));
            String count = cursor.getString(cursor.getColumnIndex("count"));
            mydict.put(mykey, Integer.parseInt(count));
        }
        cursor.close();
        database.close();
        return mydict;
    }

    public ArrayList<Label> queryAllLabels(){
        ArrayList<Label> myInfo = new ArrayList<>();
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from labels", null);
        while (cursor.moveToNext()){
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String link = cursor.getString(cursor.getColumnIndex("link"));
            Label label = new Label(title, link);
            myInfo.add(label);
        }
        cursor.close();
        database.close();
        return myInfo;
    }

    public ArrayList<Label> queryAllLabels(int status){
        String _status = "";
        if(status == 0)
            _status = "normal";
        else
            _status = "delete";
        ArrayList<Label> myInfo = new ArrayList<>();
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from labels", null);
        while (cursor.moveToNext()){
            String myStatus = cursor.getString(cursor.getColumnIndex("status"));
            if(!myStatus.equals(_status))
                continue;
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String link = cursor.getString(cursor.getColumnIndex("link"));
            Label label = new Label(title, link);
            myInfo.add(label);
        }
        cursor.close();
        database.close();
        return myInfo;
    }

    public int queryNews(String title){
        int id = -1;
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.query("news", null, "title=?", new String[]{title}, null, null, null);
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex("newsID"));
        }
        cursor.close();
        database.close();
        return id;
    }

    public int queryWords(String key){
        int id = -1;
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.query("words", null, "mykey=?", new String[]{key}, null, null, null);
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex("wordID"));
        }
        cursor.close();
        database.close();
        return id;
    }

    public int queryFavoriteNews(String title){
        int id = -1;
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.query("news", null, "title=?", new String[]{title}, null, null, null);
        if (cursor.moveToFirst()) {
            if(cursor.getString(cursor.getColumnIndex("favorite")).equals("1"))
                id = cursor.getInt(cursor.getColumnIndex("newsID"));
        }
        cursor.close();
        database.close();
        return id;
    }

    public int queryLabel(String title){
        int id = -1;
        helper.createDataBase();
        helper.close();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.query("labels", null, "title=?", new String[]{title}, null, null, null);
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex("labelsID"));
        }
        cursor.close();
        database.close();
        return id;
    }

    public boolean deleteNews(String title){
        int id = queryNews(title);
        if(id != -1){
            helper.createDataBase();
            helper.close();
            SQLiteDatabase database = helper.getReadableDatabase();
            database.execSQL("delete from news where title=?", new Object[] { title });
            database.close();
            return true;
        }
        return false;
    }

    public boolean deleteLabel(String title){
        int id = queryLabel(title);
        if(id != -1){
            helper.createDataBase();
            helper.close();
            SQLiteDatabase database = helper.getReadableDatabase();
            database.execSQL("delete from labels where title=?", new Object[] { title });
            database.close();
            return true;
        }
        return false;
    }
}
