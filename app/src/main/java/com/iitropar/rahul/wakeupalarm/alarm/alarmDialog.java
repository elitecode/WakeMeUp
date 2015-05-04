package com.iitropar.rahul.wakeupalarm.alarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.iitropar.rahul.wakeupalarm.R;

/**
 * Created by Rahul on 23 Apr 2015.
 */
public class alarmDialog extends Activity {

    Button stopalarm;
    Ringtone rt;
    RingtoneManager rm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarmring);

        rm=new RingtoneManager(this);
        SharedPreferences sharedpreferences = this.getSharedPreferences(this.getString(R.string.Saved_Phone_Number), Context.MODE_PRIVATE) ;
        String URI = sharedpreferences.getString(this.getString(R.string.Ringtone_uri), "") ;
        Log.i("deepak rec", " " + URI);
        rt=rm.getRingtone(this,Uri.parse(URI));
        Toast.makeText(this, "It's time !!" , Toast.LENGTH_LONG).show();
        rt.play();

        stopalarm=(Button)findViewById(R.id.alarmstop);
        stopalarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rt.stop();

                Intent intent = new Intent(getBaseContext(), AlarmActivity.class);
                intent.putExtra("type","stopalarm");
                sendBroadcast(intent);

                finish();

            }
        });
    }
};