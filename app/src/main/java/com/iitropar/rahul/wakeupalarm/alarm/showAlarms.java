package com.iitropar.rahul.wakeupalarm.alarm;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.iitropar.rahul.wakeupalarm.R;

import java.text.ParseException;
import java.util.List;

/**
 * Created by sds on 12/4/15.
 */
public class showAlarms extends ListActivity {

    Button addAlarm,selectFriends,selectTone;
    List ids;
    Ringtone ringtone;
    RingtoneManager rm;
    Cursor rcursor;
    Uri ringtoneuri;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent optionsintent=new Intent("com.iitropar.rahul.wakeupalarm.alarm.OPTIONSACTIVITY").putExtra("uniqueid",""+(ids.get(position))+"");
        startActivity(optionsintent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showalarms);
        ringtoneuri=null;
        addAlarm=(Button)findViewById(R.id.addalarmbutton);
        addAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setalarmintent=new Intent("com.iitropar.rahul.wakeupalarm.alarm.SETALARMACTIVITY");

//                setalarmintent.putExtra("ringuri",ringtoneuri);
                startActivity(setalarmintent);
            }
        });

        selectFriends=(Button)findViewById(R.id.selectfriends);
        selectFriends.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                /// internet check
                if(!isNetworkAvailable()){
                    showInternetDisabledAlertToUser();
                }
                /// internet check
                else{
                    Intent selectfriendsintent=new Intent("com.iitropar.rahul.wakeupalarm.alarm.SELECTFRIENDS");
                    startActivity(selectfriendsintent);
                }
            }
        });

        //Select Ringtone
        rm=new RingtoneManager(this);
        rcursor=rm.getCursor();
        selectTone=(Button)findViewById(R.id.selecttone);
        selectTone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ringtoneintent=new Intent(rm.ACTION_RINGTONE_PICKER);
                ringtoneintent.putExtra(rm.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                ringtoneintent.putExtra(rm.EXTRA_RINGTONE_TITLE,"Select the tone for Alarm");

                String uri=null;
                if ( uri != null ) {
                    ringtoneintent.putExtra(rm.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uri));
                } else {
                    ringtoneintent.putExtra(rm.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                }

                startActivityForResult(ringtoneintent, 0);

            }
        });

        AlarmDatabase ad=new AlarmDatabase(this);
        ad.open();
        List data= null;
        try {
            data = ad.getData();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ids=ad.getIDs();
        ad.close();
        ListView listview=(ListView)findViewById(android.R.id.list);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(showAlarms.this,R.layout.fragment_list,data);
        listview.setAdapter(adapter);

    }


    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public void showInternetDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Device is not connected to internet. Connect it now?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Connect To Internet",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callInternetSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_SETTINGS);
                                startActivity(callInternetSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        AlarmDatabase ad=new AlarmDatabase(this);
        ad.open();
        List data= null;
        try {
            data = ad.getData();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ids=ad.getIDs();
        ad.close();
        ListView listview=(ListView)findViewById(android.R.id.list);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(showAlarms.this,R.layout.fragment_list,data);
        listview.setAdapter(adapter);
    }

    //For ringtone
    protected void onActivityResult(int requestCode, int resultCode, Intent ringtoneintent) {
        switch (resultCode) {

            case RESULT_OK:

                Uri uri = ringtoneintent.getParcelableExtra(rm.EXTRA_RINGTONE_PICKED_URI);
                ringtoneuri=uri;

                if(uri==null)
                    Log.i("deepak","asdkjaskjs");


//prints out the result in the console window
                Log.i("deepak", "uri " + uri);

//this passed the ringtone selected from the user to a new method
//                play(uri);

//inserts another line break for more data, this times adds the cursor count on the selected item
                Log.i("deepak",("\n " + rcursor.getCount()));

//set default ringtone
                try
                {
                    if(uri!=null) {
                        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.Saved_Phone_Number), MODE_PRIVATE);
                        if (sharedPreferences != null) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.Ringtone_uri), uri.toString());
                            Log.i("deepak send", uri.toString());
                            editor.commit();
                        } else
                            Log.i("deepak null", "nulll");
                    }
                }
                catch (Exception localException)
                {

                }
                break;


        }

    }


}

