package com.iitropar.rahul.wakeupalarm.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul on 25 Apr 2015.
 */
public class WifiScanner {
    private static String TAG = "MyDebug: ";
    private Context context = null;
    private static WifiManager wifi;
    private WifiReceiver wifiReceiver;
    public ArrayList<String> WiFiNetworks;
    public boolean canGetWifi = false;

    public WifiScanner(Context ctx)
    {
        this.context = ctx;
        GetWifiNetworks();
    }

    public void GetWifiNetworks()
    {
        WiFiNetworks = new ArrayList<String>();

        // Get a handle to the Wifi
        wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        // Start a scan and register the Broadcast receiver to get the list of Wifi Networks
        wifiReceiver = new WifiReceiver();
        context.getApplicationContext().registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifi.startScan();
        List<ScanResult> scanwifinetworks = wifi.getScanResults();
        WiFiNetworks.clear();
        for(ScanResult wifinetwork : scanwifinetworks)
        {
            WiFiNetworks.add(wifinetwork.SSID);
        }
        canGetWifi = true;
        Log.d(TAG,"New Wifi List");
    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            List<ScanResult> scanwifinetworks = wifi.getScanResults();
            WiFiNetworks.clear();
            for(ScanResult wifinetwork : scanwifinetworks)
            {
                WiFiNetworks.add(wifinetwork.SSID+wifinetwork.BSSID);
            }
            canGetWifi = true;
            Log.d(TAG,"New Wifi List");
        }
    }
}