package com.iitropar.rahul.wakeupalarm.alarm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.*;
import com.google.android.*;
import com.google.android.gms.gcm.*;
import com.google.android.gms.gcm.GoogleCloudMessaging.*;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.iitropar.rahul.wakeupalarm.utility.AlertDialogManager;
import com.iitropar.rahul.wakeupalarm.utility.ConnectionDetector ;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import com.iitropar.rahul.wakeupalarm.R;
import com.iitropar.rahul.wakeupalarm.utility.JSONParser;

import org.json.JSONObject;

public class alarmRegister{

    private static String TAG = "MyDebug: ";
    private static ConnectionDetector cd;
    private AlertDialogManager alert;
    private static SharedPreferences preferences;
    private static String mobileNumber;
    public static final String EXTRA_MESSAGE = "message";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static GoogleCloudMessaging gcm;
    private static AtomicInteger msgId = new AtomicInteger();
    private static SharedPreferences prefs;
    private static Context context;
    private static String regid;

    public alarmRegister(Context ctx, String mobile) {
        context = ctx;
        mobileNumber = mobile;

        if (!checkConnection()) {
            return;
        }
        Log.d(TAG, "internet checked");

        this.checkRegistered();
    }

    private void checkRegistered() {

        if (this.checkPlayServices()) {

            gcm = GoogleCloudMessaging.getInstance(context);

            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                //setContentView(R.layout.activity_alarm_register);
//                this.notRegistered() ;
                registerInBackground(mobileNumber);
            } else {
                this.startShowAlarms(regid);
                Toast.makeText(context, "Already registered", Toast.LENGTH_LONG).show();
            }

        } else {
            Log.d(TAG, "No valid Google Play Services APK found.");
        }
    }

    private String getRegistrationId(Context context) {

        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(context.getString(R.string.PROPERTY_REG_ID), "");

        if (registrationId.isEmpty()) {
            Log.d(TAG, "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.

        int registeredVersion = prefs.getInt(context.getString(R.string.PROPERTY_APP_VERSION), Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);

        if (registeredVersion != currentVersion) {
            Log.d(TAG, "App version changed.");
            return "";
        }

        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return context.getSharedPreferences(context.getString(R.string.gcmPreference),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground(final String mobileNumber) {

        new AsyncTask<Void, Void, Boolean>() {
//            private ProgressDialog progressMessage;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                /*progressMessage = new ProgressDialog(context);
                progressMessage.setMessage("Loading ...");
                progressMessage.setIndeterminate(false);
                progressMessage.setCancelable(false);
                progressMessage.show();*/
            }

            @Override
            protected Boolean doInBackground(Void[] params) {
                String msg = "";
                boolean registeredOnGCM = false;
                boolean registeredOnOurServer = false;
                Log.d(TAG, "Reached doInBack of register in background");
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                        Log.d(TAG, "New reistering");
                    }
                    regid = gcm.register(context.getString(R.string.SENDER_ID));
                    msg = "Device registered, registration ID=" + regid;
                    Log.d(TAG, "New registration: " + msg);
                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    /*while (!sendRegistrationIdToBackend(username, mobile, regid));*/
                    registeredOnOurServer = sendRegistrationIdToBackend(mobileNumber, regid);

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    if (registeredOnOurServer)
                        storeRegistrationId(context, regid);
                    registeredOnGCM = true;
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.d(TAG, "error: " + msg);
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                if (registeredOnGCM && registeredOnOurServer) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean msg) {
//              mDisplay.append(msg + "\n");
                if (msg) {
                    Toast.makeText(context, "Registered successfully.", Toast.LENGTH_LONG).show();
                    startShowAlarms(regid);
                } else
                    Toast.makeText(context, "Cannot register.", Toast.LENGTH_LONG).show();
                /*if (progressMessage != null && progressMessage.isShowing()) {
                    progressMessage.dismiss();
                }*/
                return;
            }
        }.execute(null, null, null);
    }


    private boolean sendRegistrationIdToBackend(String mobileNumber, String regid) {
        String ip = context.getString(R.string.SERVER_IP);
        String page = context.getString(R.string.Register_Page);
        String registerUrl = ip + page;
        Log.d(TAG, registerUrl);
        JSONParser jsonParser = new JSONParser();
        String params = "mobile=" + mobileNumber + "&regid=" + regid;
        Log.d(TAG, "params: " + params);
        JSONObject jsonObject = jsonParser.makeHttpRequest(registerUrl, "GET", params);
        try {
            boolean success = jsonObject.getBoolean("success");
            Log.d(TAG, "sucees: " + String.valueOf(success));
            if (success) {
                boolean registered = jsonObject.getBoolean("insert");
                Log.d(TAG, "registered: " + String.valueOf(registered));
                if (registered == true) {
                    Log.d(TAG, "registered true");
//                    Toast.makeText(context,"Successfully Registered",Toast.LENGTH_LONG).show() ;
                    Log.d(TAG, "ashdjadas");
                    return true;
                } else {
//                    Toast.makeText(context,"Maybe already registered, try different name",Toast.LENGTH_LONG).show() ;
                    return false;
                }
            } else {
                Toast.makeText(context, "Cannot Register, Server Problem, Try Later", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "elelelelele");
            return false;
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.d(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.PROPERTY_REG_ID), regId);
        editor.putInt(context.getString(R.string.PROPERTY_APP_VERSION), appVersion);
        editor.commit();
    }

    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (android.app.Activity) context,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                ((android.app.Activity) context).finish();
            }
            return false;
        }

        return true;
    }


//    private void notRegistered(){
//
//        Log.d(TAG,"reached not registered") ;
//        b.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                EditText edit_name = (EditText)findViewById(R.id.txtName) ;
//                EditText edit_mobileNumber = (EditText)findViewById(R.id.txtmobileNumber) ;
//                String entered_name = edit_name.getText().toString() ;
//                String entered_mobileNumber = edit_mobileNumber.getText().toString() ;
//
//                if (entered_name.trim().length() > 0 && entered_mobileNumber.trim().length() > 0){
////                    startShowAlarms();
//                    username = entered_name ;
//                    mobileNumber = entered_mobileNumber ;
//                    registerInBackground(username,mobileNumber);
//                    b.setEnabled(false);
//                }
//                else{
//                    b.setEnabled(true);
//                    Toast.makeText(alarmRegister.this,"Please correctly fill the text fields",Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//    }

    private void startShowAlarms(String regid) {
        Intent i = new Intent(context.getApplicationContext(), showAlarms.class);
        i.putExtra(context.getString(R.string.mobile), mobileNumber);
        i.putExtra(context.getString(R.string.PROPERTY_REG_ID), regid);
/*        SharedPreferences.Editor editor = preferences.edit() ;
        editor.putString(getString(R.string.username),username) ;
        editor.putString(getString(R.string.mobile),mobile);
        editor.commit();*/
        context.startActivity(i);
        ((android.app.Activity) context).finish();
    }

    private boolean checkConnection() {

        cd = new ConnectionDetector(context);
        alert = new AlertDialogManager();
        Log.d(TAG, "Checking internet");

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            Log.d(TAG, "not connected to any network");
            alert.showAlertDialog(context, "Network Connection Error", "Please connect to working Internet connection", false);
            Log.d(TAG, "determining done");
            return true;
        } else {
            Log.d(TAG, "connected to some network");
            return true;
        }
    }
}