package com.iitropar.rahul.wakeupalarm.alarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sds on 12/4/15.
 */
public class AlarmDatabase {
    private static String TAG = "MyDebug" ;
    private dbhelper helper;
    private final Context context;
    private SQLiteDatabase mydb;

    public AlarmDatabase(Context context) {
        this.context = context;
    }



    private static class dbhelper extends SQLiteOpenHelper{

        public dbhelper(Context context) {
            super(context, "alarmsdb", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE alarms (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "hour INTEGER," +
                    "minutes INTEGER," +
                    "dateofAlarm varchar(20)," +
                    "uniqueid integer,"+
                    "latitude varchar(25)," +
                    "longitude varchar(25)," +
                    "address varchar(100)" +
                    ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS alarms;");
            onCreate(db);
        }
    }

    public AlarmDatabase open()
    {
        helper=new dbhelper(context);
        mydb=helper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        helper.close();
    }



    public long insertEntry(int hrs, int mins, String dateofAlarm,int uniqueid, Double latitude, Double longitude, String address) {
        ContentValues cv=new ContentValues();
        cv.put("hour",hrs);
        cv.put("minutes",mins);
        cv.put("dateofAlarm",dateofAlarm);
        cv.put("uniqueid",uniqueid);
        if (latitude!=null && !(String.valueOf(latitude).equals(""))) {
            cv.put("latitude", String.valueOf(latitude));
            Log.d(TAG,"adding latitude") ;
        }
        if (longitude!=null && !(String.valueOf(longitude).equals(""))) {
            cv.put("longitude", String.valueOf(longitude));
            Log.d(TAG,"adding longitude") ;
        }
        if (address != null && !address.equals("")){
            cv.put("address",address) ;
            Log.d(TAG,"adding address") ;
        }
        return mydb.insert("alarms",null,cv);
    }

    public void updateEntry(long uniqid,int hrs, int mins, String dateofAlarm, Double latitude, Double longitude,String address) {
        ContentValues cv=new ContentValues();
        cv.put("hour",hrs);
        cv.put("minutes",mins);
        cv.put("dateOfAlarm",dateofAlarm);
        if (latitude!=null && !(String.valueOf(latitude).equals(""))) {
            cv.put("latitude", String.valueOf(latitude));
            Log.d(TAG,"updating latitude") ;
        }
        if (longitude!=null && !(String.valueOf(longitude).equals(""))) {
            cv.put("longitude", String.valueOf(longitude));
            Log.d(TAG,"updating longitude") ;
        }
        if (address != null && !address.equals("")){
            cv.put("address",address);
            Log.d(TAG,"updating address") ;
        }
        mydb.update("alarms",cv,"uniqueid = "+uniqid,null);
    }

    public void deleteEntry(long uniqid) {
        mydb.delete("alarms","uniqueid="+uniqid,null);
    }

    public int getHour(long l)
    {
        String[] columns=new String[]{"id","hour","minutes","dateofAlarm","uniqueid"};
        Cursor c=mydb.query("alarms",columns,"uniqueid = "+l,null,null,null,null);
        if(c!=null)
        {
            c.moveToFirst();
            String hour=c.getString(1);
            return Integer.parseInt(hour);
        }
        return -1;
    }

    public int getMinutes(long l)
    {
        String[] columns=new String[]{"id","hour","minutes","dateofAlarm","uniqueid"};
        Cursor c=mydb.query("alarms",columns,"uniqueid = "+l,null,null,null,null);
        if(c!=null)
        {
            c.moveToFirst();
            String minutes=c.getString(2);
            return Integer.parseInt(minutes);
        }
        return -1;
    }

    public Pair<Double,Double> getLocation(long l){
        String[] columns=new String[]{"id","hour","minutes","dateofAlarm","uniqueid","latitude","longitude"};
        Log.d(TAG,"getting latitude") ;
        Cursor c=mydb.query("alarms",columns,"uniqueid = "+l,null,null,null,null);
        Pair<Double, Double> pair;
        if(c!=null)
        {
            c.moveToFirst();
            String lat=c.getString(5);
            String lon = c.getString(6) ;
            if (lat != null && lon != null && !lat.equals("") && !lon.equals("")) {
                pair = new Pair<Double,Double>(Double.valueOf(lat), Double.valueOf(lon)) ;
                return pair ;
            }else{
                return null;
            }
        }
        return null;
    }

    public String getAddress(long l){
        String[] columns=new String[]{"id","hour","minutes","dateofAlarm","uniqueid","latitude","longitude","address"};
        Log.d(TAG,"getting latitude") ;
        Cursor c=mydb.query("alarms",columns,"uniqueid = "+l,null,null,null,null);
        Pair<Double, Double> pair;
        if(c!=null)
        {
            c.moveToFirst();
            String address = c.getString(7) ;
            if (address != null && !address.equals("")) {
                return address ;
            }else{
                return null;
            }
        }
        return null;
    }

    public int getUniqueID(long l)
    {
        String[] columns=new String[]{"id","hour","minutes","dateofAlarm","uniqueid"};
        Cursor c=mydb.query("alarms",columns,"uniqueid = "+l,null,null,null,null);
        if(c!=null)
        {
            c.moveToFirst();
            String uniq=c.getString(4);
            return Integer.parseInt(uniq);
        }
        return -1;
    }

    public String getDate(long l)
    {
        String[] columns=new String[]{"id","hour","minutes","dateofAlarm","uniqueid"};
        Cursor c=mydb.query("alarms",columns,"uniqueid = "+l,null,null,null,null);
        if(c!=null)
        {
            c.moveToFirst();
            String date=c.getString(3);
            return date;
        }
        return null;
    }

    public List getIDs() {

       String[] cols=new String[]{"id","hour","minutes","dateofAlarm","uniqueid"};
        Cursor c=mydb.query("alarms",cols,null,null,null,null,null);

        ArrayList<Integer> result = new ArrayList<Integer>();

        int iuq=c.getColumnIndex("uniqueid");


        int i=0;
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            String res=c.getString(iuq);
            result.add(Integer.parseInt(res));
            i++;
        }

        return result;
    }


