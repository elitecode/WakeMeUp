package com.iitropar.rahul.wakeupalarm.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

/**
 * Created by tangbang on 3/23/2015.
 */
public class AlertDialogManager {

    public void showAlertDialog(final Context context, String title, String message,Boolean status){
        Log.d("RamRam","here") ;
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        Log.d("RamRam","here1") ;
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        Log.d("RamRam","here2") ;
        if (status != null){
            Log.d("RamRam","here3") ;
            if(status){
//                maybe used later for adding images
//                alertDialog.setIcon(R.drawable.success);
            }
            else{
//                maybe used later for adding images in case of failure
//                alertDialog.setIcon(R.drawable.fail);
            }
        }
        Log.d("RamRam","her4") ;
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("RamRam", "here5") ;
                System.exit(0);
            }
        });
        Log.d("RamRam","here5") ;
        alertDialog.show();
        Log.d("RamRam","here6") ;
    }

}
