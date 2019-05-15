package com.java.yechong;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.UrlQuerySanitizer;
import android.sax.EndElementListener;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RssHelper {

    public ArrayList<Label> getAllChannels(){
        ArrayList<Label> allChannels = new ArrayList<>();
        try {
            URL url = new URL("http://rss.qq.com/index.shtml");
            URLConnection myConnection = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(myConnection.getInputStream(), "gb2312"));
            String input;
            input = in.readLine();
            Pattern channelPattern = Pattern.compile("(http://rss\\.qq\\.com/.+?\\.htm).+?>(.+?)</a>");
            Matcher myMatcher;
            while(input != null){
                myMatcher = channelPattern.matcher(input);
                while(myMatcher.find()){
                    String link = myMatcher.group(1);
                    String title = myMatcher.group(2);
                    title = title.replace("频道", "");
                    Label channel = new Label(title, link);
                    allChannels.add(channel);
                }
                input = in.readLine();
            }
            in.close();
        } catch (MalformedURLException e){
            e.printStackTrace();
            System.out.println(e.toString());
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(e.toString());
        }
        return allChannels;
    }
    public ArrayList<String> getXML(String urlName){
        ArrayList<String> allXML = new ArrayList<>();
        try{
            URL url = new URL(urlName);
            HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(myConnection.getInputStream(), "gb2312"));
            String input;
            input = in.readLine();
            Pattern xmlPattern = Pattern.compile("(http[^\"]+?\\.xml)");
            Matcher myMatcher;
            while(input!=null) {
                myMatcher = xmlPattern.matcher(input);
                while(myMatcher.find()) {
                    if(!myMatcher.group(1).contains("mail.qq"))
                        allXML.add(myMatcher.group(1));
                }
                input = in.readLine();
            }
            in.close();
        } catch(Exception e) {
            System.out.println(e.toString());
        }
        return allXML;
    }

    private String getEncoding(String urlName){
        String encoding = "";
        try{
            URL url = new URL(urlName);
            HttpURLConnection myConnection = (HttpURLConnection)url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(myConnection.getInputStream()));
            Pattern encodingPattern = Pattern.compile("(charset=|encoding=\")(.+?)\"");
            Matcher matcher;
            String input;
            input = in.readLine();

            while(input!=null) {
                matcher = encodingPattern.matcher(input);
                if(matcher.find()) {
                    in.close();
                    return matcher.group(2);
                }
                input = in.readLine();
            }
            in.close();
        } catch(Exception e) {
            System.out.println(e.toString());
        }
        return encoding;
    }

    private String getCode(String urlName) {
        String content = "";
        try{
            String encoding = getEncoding(urlName);
            Log.e("encoding", encoding);
            URL url = new URL(urlName);
            HttpURLConnection myConnection = (HttpURLConnection)url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(myConnection.getInputStream(), encoding));
            String input;
            input = in.readLine();
            while(input!=null) {
                content += input;
                input = in.readLine();
            }
            in.close();
        } catch(Exception e) {
            System.out.println(e.toString());
        }
//        System.out.println(content);
        return content;
    }

    public String getImgLink(String urlName){
        String imgLink = "";
        try {
            Document doc = Jsoup.connect(urlName).get();
            Elements elements = doc.select("div.qq_article");
            imgLink = elements.select("img").attr("src");
            if(imgLink.equals(""))
                return imgLink;
            if(!imgLink.contains("http:") && !imgLink.contains("https:"))
                imgLink = "https:"+imgLink;
        }catch(Exception e) {
            Log.e("getImg error", e.toString());
        }
        return imgLink;
    }

    public Bitmap getImg(String urlName){
        Bitmap bitmap = null;
        try {
            Document doc = Jsoup.connect(urlName).get();
            Elements elements = doc.select("div.qq_article");
            String imgUrl = elements.select("img").attr("src");
            if(imgUrl.equals(""))
                return null;
            if(!imgUrl.contains("http:"))
                imgUrl = "https:"+imgUrl;
            URL url = new URL(imgUrl);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int length;
            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            bitmap = ImgHelper.BytesToBitmap(output.toByteArray());
            Log.i("img",imgUrl);
        }catch(Exception e) {
            Log.e("getImg error", e.toString());
        }
        return bitmap;
    }

    public ArrayList<NewsItem> getItems(String urlName){
        ArrayList<NewsItem> res = new ArrayList<>();
        String content = getCode(urlName);

        System.out.println(content);
        Pattern itemPattern = Pattern.compile("<item>(.+?)</item>");
        Pattern titlePattern = Pattern.compile("<title>(.+?)</title>");
        Pattern linkPattern = Pattern.compile("<link>(.+?)</link>");
        Pattern datePattern = Pattern.compile("<pubDate>(.+?)</pubDate>");
        Pattern descriptionPattern = Pattern.compile("<description>(.+?)</description>");
        Matcher myMatcher;
        Matcher tempMatcher;
        myMatcher = itemPattern.matcher(content);
        String temp;
        while(myMatcher.find()) {
            NewsItem myItem = new NewsItem();
            temp = myMatcher.group();
            tempMatcher = titlePattern.matcher(temp);
            if(tempMatcher.find()) {
                myItem.setTitle(tempMatcher.group(1));
            }
            tempMatcher = linkPattern.matcher(temp);
            if(tempMatcher.find()) {
                String link = tempMatcher.group(1);
                myItem.setLink(link);
            }
            tempMatcher = datePattern.matcher(temp);
            if(tempMatcher.find()) {
                myItem.setDate(tempMatcher.group(1));
            }
            tempMatcher = descriptionPattern.matcher(temp);
            if(tempMatcher.find()) {
                myItem.setDescription(tempMatcher.group(1));
            }
            res.add(myItem);
        }
        return res;
    }
//    public static void main(String args[]) {
//        RssHelper myReader = new RssHelper();
//        ArrayList<XMLItem> list = myReader.getItems("http://news.qq.com/newsgn/rss_newsgn.xml");
//        for(int i = 0; i < list.size(); i++) {
//            list.get(i).show();
//        }
////        		getXML("http://rss.qq.com/index.shtml");
//    }
}

class NewsItem implements Serializable{
    private String title;
    private String link;
    private String pubDate;
    private String description;
    private Bitmap bitmap;
    private boolean visited;
    private boolean favorite;
    public NewsItem() {
        title = "";
        link = "";
        pubDate = "";
        description = "";
        bitmap = null;
        visited = false;
        favorite = false;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public void setDate(String pubDate) {
        this.pubDate = pubDate;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setBitmap(Bitmap bitmap){ this.bitmap = bitmap; }
    public void setVisited(boolean status){ this.visited = status; }
    public void setFavorite(boolean favorite){ this.favorite = favorite; }

    public String getTitle() {
        return title;
    }
    public String getLink() {
        return link;
    }
    public String getDate() {
        return pubDate;
    }
    public String getDescription() {
        return description;
    }
    public Bitmap getBitmap(){ return bitmap; }
    public boolean isVisited(){ return visited; }
    public boolean isFavorite(){ return favorite; }
    public void show() {
//        Log.i("title", title);
        System.out.println("title: "+title);
        System.out.println("date "+pubDate);
        System.out.println("link: "+link);
        System.out.println("description: "+description);
        System.out.println();
    }
}

class Label implements Serializable{
    private String title;
    private String link;
    public Label(String title, String link){
        this.title = title;
        this.link = link;
    }
    public String getTitle(){
        return title;
    }
    public String getLink(){
        return link;
    }
}