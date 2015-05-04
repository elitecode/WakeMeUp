package com.iitropar.rahul.wakeupalarm.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by tangbang on 3/23/2015.
 */
public class ConnectionDetector {
    private Context _context;
    public ConnectionDetector(Context context)
    {
        this._context = context;
    }
    private String TAG = "RamRam" ;

    public Boolean isConnectingToInternet(){
        Log.d("TAG",_context.toString()) ;
        ConnectivityManager connectivityManager = (ConnectivityManager)_context.getSystemService(Context.CONNECTIVITY_SERVICE) ;
        if (connectivityManager ==  null)
        {
            return false;
        }
        else
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo() ;
            if (info != null)
            {
                for (int i= 0 ; i <info.length ; ++i)
                {
                    Log.d(TAG,info[i].toString()) ;
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return false;
            }
        }
    }
}