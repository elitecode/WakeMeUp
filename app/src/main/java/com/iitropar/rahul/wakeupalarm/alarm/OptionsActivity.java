package com.iitropar.rahul.wakeupalarm.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.iitropar.rahul.wakeupalarm.R;

import java.util.Calendar;

public class OptionsActivity extends Activity {


    TextView time,date;
    Button settime,setdate,updalarm,deletebutton;
    private static String TAG = "MyDebug" ;
    int selectedyear;
    int selectedday;
    int selectedmonth;
    int selectedhour;
    int selectedminutes;
    int uniqid;
    TextView locationText ;
    Button locationButton ;
    Pair<Double,Double> locationFromDB;
    Pair<Double,Double> location_from_map = null;
    String addressFromMap ;
    String addressFromDB ;
    public DatePickerDialog.OnDateSetListener ondateset = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            selectedday=dayOfMonth;
            selectedmonth=monthOfYear;
            selectedyear=year;
            updatedatetext();
        }
    };

    private void updatedatetext() {
        date.setText(selectedday+"/"+(selectedmonth+1)+"/"+selectedyear);
    }


    public OnTimeSetListener ontimeset = new OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            selectedhour=hourOfDay;
            selectedminutes=minute;
            updatetimetext();
        }
    };

    private void updatetimetext() {
        String hour = (selectedhour > 9) ? ""+selectedhour: "0"+selectedhour ;
        String minutes = (selectedminutes > 9) ?""+selectedminutes : "0"+selectedminutes;
        time.setText(hour+":"+minutes);

    }

    private void updateLocation(){
        if (addressFromDB == null){

        }
        else{
            if (locationText != null){
                locationText.setText(addressFromDB);
            }
        }
    }

    private DatePickerDialog showDatePickerDialog(int initialYear, int initialMonth, int initialDay, DatePickerDialog.OnDateSetListener listener) {
        DatePickerDialog dialog = new DatePickerDialog(this, listener, initialYear, initialMonth, initialDay);
        dialog.show();
        return dialog;
    }

    private TimePickerDialog showTimePickerDialog(int initialHour, int initialMinutes, boolean is24Hour, OnTimeSetListener listener) {
        TimePickerDialog dialog = new TimePickerDialog(this, listener, initialHour, initialMinutes, is24Hour);
        dialog.show();
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);
       uniqid= Integer.parseInt(getIntent().getStringExtra("uniqueid"));
        Log.i("uniqueid","uu:"+uniqid);

        final AlarmDatabase ad=new AlarmDatabase(this);
        ad.open();

        final Calendar cal = Calendar.getInstance();

        selectedhour=ad.getHour(uniqid);
        selectedminutes=ad.getMinutes(uniqid);
        locationFromDB = ad.getLocation(uniqid) ;
        addressFromDB = ad.getAddress(uniqid) ;
        selectedyear=cal.get(Calendar.YEAR);
        selectedmonth=cal.get(Calendar.MONTH);
        selectedday=cal.get(Calendar.DAY_OF_MONTH);

        time = (TextView) findViewById(R.id.textView2);
        date = (TextView) findViewById(R.id.textView3);
        locationText = (TextView)findViewById(R.id.text_location);
        locationButton = (Button)findViewById(R.id.button_location);
        updateLocation();
        date.setText(ad.getDate(uniqid));
        updatetimetext();

        settime = (Button) findViewById(R.id.button2);
        setdate = (Button) findViewById(R.id.button3);

        settime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(selectedhour, selectedminutes, true, ontimeset);
            }
        });

        setdate.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showDatePickerDialog(selectedyear, selectedmonth, selectedday, ondateset);
            }
        });


        updalarm=(Button)findViewById(R.id.openApp);
        updalarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               updatealarm();finish();
            }
        });


        deletebutton=(Button)findViewById(R.id.button5);
        deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cancel pendingintent
                Intent intent = new Intent(getBaseContext(), AlarmActivity.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), (int)uniqid, intent, 0);
                AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);

                //Delete from database
                ad.deleteEntry(uniqid);

                //Prompt
                Toast.makeText(getBaseContext(),"Deleted Alarm",Toast.LENGTH_LONG).show();
                ad.close();
                fin();
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // gps check

                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    showGPSDisabledAlertToUser();
                }else{

                    Intent intent = new Intent(OptionsActivity.this, com.iitropar.rahul.wakeupalarm.map.MainActivity.class) ;
                    if (locationFromDB!=null){
                        Double lat = locationFromDB.first ;
                        Double lon = locationFromDB.second ;
                        intent.putExtra("latlong",true) ;
                        intent.putExtra("latitude",lat) ;
                        intent.putExtra("longitude",lon) ;
                    }
                    else{
                        intent.putExtra("latlong",false) ;
                    }

                    startActivityForResult(intent, Integer.parseInt(getString(R.string.REQUEST_LOCATION)));

                }

            }
        });
    }


    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==Integer.parseInt(getString(R.string.REQUEST_LOCATION))){
            if (resultCode == RESULT_OK) {
                double latitude = data.getDoubleExtra("latitude", 0.0);
                double longitude = data.getDoubleExtra("longitude", 0.0);
                location_from_map = new Pair<Double, Double>(latitude, longitude);
                addressFromMap = data.getStringExtra("address") ;
                String lat = String.valueOf(latitude);
                String lon = String.valueOf(longitude);
                lat = lat.substring(0, 5);
                lon = lon.substring(0, 5);
                locationText.setText(addressFromMap);
                Log.d(TAG, "lat: " + latitude + "; long: " + longitude);
                Log.d(TAG,"address: " + addressFromMap);
            }
            else{
                return;
            }
        }
    }

    private void updatealarm() {

        Calendar currentcal=Calendar.getInstance();
        Calendar targetCal=Calendar.getInstance();
        targetCal.set(selectedyear,selectedmonth,selectedday,selectedhour,selectedminutes,00);

        if(targetCal.compareTo(currentcal)>0) {

            //Cancel previous intent
            Intent intent = new Intent(getBaseContext(), AlarmActivity.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(),  uniqid, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);


            //Creating the pending intent and giving the alarm manager
            intent = new Intent(getBaseContext(), AlarmActivity.class);
            int intuniqid=uniqid;
            intent.putExtra("uniqueid",intuniqid);
            intent.putExtra("type","alarm");
            Log.i("mydebug",intent.getStringExtra("type")+" "+intent.getIntExtra("uniqueid",0));
            pendingIntent = PendingIntent.getBroadcast(
                    getBaseContext(),intuniqid, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);

            //Updating in database
            AlarmDatabase ad = new AlarmDatabase(getBaseContext());
            ad.open();
            int month=(selectedmonth+1);
            String monthstr="";
            if(month<10)
                monthstr="0"+month;
            else
                monthstr=""+month;
            Double latitude = null ;
            Double longitude = null ;
            String address = null;
            if (location_from_map != null) {
                latitude = location_from_map.first;
                longitude = location_from_map.second;
            }
            else{
            }

            ad.updateEntry(uniqid,targetCal.get(Calendar.HOUR_OF_DAY),targetCal.get(Calendar.MINUTE),(targetCal.get(Calendar.DAY_OF_MONTH)+1)+"/"+monthstr+"/"+targetCal.get(Calendar.YEAR),latitude,longitude,addressFromMap);

            //Prompt
            Toast.makeText(getBaseContext(), "Alarm Updated at " + targetCal.getTime().toString(), Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(getBaseContext(),"Invalid Date/Time ",Toast.LENGTH_LONG).show();
    }
    private void fin() {
        this.finish();
    }
}