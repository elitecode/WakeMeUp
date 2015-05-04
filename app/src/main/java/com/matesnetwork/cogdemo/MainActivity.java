package com.matesnetwork.cogdemo;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iitropar.rahul.wakeupalarm.R;
import com.iitropar.rahul.wakeupalarm.alarm.alarmRegister;
import com.iitropar.rahul.wakeupalarm.gps.GPSTracker;
import com.matesnetwork.callverification.Cognalys;
import com.matesnetwork.interfaces.VerificationListner;

public class MainActivity  extends Activity {
	private TextView timertv = null;
	private EditText phoneNumbTv = null;
	private CountDownTimer countDownTimer;
	private RelativeLayout timerLayout;
    private Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.phone_verify);
        context = this ;
		phoneNumbTv = (EditText) findViewById(R.id.ph_et);
		timertv = (TextView) findViewById(R.id.timer_tv);
//		TextView country_code_tv = (TextView) findViewById(R.id.country_code_tv);
//		country_code_tv.setText(Cognalys.getCountryCode(getApplicationContext()));
		timerLayout = (RelativeLayout) findViewById(R.id.timer_rl);
		
		findViewById(R.id.verifybutton).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {

                        ////internet
                        if(!isNetworkAvailable()){
                            showInternetDisabledAlertToUser();
                        }
                        else{

                            if (!TextUtils.isEmpty(phoneNumbTv.getText().toString())) {
                                try {
                                    final String phnN = phoneNumbTv.getText().toString();
                                    double num = Double.parseDouble(phnN);
                                    Log.d("MyDebug: ",num+" is a number");
                                    int len=phnN.toCharArray().length;
                                    if(len==10){
                                        Log.d("MyDebug: ",num+"is valid");
                                        verify();
                                    }
                                    else
                                        Toast.makeText(getApplicationContext(), "ERROR! Please enter valid phone number ", Toast.LENGTH_LONG).show();
                                } catch (NumberFormatException e) {
                                    Log.d("MyDebug: ","is not a number");
                                    Toast.makeText(getApplicationContext(), "ERROR!!! Please enter only digits in phone number ", Toast.LENGTH_LONG).show();

                                }
                            }else{
                                Toast.makeText(getApplicationContext(), "Please enter your phone number to verify", Toast.LENGTH_LONG).show();
                            }

                        }

						
					}
				});
	}




    public void showInternetDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Device is not connected to internet. Connect it now?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Connect To Internet",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callInternetSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_SETTINGS);
                                startActivity(callInternetSettingIntent);
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


    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

	private void verify() {
		timerLayout.setVisibility(View.VISIBLE);
		countDownTimer = new CountDownTimer(60000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				timertv.setText("" + millisUntilFinished / 1000);
			}

			@Override
			public void onFinish() {
				timerLayout.setVisibility(View.GONE);
			}

		};
		countDownTimer.start();
		final String phnN = phoneNumbTv.getText().toString();
        Log.d("TAG: ","rr" + phnN + "alksd") ;

        Cognalys.verifyMobileNumber(MainActivity.this, "5d4430be8c4cfef5a06e8607fa6a527abc23c6ba", "155521c0e3e24e51a57de1a", phoneNumbTv.getText().toString(), new VerificationListner() {

            @Override
            public void onVerificationStarted() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onVerificationSuccess() {
                countDownTimer.cancel();
                timerLayout.setVisibility(View.GONE);
//                showAlert("Your number has been verified\n\nThanks!!", true);
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.Saved_Phone_Number),MODE_PRIVATE) ;

                if (sharedPreferences != null){
                    SharedPreferences.Editor editor = sharedPreferences.edit() ;
                    editor.putString(getString(R.string.mobile),phnN) ;
                    editor.commit() ;
                    alarmRegister ar = new alarmRegister(MainActivity.this, phnN);
                    finish();
                    return ;
                }
            }

            @Override
            public void onVerificationFailed(ArrayList<String> errorList) {
                countDownTimer.cancel();
                timerLayout.setVisibility(View.GONE);
                for (String error : errorList) {
                    Log.d("abx", "error:" + error);
                }
                showAlert("Something went wrong.\n please try again", false);
            }
        });
	}

    private void showAlert(String message,boolean status){
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);


        ImageView mImageView = (ImageView) dialog.findViewById(R.id.verify_im);
        TextView messageTv=(TextView) dialog.findViewById(R.id.messagetv);
        if (status) {
            mImageView.setImageResource(R.drawable.blue_tick);
        }else{
            mImageView.setImageResource(R.drawable.wrong);
        }

        messageTv.setText(message);
        dialog.show();
    }

    private String checkIfPhoneNumberAlreadyRegistered(Context context){
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.Saved_Phone_Number),Context.MODE_PRIVATE) ;
        String phoneNumber = sharedPreferences.getString(getString(R.string.Phone_number),"") ;
        if (phoneNumber == null || phoneNumber.equals("")){
            return null ;
        }
        return phoneNumber ;
    }
}