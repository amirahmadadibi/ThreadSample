package com.example.android.concurrency;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.android.concurrency.services.MyIntentService;
import com.example.android.concurrency.services.MyService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CodeRunner";
    private static final String MESSAGE_KEY = "message_key";
    private Handler mHandler;
    // View object references
    private ScrollView mScroll;
    private TextView mLog;
    private ProgressBar mProgressBar;
    ExecutorService mExecutor;
    MyTask myTask;
    private boolean mTaskRunning;
    private MyService mService;
    private final ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //service get's started and this method gets call and i initialize my service filed
            MyService.ServiceBinder serviceBinder = (MyService.ServiceBinder) service;
            mService = serviceBinder.getService();
            Log.i(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mService != null) {
                //cleaning up service
                mService = null;
            }
            Log.i(TAG, "onServiceDisconnected");
        }
    };
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(MESSAGE_KEY) != null) {
                Log.i(TAG, "onReceive: ");
                String message = intent.getStringExtra(MESSAGE_KEY);
                log(message);
            }

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        //Binding to Bound Service
        Intent serviceIntent = new Intent(this, MyService.class);
        //binding usage intent and connection,context
        //bind auto create means when i bind service create the service
        bindService(serviceIntent, mServiceCon, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "onStart bind to BoundService");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the logging components
        mScroll = (ScrollView) findViewById(R.id.scrollLog);
        mLog = (TextView) findViewById(R.id.tvLog);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mLog.setText(R.string.lorem_ipsum);
        //I want to create a pool of 5 threads
        mExecutor = Executors.newFixedThreadPool(5);
    }


    //Run some code called from the onClick event in the layout file
    public void runCode(View v) {
        MyIntentService.startActionFoo(this, "value1", "value2");
    }

    //  Clear the output, called from the onClick event in the layout file
    public void clearOutput(View v) {
        mLog.setText("");
        scrollTextToEnd();
    }

    //  Log output to logcat and the screen
    private void log(String message) {
        Log.i(TAG, message);
        mLog.append(message + "\n");
        scrollTextToEnd();
    }

    private void scrollTextToEnd() {
        mScroll.post(new Runnable() {
            @Override
            public void run() {
                mScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @SuppressWarnings("unused")
    private void displayProgressBar(boolean display) {
        if (display) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    //it's best used for task that hva one or two seconds live period
    //AsyncTask is sensitive when it's time to configuration changes
    class MyTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            for (String value :
                    strings) {
                if (isCancelled()) {
                    //when task is cancelled other methods do not gets run insted of onCancelled
                    break;
                }
                Log.i(TAG, "doInBackground: " + value);
                publishProgress(value);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //return finished data to onPostExecute Method
            return "it's finished";
        }

        //onProgress is touch with publishProgress method and runs on mainUiThread
        @Override
        protected void onProgressUpdate(String... values) {
            log(values[0]);
        }

        //we can only return single value after finishing job - runs on mainUiThread
        @Override
        protected void onPostExecute(String s) {
            log(s);
        }

        //- runs on mainUiThread
        @Override
        protected void onCancelled() {
            log("task cancelled");
        }

        //if we return vlue form doInBackground and We use this type of OnCancelled,
        //this version of onCancelled get's used
        //- runs on mainUiThread
        @Override
        protected void onCancelled(String s) {
            log("Cancelled With Result " + s);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unbindin from service
        unbindService(mServiceCon);
        Log.i(TAG, "onStop: unbind serivce");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutor.shutdown();
        //deregister broadcast receiver
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }

}