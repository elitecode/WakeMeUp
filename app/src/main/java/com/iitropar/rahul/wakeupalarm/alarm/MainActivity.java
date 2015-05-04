package com.iitropar.rahul.wakeupalarm.alarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.iitropar.rahul.wakeupalarm.R;
import com.iitropar.rahul.wakeupalarm.gps.GPSTracker;

public class MainActivity extends ActionBarActivity {

    Button openapp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new GPSTracker(this);
        Log.d("starting: ", String.valueOf(GPSTracker.canGetLocation)) ;


//        openapp=(Button)findViewById(R.id.openApp);
//        openapp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("activity", "start");
        String present = checkIfPhoneNumberAlreadyRegistered(MainActivity.this);
        Intent intent;
        if (present != null){
            alarmRegister ar = new alarmRegister(MainActivity.this,present);
        }
        else {
            intent = new Intent(MainActivity.this, com.matesnetwork.cogdemo.MainActivity.class);
            startActivity(intent);
        }
        Log.d("activity", "starting");
    }

    private String checkIfPhoneNumberAlreadyRegistered(Context context){
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.Saved_Phone_Number),Context.MODE_PRIVATE) ;
        String phoneNumber = sharedPreferences.getString(getString(R.string.mobile),"") ;
        if (phoneNumber == null || phoneNumber.equals("")){
            return null ;
        }
        return phoneNumber ;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
