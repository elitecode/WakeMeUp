package com.iitropar.rahul.wakeupalarm.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.iitropar.rahul.wakeupalarm.R;
import com.iitropar.rahul.wakeupalarm.alarm.AlarmActivity;
import com.iitropar.rahul.wakeupalarm.gps.GPSTracker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tangbang on 3/25/2015.
 */
public class GcmIntentService extends IntentService {
    private static String TAG = "MyDebug: " ;
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    private static GPSTracker gps ;
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

//                sendNotification(intent.getStringExtra("price")) ;
                Log.i(TAG, "Received: " + extras.toString()) ;
                Log.i(TAG, "intent was" + intent.toString()) ;
                handleMessage(intent.getStringExtra("price")) ;
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        Log.d(TAG,"msg: " + msg) ;
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        /*PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DemoActivity.class), 0);*/

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

//        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void handleMessage(String msg){
        try {
            Log.d(TAG,"Handle") ;
            JSONObject jsonObject = new JSONObject(msg);
            Log.d(TAG,"jsonstr: " + jsonObject.toString()) ;
            String messageType = jsonObject.getString(getString(R.string.Message_Type)) ;
            Log.d(TAG,"message type: " + messageType) ;
            if (messageType.toUpperCase().equals( getString(R.string.Return_Distance).toUpperCase() )){
                Log.d(TAG,"Calc Distance");
                Intent intent = new Intent(getBaseContext(), AlarmActivity.class);
                intent.putExtra("type", getString(R.string.Return_Distance));
                intent.putExtra("message", jsonObject.toString());
                intent.putExtra("uniqueid", jsonObject.getString("randCheck"));
                Log.d(TAG,intent.getStringExtra("uniqueid"));
                Log.d(TAG,"Broadcast");
                sendBroadcast(intent);
            }
            else if(messageType.toUpperCase().equals( getString(R.string.Receive_Distance).toUpperCase() )) {
                Log.d(TAG,"Receive Distance");
                Intent intent = new Intent(getBaseContext(), AlarmActivity.class);
                intent.putExtra("type", getString(R.string.Receive_Distance));
                intent.putExtra("message", jsonObject.toString());
                Log.d(TAG,"RECEIVEDISTFROM: "+jsonObject.getString("from"));
                intent.putExtra("uniqueid", jsonObject.getString("randCheck"));
                Log.d(TAG,"Broadcast");
                sendBroadcast(intent);
            }
            else if(messageType.toUpperCase().equals(getString(R.string.Call_Person).toUpperCase())) {
                Log.d(TAG, "Call Person");
                Intent intent = new Intent(getBaseContext(), AlarmActivity.class);
                intent.putExtra("type", getString(R.string.Call_Person));
                intent.putExtra("message", jsonObject.toString());
                Log.d(TAG, "CALLFROM: " + jsonObject.getString("from"));
                intent.putExtra("uniqueid", jsonObject.getString("randCheck"));
                Log.d(TAG, "Broadcast");
                sendBroadcast(intent);
            }
            else if(messageType.toUpperCase().equals(getString(R.string.Receive_Distance).toUpperCase())){
                Log.d(TAG,"Distance Received") ;
                sendNotification(msg) ;
            }

        }catch(JSONException e){
            Log.d("MyDebug","Error");
            e.printStackTrace();
        }
    }
}