package com.example.android.concurrency;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutor.shutdown();
    }

    //Run some code called from the onClick event in the layout file
    public void runCode(View v) {
        //now I'm running 3 background task at a time sequentially and
        //synchronously within background class
        //all aysnc task have hone worker thread or in other words android uses just one thread
        //for all asyncTasks


        //makeing sure asynctask object exists and running to cancled
        if(mTaskRunning && myTask != null){
            myTask.cancel(true);
            mTaskRunning = false;
        }else{
            myTask = new MyTask();
            myTask.execute("Red", "Green", "Blue");
            mTaskRunning = true;
        }
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
                if(isCancelled()){
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
}