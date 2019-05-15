package com.java.yechong;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;

public class TextAnalyzer {
    private HashMap<String, Integer> dictionary;

    public HashMap<String, Integer> getDictionary() {
        return dictionary;
    }

    public void setDictionary(HashMap<String, Integer> dictionary) {
        this.dictionary = dictionary;
    }

    public static void analysis(HashMap<String, Integer> mydict, String text) {
        if(mydict == null) {
            mydict = new HashMap<>();
        }
        StringReader sr = new StringReader(text);
        IKSegmenter ik = new IKSegmenter(sr, true);
        Lexeme lex = null;
        try{
            while((lex=ik.next())!=null) {
                String temp = lex.getLexemeText();
                int a = 0;
                if(mydict.containsKey(temp)) {
                    mydict.get(temp);
                }
                mydict.put(temp, a+1);
            }
        }catch (IOException e){
            System.out.println(e.toString());
        }
    }

    public static void analysis(String text, DataBaseOperator operator) {
        HashMap<String, Integer> mydict = new HashMap<>();
        StringReader sr = new StringReader(text);
        IKSegmenter ik = new IKSegmenter(sr, true);
        Lexeme lex = null;
        try{
            while((lex=ik.next())!=null) {
                String temp = lex.getLexemeText();
                int a = 0;
                if(mydict.containsKey(temp)) {
                    mydict.get(temp);
                }
                mydict.put(temp, a+1);
            }
        }catch (IOException e){
            System.out.println(e.toString());
        }
        Iterator iterator = mydict.entrySet().iterator();
        while(iterator.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iterator.next();
            String key = (String)entry.getKey();
            int count = (Integer)entry.getValue();
            operator.updateData(key, count);
        }
    }

    //根据mydict对text的相关性进行评价
    public static int evaluate(HashMap<String, Integer> mydict, String text){
        if(mydict.isEmpty())
            return 0;
        int score = 0;
        HashMap<String, Integer> textDict = new HashMap<>();
        analysis(textDict, text);
        Iterator iterator = mydict.entrySet().iterator();
        while(iterator.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iterator.next();
            String key = (String)entry.getKey();
            int value = (Integer)entry.getValue();
            if(mydict.containsKey(key)){
                score += value*mydict.get(key);
            }
        }
        return score;
    }
}
