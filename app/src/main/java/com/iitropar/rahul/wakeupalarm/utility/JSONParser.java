package com.iitropar.rahul.wakeupalarm.utility;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.iitropar.rahul.wakeupalarm.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by tangbang on 4/23/2015.
 */

public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    static String TAG = "MyDebug: " ;
    // constructor

    public JSONParser() {
        Log.d(TAG,"json constructor") ;
    }

    // function get json from url
    // by making HTTP POST or GET mehtod
//    public JSONObject makeHttpRequest(String url, String method,
//                                      HashMap<String,String> params) {
//
//        // Making HTTP request
//        Log.d(TAG,"beforetry") ;
//
//        return jObj;
//        new AsyncTask<Void, Void, Boolean>() {
//            private ProgressDialog progressMessage;
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                progressMessage = new ProgressDialog(context);
//                progressMessage.setMessage("Loading ...");
//                progressMessage.setIndeterminate(false);
//                progressMessage.setCancelable(false);
//                progressMessage.show();
//            }
//
//            @Override
//            protected Boolean doInBackground(Void[] params) {
//                Log.d(TAG, "Reached doInBack of register in background");
//                try {
//
//                    // check for request method
//                    if(method == "POST"){
//
//                        HttpClient httpClient = new DefaultHttpClient();
//                        HttpPost httpPost = new HttpPost(url);
//                        //httpPost.setEntity(new UrlEncodedFormEntity(params));
//                        List <NameValuePair> nameValuePairs = new ArrayList<NameValuePair>() ;
//
//                /*Iterator it = params.entrySet().iterator();
//                while (it.hasNext()) {
//                    Map.Entry pair = (Map.Entry)it.next();
//                    Log.d(TAG, pair.getKey() + " = " + pair.getValue());
//                    String key = (String)pair.getKey() ;
//                    String value = (String)pair.getValue();
//                    nameValuePairs.add(new BasicNameValuePair("0","wo"));
//                }*/
//                        nameValuePairs.add(new BasicNameValuePair("0","8288909909"));
////                httpPost.setHeader(HTTP.CONTENT_TYPE,
////                        "application/x-www-form-urlencoded;charset=UTF-8");
//                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                        Log.d(TAG, "before executing") ;
//                        Log.d(TAG,"url: " + url + "a") ;
//                        HttpResponse httpResponse = httpClient.execute(httpPost);
//                        Log.d(TAG,"after executing") ;
//                        HttpEntity httpEntity = httpResponse.getEntity();
//                        String responseText = EntityUtils.toString(httpEntity) ;
//                        Log.d(TAG,"responseText: " + responseText) ;
////                jObj = new JSONObject(responseText) ;
//                    }
//                } catch (Exception e) {
//                    Log.e(TAG,"UnsupportedEncodingException") ;
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            protected void onPostExecute(Boolean msg) {
////              mDisplay.append(msg + "\n");
//                if (msg) {
//                    Toast.makeText(context, "Registered successfully.", Toast.LENGTH_LONG).show();
//                    startShowAlarms(regid);
//                } else
//                    Toast.makeText(context, "Cannot register.", Toast.LENGTH_LONG).show();
//                if (progressMessage != null && progressMessage.isShowing()) {
//                    progressMessage.dismiss();
//                }
//                return;
//            }
//        }.execute(null, null, null);
//    }


    public JSONObject makeHttpRequest(String url, String method,
                                      String params) {
        try{
            if(method == "GET"){
                Log.d(TAG,"methodget") ;
                // request method is GET
                DefaultHttpClient httpClient = new DefaultHttpClient();
                Log.d(TAG,"hello") ;
                final HttpParams httpParams = httpClient.getParams();
                Log.d(TAG,"wow") ;
                HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
                Log.d(TAG,"ma") ;
                HttpConnectionParams.setSoTimeout(httpParams, 10000);
                Log.d(TAG,"maa") ;
                String paramString = params;//URLEncodedUtils.format(params, "utf-8");
                Log.d(TAG,"makingurl") ;
                url += "?" + params.replace("+","%2B").replace(" ","+");
                Log.d(TAG,"ritesh"+paramString+"kumar") ;
                Log.d(TAG,"riteshencoded: "+url+" |kumar");
                HttpGet httpGet = new HttpGet(url);
                Log.d(TAG,"aafter newhttpget") ;
                HttpResponse httpResponse = httpClient.execute(httpGet);
                Log.d(TAG,"aafter httpresponse") ;
                HttpEntity httpEntity = httpResponse.getEntity();
                Log.d(TAG,"aafter httpentity") ;
                is = httpEntity.getContent();
                Log.d(TAG,"getend") ;
            }

        } catch (UnsupportedEncodingException e) {
            Log.e("JSON: ","UnsupportedEncodingException") ;
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            Log.e("JSON: ","ClientProtocolException") ;
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("JSON: ","io") ;
            e.printStackTrace();
        }

        try {
            Log.d(TAG,"bufferreader") ;
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.d(TAG,json) ;
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        Log.d(TAG,"bufferend") ;
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String

        Log.d(TAG, "jsonend") ;
        return jObj;
    }
}