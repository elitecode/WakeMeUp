package com.iitropar.rahul.wakeupalarm.map;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.iitropar.rahul.wakeupalarm.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity {

    GoogleMap map;
    LatLng pointClicked ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent intent = getIntent();
        pointClicked = null ;

        SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        Button btn_select = (Button)findViewById(R.id.btn_draw);
        map = fm.getMap();
        map.setMyLocationEnabled(true);

        boolean ifLatLong = intent.getBooleanExtra("latlong",false) ;
        if (ifLatLong){
            Double latitide = intent.getDoubleExtra("latitude",0.0) ;
            Double longitude = intent.getDoubleExtra("longitude", 0.0) ;
            pointClicked = new LatLng(latitide,longitude) ;
            map.clear() ;
            MarkerOptions options = new MarkerOptions();
            options.position(pointClicked);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            map.addMarker(options);
        }

        map.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                map.clear() ;
                pointClicked = point ;
                MarkerOptions options = new MarkerOptions();
                options.position(point);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                map.addMarker(options);
            }
        });

        map.setOnMapLongClickListener(new OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                map.clear();
            }
        });

        btn_select.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if(pointClicked != null){

                    double latitude = pointClicked.latitude ;
                    double longitude = pointClicked.longitude ;
                    StringBuilder result = new StringBuilder();
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (addresses.size() > 0) {
                            Address address = addresses.get(0);
                            result.append(address.getAddressLine(0)).append(", ");
                            result.append(address.getAddressLine(1));
                        }
                    } catch (IOException e) {
                        Log.e("tag", e.getMessage());
                    }
                    String address = result.toString();
                    Toast.makeText(MainActivity.this,address,Toast.LENGTH_LONG).show();
                    Log.d("MyDebug","address: " + address) ;
                    Intent intent=new Intent();
                    intent.putExtra("latitude",latitude);
                    intent.putExtra("longitude",longitude);
                    intent.putExtra("address",address) ;
                    setResult(Integer.parseInt(getString(R.string.REQUEST_LOCATION)),intent);
                    setResult(RESULT_OK,intent);
                    finish();
                }
                else{
                    Toast.makeText(MainActivity.this,"Please select a point first",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}