    public List getData() throws ParseException {

        String[] columns=new String[]{"id","hour","minutes","dateofAlarm","uniqueid"};
        Cursor c=mydb.query("alarms",columns,null,null,null,null,null);

        List<String> result = new ArrayList<String>();

        int iid=c.getColumnIndex("id");
        int ihr=c.getColumnIndex("hour");
        int imin=c.getColumnIndex("minutes");
        int idt=c.getColumnIndex("dateofAlarm");
        int iuq=c.getColumnIndex("uniqueid");

        Calendar currentcal=Calendar.getInstance();
        Calendar targetcal=Calendar.getInstance();

        int i=0;
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            String hr=c.getString(ihr);
            String min=c.getString(imin);
            if(hr.length()==1)
                hr="0"+hr;
            if(min.length()==1)
                min="0"+min;

            String date=c.getString(idt);
            int ind=date.indexOf('/');
            int day=Integer.parseInt(date.substring(0,ind))-1;
            int ind1=date.indexOf('/',ind+1);
            int month=Integer.parseInt(date.substring(ind+1,ind1))-1;
            int year=Integer.parseInt(date.substring(ind1+1,date.length()));
            targetcal.set(year,month,day,Integer.parseInt(hr),Integer.parseInt(min),00);
targetcal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(hr));
            Log.i("deepak",""+targetcal.compareTo(currentcal));
            Log.i("deepak tar",""+targetcal.toString());
            Log.i("deepak cur",""+currentcal.toString());


            String res;
            if(targetcal.compareTo(currentcal)<=0)
                res=hr+":"+min+" | "+c.getString(idt)+" | Not Active";
            else
                res=hr+":"+min+" | "+c.getString(idt)+" | Active";
            result.add(res);
            i++;
        }

        return result;
    }
}