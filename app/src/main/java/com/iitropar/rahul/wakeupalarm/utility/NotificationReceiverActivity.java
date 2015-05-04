package com.iitropar.rahul.wakeupalarm.utility;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.iitropar.rahul.wakeupalarm.R;

public class NotificationReceiverActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        Log.d("notification clic","alsdnlas") ;
        Button b = (Button)findViewById(R.id.button1) ;
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("notification click:" ,"kajshdljaldls") ;
            }
        });
    }
}