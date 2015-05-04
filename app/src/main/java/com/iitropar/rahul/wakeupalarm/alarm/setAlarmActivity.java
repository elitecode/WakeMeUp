package com.iitropar.rahul.wakeupalarm.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
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
import java.util.Random;

public class setAlarmActivity extends Activity {

    int uniqueid;
    static String TAG = "MyDebug" ;
    TextView time,date,location_selected;
    Button settime,setdate,setalarm;
    Button location_btn;

    int selectedyear;
    int selectedday;
    int selectedmonth;
    int selectedhour;
    int selectedminutes;
    Pair<Double,Double> location_from_map = null;
    String addressFromMap = null ;

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
        setContentView(R.layout.alarmview);

        final Calendar cal = Calendar.getInstance();
        selectedhour=cal.get(Calendar.HOUR_OF_DAY);
        selectedminutes=cal.get(Calendar.MINUTE);
        selectedyear=cal.get(Calendar.YEAR);
        selectedmonth=cal.get(Calendar.MONTH);
        selectedday=cal.get(Calendar.DAY_OF_MONTH);


        time = (TextView) findViewById(R.id.textView2);
        date = (TextView) findViewById(R.id.textView3);

        updatedatetext();
        updatetimetext();


        settime = (Button) findViewById(R.id.button2);
        setdate = (Button) findViewById(R.id.button4);
        location_btn = (Button)findViewById(R.id.button5) ;
        location_selected = (TextView)findViewById((R.id.textView4)) ;

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


        setalarm=(Button)findViewById(R.id.openApp);
        setalarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar newcal=Calendar.getInstance();
                final Calendar currentcal=Calendar.getInstance();

                newcal.set(selectedyear,selectedmonth,selectedday,selectedhour,selectedminutes,00);
                if(newcal.compareTo(currentcal)<=0)
                    Toast.makeText(getBaseContext(),"Invalid Date/Time",Toast.LENGTH_LONG).show();
                else {
                    setAlarm(newcal);
                    finish();
                }
            }
        });

        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickContactIntent = new Intent(setAlarmActivity.this, com.iitropar.rahul.wakeupalarm.map.MainActivity.class);
//                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
                startActivityForResult(pickContactIntent, Integer.parseInt(getString(R.string.REQUEST_LOCATION)));
            }
        });

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
                addressFromMap = data.getStringExtra("address") ;
                location_from_map = new Pair<Double, Double>(latitude, longitude);
                String lat = String.valueOf(latitude);
                String lon = String.valueOf(longitude);
                lat = lat.substring(0, 5);
                lon = lon.substring(0, 5);
                location_selected.setText(addressFromMap);
                Log.d(TAG, "lat: " + latitude + "; long: " + longitude);
                Log.d(TAG,"address: " + addressFromMap) ;
            }
            else{
                return ;
            }
        }
    }


    private void setAlarm(Calendar targetCal) {

        //Inserting into database
        AlarmDatabase ad=new AlarmDatabase(this);
        ad.open();
        Random rgn=new Random();
        uniqueid=rgn.nextInt(20000);
        int month=(targetCal.get(Calendar.MONTH)+1);
        String monthstr="";
        if(month<10)
            monthstr="0"+month;
        else
            monthstr=""+month;
        Double latitude = null ;
        Double longitude = null ;
        if (location_from_map != null) {
            latitude = location_from_map.first;
            longitude = location_from_map.second;
        }
        else{
        }

        ad.insertEntry(targetCal.get(Calendar.HOUR_OF_DAY),targetCal.get(Calendar.MINUTE),(targetCal.get(Calendar.DAY_OF_MONTH)+1)+"/"+monthstr+"/"+targetCal.get(Calendar.YEAR),uniqueid,latitude,longitude,addressFromMap);
        //Creating the pending intent and giving the alarm manager

        Intent intent = new Intent(getBaseContext(), AlarmActivity.class);
        intent.putExtra("uniqueid",uniqueid);
        intent.putExtra("type","alarm");
        Log.d(TAG,"puttin in intent latitude and long") ;
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        Log.d(TAG,"done putting") ;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getBaseContext(),uniqueid, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(),pendingIntent);

        ad.close();

        //Prompt
        Toast.makeText(getBaseContext(),"Alarm Set at "+targetCal.getTime().toString(),Toast.LENGTH_LONG).show();
    }
}