package io.development.tymo;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;

import java.io.InputStream;
import java.util.ArrayList;

import io.development.tymo.utils.ActivityJobCreator;

public class TymoApplication extends MultiDexApplication {
    private static TymoApplication singleton;
    private InputStream inputStream = null;
    private ArrayList<Integer> date = null;
    private boolean created_activity = false;

    private double lat = -500, lng = -500;

    /*public static RefWatcher getRefWatcher(Context context) {
        TymoApplication application = (TymoApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;*/

    public static TymoApplication getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        Stetho.initializeWithDefaults(this);

        JobManager.create(this).addJobCreator(new ActivityJobCreator());
    }

    public void setCreatedActivity(boolean created){
        created_activity = created;
    }

    public boolean isCreatedActivity(){
        return created_activity;
    }

    public void setDate(ArrayList<Integer> dt){
        date = dt;
    }

    public ArrayList<Integer> getDate(){
        return date;
    }

    public void setInputStreamer(InputStream is){
        inputStream = is;
    }

    public InputStream getInputStreamer(){
        return inputStream;
    }

    public void setLatLng(double latitude, double longitute){
        lat = latitude;
        lng = longitute;
    }

    public  ArrayList<Double> getLatLng(){
        ArrayList<Double> list = new ArrayList<>();
        list.add(lat);
        list.add(lng);
        return list;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
