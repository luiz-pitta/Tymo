/*
 * Copyright 2016 Manas Chaudhari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.development.tymo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.jude.easyrecyclerview.EasyRecyclerView;

import java.io.InputStream;
import java.util.ArrayList;

import io.development.tymo.adapters.PlansCardsAdapter;

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
