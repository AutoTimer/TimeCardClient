package com.timecardclient.conortoner.timecardclient;

import android.app.AlertDialog;
import android.app.MediaRouteButton;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimerActivity extends AppCompatActivity {

    private static final String LOG_TAG = "TimerActivity";
    private static final String OUTPUT_FILENAME = "results.csv";
    private boolean timerStarted = false;
    private Handler timerHandler = new Handler();
    private long startTime;
    private TextView timer;
    private FloatingActionButton saveFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        timer = (TextView) findViewById(R.id.timer);
        saveFab = (FloatingActionButton) findViewById(R.id.saveFab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(getString(R.string.startTimeLable), startTime);
        savedInstanceState.putBoolean(getString(R.string.timerStateLable), timerStarted);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        startTime = savedInstanceState.getLong(getString(R.string.startTimeLable));
        timerStarted = savedInstanceState.getBoolean(getString(R.string.timerStateLable));
        if (timerStarted) {
            timerHandler.postDelayed(startTimer, 0);
        }
    }

    public void onStartStop(View view) {
        if (!timerStarted) {
            timerStarted = true;
            startTime = System.currentTimeMillis();
            timerHandler.removeCallbacks(startTimer);
            timerHandler.postDelayed(startTimer, 0);
            saveFab.setVisibility(View.INVISIBLE);
        } else {
            timerStarted = false;
            timerHandler.removeCallbacks(startTimer);
            saveFab.setVisibility(View.VISIBLE);
        }
    }

    Runnable startTimer = new Runnable() {
        @Override
        public void run() {
            long timerTime = System.currentTimeMillis() - startTime;
            updateTimer(timerTime);
            timerHandler.postDelayed(this, 10);
        }
    };

    private void updateTimer(long timerTime) {
        long seconds = timerTime / 1000;
        long milliS = (timerTime % 1000) / 10;
        String timerString;
        if (milliS < 10) {
            timerString = seconds + ":0" + milliS;
        } else {
            timerString = seconds + ":" + milliS;
        }
        timer.setText(timerString);
    }

    public void onSave(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("Do you really want to whatever?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(TimerActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private File getFileForStorage() {
        // Get the directory for the user's public pictures directory.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyddMM_");

        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), String.format("%s%s", sdf.format(new Date()), OUTPUT_FILENAME));
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    private void writeToFile(String result){
        File file = getFileForStorage();
        try {
            FileWriter fileWriter = new FileWriter(file,true);
        } catch (IOException e) {
            Log.e(LOG_TAG,String.format("Something went wrong writing to the file: %s",file.getName()),e);
        }
    }
}
