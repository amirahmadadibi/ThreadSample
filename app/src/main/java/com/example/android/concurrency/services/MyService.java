package com.example.android.concurrency.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
    public static final String TAG = "service";
    //filed that returns binder object
    private final Binder mBinder = new ServiceBinder();
    public MyService() {
        Log.i(TAG, "MyService: service created");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: bound service");
    }

    //returns implementation of IBinder Interface our job is
    // construct this object so we can return it
    //i can call this method from else where of the application
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: bound service");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind: bound service");
        return super.onUnbind(intent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: bound service");
    }


    //a class that extends Binder
    public class ServiceBinder extends Binder{
        public MyService getService(){
            return MyService.this;
        }
    }
}
