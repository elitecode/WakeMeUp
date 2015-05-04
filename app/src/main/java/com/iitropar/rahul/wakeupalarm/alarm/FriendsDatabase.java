package com.iitropar.rahul.wakeupalarm.alarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sds on 12/4/15.
 */
public class FriendsDatabase {

    private dbhelper helper;
    private final Context context;
    private SQLiteDatabase mydb;

    public FriendsDatabase(Context context) {
        this.context = context;
    }



    private static class dbhelper extends SQLiteOpenHelper{

        public dbhelper(Context context) {
            super(context, "friendsdb", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE friends (" +
                    "phnumber varchar(20),"+
                    "name varchar(50)"+
                    ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS friends;");
            onCreate(db);
        }
    }

    public FriendsDatabase open()
    {
        helper=new dbhelper(context);
        mydb=helper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        helper.close();
    }



    public long insertEntry(String num,String name) {
        ContentValues cv=new ContentValues();
        cv.put("phnumber",num);
        cv.put("name",name);
        return mydb.insert("friends",null,cv);
    }





    public void deleteEntry(String phnumber) {
        mydb.delete("friends","phnumber=\""+phnumber+"\"",null);
    }




    public List getNames() {

        String[] columns=new String[]{"phnumber","name"};
        Cursor c=mydb.query("friends",columns,null,null,null,null,null);

        List<String> result = new ArrayList<String>();

        int iname=c.getColumnIndex("name");


        int i=0;
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            String res=c.getString(iname);
            result.add(res);
            i++;
        }

        return result;
    }

    public ArrayList<String> getNumbers() {

        String[] columns=new String[]{"phnumber","name"};
        Cursor c=mydb.query("friends",columns,null,null,null,null,null);

        ArrayList<String> result = new ArrayList<String>();

        int inum=c.getColumnIndex("phnumber");


        int i=0;
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            String res=c.getString(inum);
            result.add(res);
            i++;
        }

        return result;
    }



}
