package com.iitropar.rahul.wakeupalarm.alarm;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationProvider;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.iitropar.rahul.wakeupalarm.R;
import com.iitropar.rahul.wakeupalarm.gcm.MultiSendMessage;
import com.iitropar.rahul.wakeupalarm.gcm.SendMessage;
import com.iitropar.rahul.wakeupalarm.gps.GPSTracker;
import com.iitropar.rahul.wakeupalarm.utility.JSONParser;
import com.iitropar.rahul.wakeupalarm.utility.NotificationReceiverActivity;
import com.iitropar.rahul.wakeupalarm.utility.WifiScanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by Rahul on 21/4/15.
 */

//Change what you want to do on alarm receive in this class
public class AlarmActivity extends BroadcastReceiver{
    private static WifiScanner ws;
    private Context ctx;
    int uniqueid;
    double lat,lon;
    int MAX_RADIUS = 200; //meters
    double MIN_WIFI_SCORE = 0.3;
    int MAX_PEOPLE = 1;
    static int currentPeople = 0;

    String TAG = "MyDebug: ";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "OnReceive");
        String type = intent.getStringExtra("type");
        if(type.equals("alarm")) {
            uniqueid = intent.getIntExtra("uniqueid", -1);
        }
        ctx = context;
        Thread t=null;
        if(type.equals("alarm")) {
            Log.d(TAG, "UniqueID: " + String.valueOf(uniqueid));
            currentPeople = 0;
            ringAlarm(context);
            t = new Thread(new requestFriendsLocation());
            t.start();
        }
        else if(type.equals("stopalarm")) {
            if(t!=null) {
                Log.i(TAG,"stopping");
//                t.stop();
            }
        }
        else if(type.equals("callresponse")) {
            if(intent.getStringExtra("value").equals("yes")) {
                NotificationManager notificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(0);
                Log.d(TAG,"To: "+intent.getStringExtra("sender")+" Sending msg: "+intent.getStringExtra("res"));
                SendMessage sendMessage = new SendMessage(ctx, intent.getStringExtra("sender"), intent.getStringExtra("res"));
                sendMessage.execute();
                Log.d(TAG,"YES");
            }
            else {
                NotificationManager notificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(0);
                Log.d(TAG,"NOOOOO");
            }
        }
        else if(type.equals(context.getString(R.string.Return_Distance))) {
            returnDistance(intent);
        }
        else if(type.equals(context.getString(R.string.Receive_Distance))) {
            receiveDistance(intent);
        }
        else if(type.equals(context.getString(R.string.Call_Person))) {
            callPerson(intent);
        }

    }

    void ringAlarm(Context context) {
        Log.d("MyDebug","Ringing Alarm");

       Intent intent=new Intent(context,alarmDialog.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void callPerson(Intent intent) {
        try {
            JSONObject jo = new JSONObject(intent.getStringExtra("message"));
            if(jo.getString("value").equals("yes")) {
                Log.d(TAG,"COMING!");
                Notification noti = new Notification.Builder(ctx)
                        .setContentTitle("Please go and wake your friend!")
                        .setContentText("Wake up request").setSmallIcon(R.drawable.icon).build();
                NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(ctx.NOTIFICATION_SERVICE);
                // hide the notification after its selected
                noti.flags |= Notification.FLAG_AUTO_CANCEL;

                notificationManager.notify(0, noti);

            }
            else {
                Log.d(TAG,"NOT COMING!");
                Notification noti = new Notification.Builder(ctx)
                        .setContentTitle("Take rest! You don't need to come")
                        .setContentText("Thanks").setSmallIcon(R.drawable.icon).build();
                NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(ctx.NOTIFICATION_SERVICE);
                // hide the notification after its selected
                noti.flags |= Notification.FLAG_AUTO_CANCEL;

                notificationManager.notify(0, noti);
            }
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(ctx, notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch(Exception e)
        {
            Log.d(TAG,"JSONERROR");
            e.printStackTrace();
        }
    }

    void receiveDistance(Intent intent) {
        try {
            JSONObject jo = new JSONObject(intent.getStringExtra("message"));
            JSONObject res = new JSONObject();
            if(jo.getString("location").equals("true")) {
                if(jo.getDouble("distance")>MAX_RADIUS) {
                    Log.d(TAG,"Distance check fail");
                    return;
                }
            }
            else if(jo.getString("wifi").equals("true")) {
                if(jo.getDouble("wifiscore")<0.5) {
                    Log.d(TAG,"Wifi check fail");
                    return;
                }
            }
            else {
                Log.d(TAG,"NO DATA AVAIlABLE");
                return;
            }
            SharedPreferences preferences = ctx.getSharedPreferences(ctx.getString(R.string.Saved_Phone_Number), Context.MODE_PRIVATE);
            String mobile = preferences.getString(ctx.getString(R.string.mobile), "");
            res.put("from",mobile);
            res.put("type", ctx.getString(R.string.Call_Person));
            res.put("randCheck",uniqueid);
            if(currentPeople<MAX_PEOPLE) {
                ++currentPeople;
                res.put("value","yes");
            }
            else {
                res.put("value","no");
            }
            SendMessage sendMessage = new SendMessage(ctx, jo.getString("from"), res.toString());
            sendMessage.execute();
            Toast.makeText(ctx,"Received | "+jo.getString("from"),Toast.LENGTH_LONG).show();
        }catch (Exception e)
        {
            Log.d(TAG,"RECEIVERROR");
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void returnDistance(Intent intent) {
        try {
            JSONObject jo = new JSONObject(intent.getStringExtra("message"));
            uniqueid = jo.getInt("randCheck");
            Log.d(TAG, "uniq: " + uniqueid);
            final String sender = jo.getString("from");
            SharedPreferences preferences = ctx.getSharedPreferences(ctx.getString(R.string.Saved_Phone_Number), Context.MODE_PRIVATE);
            String mobile = preferences.getString(ctx.getString(R.string.mobile), "");
            Log.d(TAG, "MOBILE: " + mobile);
            final JSONObject res = new JSONObject();
            res.put("randCheck", uniqueid);
            res.put("type",ctx.getString(R.string.Receive_Distance));
            res.put("from", String.valueOf(mobile));
            res.put("wifi","false");
            res.put("location","false");
            ws = new WifiScanner(ctx);
//            Thread.sleep(5000);
            Log.d(TAG,"location value: "+jo.getString("location"));
            Log.d(TAG,"gps value: " + GPSTracker.canGetLocation);
            if(jo.getString("location").equals("true")&&GPSTracker.canGetLocation) {
                double latitude = Double.parseDouble(jo.getString("latitude"));
                Log.d(TAG, "lat: " + latitude);
                double longitude = Double.parseDouble(jo.getString("longitude"));

                Location l1 = new Location("google");
                    l1.setLatitude(latitude);
                    l1.setLongitude(longitude);
                    Location l2 = GPSTracker.location;
                    Log.d(TAG, "mylat: " + l2.getLatitude() + " myLong: " + l2.getLongitude());
                    double distance = l2.distanceTo(l1);
                    Toast.makeText(ctx, "from: " + sender + " | Score: " + String.valueOf(distance) + " | lat/lon: " + latitude + "/" + longitude, Toast.LENGTH_LONG).show();
                    if(distance>MAX_RADIUS) {
                        Log.d(TAG,"NOT IN RADIUS");
                        return;
                    }
                    res.remove("location");
                    res.put("location","true");
                    res.put("distance", String.valueOf(distance));
            }
            else if(jo.getString("wifi").equals("true")) {
                int count = 10;
                Log.d(TAG,"Getting Wifi");
                while(!ws.canGetWifi&&count>0) {
                    count--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG,"Wifi Value: "+ws.canGetWifi);
                if(ws.canGetWifi) {
                    double score = 0, match = 0;
                    ArrayList<String> mywlist = ws.WiFiNetworks;
                    JSONArray jwlist =  jo.getJSONArray("wifilist");
                    ArrayList<String> wlist = new ArrayList<String>();
                    for (int i=0;i<jwlist.length();i++) {
                        wlist.add(jwlist.get(i).toString());
                    }
                    int mywlsize = mywlist.size();
                    for(String w: wlist) {
                        if(mywlist.contains(w)) {
                            mywlist.remove(w);
                            for(String x : mywlist) {
                                Log.d(TAG,"wifi: "+x);
                            }
                            match++;
                        }
                    }
                    score = match/(wlist.size()+mywlsize-match);
                    if(score<MIN_WIFI_SCORE) {
                        Log.d(TAG,"NO WIFI MATCH");
                        return;
                    }
                    res.remove("wifi");
                    res.put("wifi","true");
                    res.put("wifiscore",String.valueOf(score));
                    Log.d(TAG,"Wifi Score: "+score);
                }
            }
            else {
                Log.d(TAG,"NO LOCATION AVAILABLE");
                return;
            }
            Intent intent1 = new Intent(ctx, AlarmActivity.class);
            intent1.putExtra("uniqueid",uniqueid);
            intent1.putExtra("type","callresponse");
            intent1.putExtra("value","yes");
            intent1.putExtra("sender",jo.getString("from"));
            Log.d(TAG,"FROM NOTIFICATION SENDER: "+
                    jo.getString("from"));
            intent1.putExtra("res",res.toString());
            PendingIntent pIntent = PendingIntent.getBroadcast(ctx, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT);

            Intent intent2 = new Intent(ctx, AlarmActivity.class);
            intent2.putExtra("uniqueid",uniqueid);
            intent2.putExtra("type","callresponse");
            intent2.putExtra("value","no");
            PendingIntent pIntent2 = PendingIntent.getBroadcast(ctx, 0, intent2, 0);

            // Build notification
            // Actions are just fake
            Notification noti = new Notification.Builder(ctx)
                    .setContentTitle("Your friend is asleep nearby.")
                    .setContentText("Would you like to go wake him up?").setSmallIcon(R.drawable.icon)
                    .addAction(R.drawable.tick, "Yes", pIntent)
                    .addAction(R.drawable.tick, "No", pIntent2).build();
            NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(ctx.NOTIFICATION_SERVICE);
            // hide the notification after its selected
            noti.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(0, noti);
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(ctx, notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            Log.d(TAG,"JSONERROR");
            e.printStackTrace();
        }
//        gps.stopUsingGPS();
    }

    class requestFriendsLocation implements Runnable {
        @Override
        public void run(){
            try {
                Thread.sleep(20000);
            }catch(Exception e) {
                e.printStackTrace();
            }
            JSONParser json = new JSONParser();
            ws = new WifiScanner(ctx);
            String url = ctx.getString(R.string.SERVER_IP) + ctx.getString(R.string.Find_Nearest_Page);
            SharedPreferences sharedpreferences = ctx.getSharedPreferences(ctx.getString(R.string.Saved_Phone_Number),Context.MODE_PRIVATE) ;
            String mobileNumber = sharedpreferences.getString(ctx.getString(R.string.mobile),"") ;
            Log.d(TAG,"mobile: "+mobileNumber);
            if (mobileNumber != null && !mobileNumber.toLowerCase().equals("")) {
                try {
                    Log.d(TAG,"Mobile found: " + mobileNumber) ;
                    FriendsDatabase fd = new FriendsDatabase(ctx);
                    fd.open();
                    ArrayList<String> arr = fd.getNumbers();
                    fd.close();
                    JSONObject jo = new JSONObject();
                    jo.put("type",ctx.getString(R.string.Return_Distance));
                    jo.put("from", mobileNumber);
                    jo.put("randCheck", String.valueOf(uniqueid));
//                    gps = new GPSTracker(ctx);
                    if (GPSTracker.canGetLocation) {
                        jo.put("location","true");
                        jo.put("latitude", GPSTracker.latitude);
                        jo.put("longitude", GPSTracker.longitude);
                    }
                    else {
                        jo.put("location","false");
                    }
                    int count = 5;
                    while(!ws.canGetWifi&&count>0) {
                        count--;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(ws.canGetWifi) {
                        jo.put("wifi","true");
                        jo.put("wifilist",new JSONArray(ws.WiFiNetworks));
                    }
                    else {
                        jo.put("wifi","false");
                    }
                    Log.d("MyDebug", "message: " + jo.toString());
                    MultiSendMessage msm = new MultiSendMessage(ctx,arr,jo.toString());
                    msm.execute();
                    Log.d(TAG, "SENT LOCATION");
                } catch(Exception e) {
                    Log.e(TAG,"JSON parsing error");
                    e.printStackTrace();
                }

            }
            else {

            }
//            gps.stopUsingGPS();
        }
    }



}