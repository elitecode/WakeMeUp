package com.iitropar.rahul.wakeupalarm.alarm;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iitropar.rahul.wakeupalarm.R;
import com.iitropar.rahul.wakeupalarm.utility.JSONParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sds on 21/4/15.
 */
public class selectFriends extends ListActivity{

    List contactnames,contactnums,dbnumbers;
    List globalnames,globalnums;
    FriendsDatabase fd;
    String TAG = "MyDebug: ";
    Context context;



    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        TextView tv=(TextView)v;
        String text=tv.getText().toString();

        if(text.contains(" |")==true)
        {
            String temp=text.substring(0,text.indexOf(" | \u2713"));
            int iid= (int) id;
            contactnames.set(iid,temp);
            fd.open();
            fd.deleteEntry(contactnums.get(iid).toString());
            fd.close();
            l.setAdapter(new ArrayAdapter<String>(selectFriends.this,R.layout.fragment_list,contactnames));
            Toast.makeText(getBaseContext(),temp+" Removed",Toast.LENGTH_SHORT).show();
        }
        else
        {
            String temp=text+" | \u2713";
            int iid=(int) id;
            contactnames.set(iid,temp);
            fd.open();
            fd.insertEntry(contactnums.get(iid).toString(),text);
            fd.close();
            l.setAdapter(new ArrayAdapter<String>(selectFriends.this,R.layout.fragment_list,contactnames));
            Toast.makeText(getBaseContext(),text+" Added",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactnames=new ArrayList();
        contactnums=new ArrayList();
        globalnames=new ArrayList();
        globalnums=new ArrayList();
        context = selectFriends.this;
        fd=new FriendsDatabase(this);
        fd.open();
        dbnumbers=fd.getNumbers();
        fd.close();

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            globalnames.add(name);
            globalnums.add(phoneNumber);


        }
        phones.close();
        Log.d(TAG,"verifyingContacts");
        verifyContacts(globalnums);
    }

    private void verifyContacts(List globalnums) {

        String url = getString(R.string.SERVER_IP)+getString(R.string.Verify_contact);
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("count",String.valueOf(globalnums.size()));
        for(int i=0;i<globalnums.size();i++)
            params.put(String.valueOf(i),(String)globalnums.get(i));
        Log.d(TAG,"url: "+url);
        makeHttpPOSTRequest(url, "POST", params);
    }

    public void makeHttpPOSTRequest(final String url, final String method,
                                      final HashMap<String,String> POSTparams) {
        Log.d(TAG,"Entering..");
        // Making HTTP request
        new AsyncTask<Void, Void, JSONObject>() {
            private ProgressDialog progressMessage;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressMessage = new ProgressDialog(context);
                progressMessage.setMessage("Loading ...");
                progressMessage.setIndeterminate(false);
                progressMessage.setCancelable(false);
                progressMessage.show();
            }

            @Override
            protected JSONObject doInBackground(Void[] params) {
                Log.d(TAG, "Reached doInBack of register in background");
                try {

                    // check for request method
                    if(method == "POST"){

                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost(url);
                        //httpPost.setEntity(new UrlEncodedFormEntity(params));
                        List <NameValuePair> nameValuePairs = new ArrayList<NameValuePair>() ;
                        Iterator it = POSTparams.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            Log.d(TAG, pair.getKey() + " = " + pair.getValue());
                            String key = (String)pair.getKey() ;
                            String value = (String)pair.getValue();
                            nameValuePairs.add(new BasicNameValuePair(key,value));
                        }
                        nameValuePairs.add(new BasicNameValuePair("count",String.valueOf(globalnums.size())));
//                httpPost.setHeader(HTTP.CONTENT_TYPE,
//                        "application/x-www-form-urlencoded;charset=UTF-8");
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        Log.d(TAG, "before executing") ;
                        Log.d(TAG,"url: " + url) ;
                        HttpResponse httpResponse = httpClient.execute(httpPost);
                        Log.d(TAG,httpResponse.getStatusLine()+" after executing") ;
                        HttpEntity httpEntity = httpResponse.getEntity();
                        Log.d(TAG,"httpentity");
//                        Log.d(TAG,httpEntity.);
                        String responseText = EntityUtils.toString(httpEntity) ;
                        Log.d(TAG,"responseText: " + responseText) ;
                        JSONObject jObj = new JSONObject(responseText) ;
                        return jObj;
                    }
                } catch (Exception e) {
                    Log.e(TAG,"UnsupportedEncodingException") ;
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject msg) {
//              mDisplay.append(msg + "\n");
                if (msg!=null) {
                    Toast.makeText(context, "Fetched successfully.", Toast.LENGTH_LONG).show();
                    retrieveContacts(msg);
                } else
                    Toast.makeText(context, "Connection Failed!", Toast.LENGTH_LONG).show();
                if (progressMessage != null && progressMessage.isShowing()) {
                    progressMessage.dismiss();
                }
                return;
            }
        }.execute(null, null, null);
    }

    void retrieveContacts(JSONObject response) {
        Log.d(TAG,"Retrieving Contacts");
        try {
            if(!response.getBoolean("success")) {
                Toast.makeText(context, "Server Error.", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"Response: "+response.toString());
        List verify=new ArrayList();
        for(int i=0;i<globalnums.size();i++) {
            try {
                if (response.getInt(String.valueOf(i)) == 1) {
                    verify.add(1);
                    Log.d(TAG,String.valueOf(i)+" MIL GYA!!");
                } else {
                    verify.add(0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                verify.add(0);
            }
        }

        for(int i=0;i<globalnums.size();i++)
        {
            if(verify.get(i)==1) {
                if (dbnumbers.contains(globalnums.get(i)))
                    contactnames.add(globalnames.get(i) + " | \u2713");
                else
                    contactnames.add(globalnames.get(i));
                contactnums.add(globalnums.get(i));
            }
        }

        setListAdapter(new ArrayAdapter<String>(selectFriends.this,R.layout.fragment_list,contactnames));

    }
}
