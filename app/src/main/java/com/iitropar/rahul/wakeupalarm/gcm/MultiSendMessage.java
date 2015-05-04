package com.iitropar.rahul.wakeupalarm.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.iitropar.rahul.wakeupalarm.R;
import com.iitropar.rahul.wakeupalarm.utility.JSONParser;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by tangbang on 4/13/2015.
 */
public class MultiSendMessage extends AsyncTask<Void,Void,Void> {

    private String message ;
    private ArrayList<String> to ;
    private Context context ;

    public MultiSendMessage(Context context, ArrayList<String> to, String message){
        this.context = context ;
        this.to = to ;
        try {
            this.message = URLEncoder.encode(message,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Void doInBackground(Void[] parmas){
        String ip = context.getString(R.string.SERVER_IP) ;
        String page = context.getString(R.string.Multi_Send_Message) ;
        String url = ip + page ;
        String params = "count=" + String.valueOf(to.size()) ;
        for (int i = 0 ; i < to.size() ; i++){
            params += "&phoneNumber" + String.valueOf(i) + "=" + to.get(i) ;
        }
        params += "&message="+message;
        JSONParser jsonParser = new JSONParser() ;
        JSONObject jsonObject = jsonParser.makeHttpRequest(url, "GET", params) ;

        return null;
    }
}