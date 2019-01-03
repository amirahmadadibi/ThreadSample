package com.example.android.concurrency;

import android.util.Log;
//*** google android developers practise
//*** https://developer.android.com/reference/java/lang/Runnable
//*** The Runnable interface should be implemented by any class whose instances are intended to be executed by a thread.
//The class must define a method of no arguments called run.
public class BackgroundTask implements Runnable {
    public static final String TAG = "coderunner";
    private int threadNumber;

    public BackgroundTask(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    @Override
    public void run() {
        Log.i(TAG, Thread.currentThread().getName() + "start , trhead number = " + threadNumber);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, Thread.currentThread().getName() + "end , trhead number = " + threadNumber);

    }
}
