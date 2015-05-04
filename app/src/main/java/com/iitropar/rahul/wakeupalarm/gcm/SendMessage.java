package com.iitropar.rahul.wakeupalarm.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.iitropar.rahul.wakeupalarm.R;
import com.iitropar.rahul.wakeupalarm.utility.JSONParser;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Rahul on 25 Apr 2015.
 */
public class SendMessage extends AsyncTask<Void,Void,Void>{
    private String to ;
    private String message ;
    private Context context ;

    public SendMessage(Context context, String to,String message){
        this.context = context ;
        this.to = to ;
        try {
            this.message = URLEncoder.encode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Void doInBackground(Void[] parmas){
        String ip = context.getString(R.string.SERVER_IP) ;
        String page = context.getString(R.string.Send_Message) ;
        String url = ip + page ;
        String param = "to=" + to + "&message=" + message ;
        JSONParser jsonParser = new JSONParser() ;
        JSONObject jsonObject = jsonParser.makeHttpRequest(url, "GET", param) ;

        return null;
    }
}