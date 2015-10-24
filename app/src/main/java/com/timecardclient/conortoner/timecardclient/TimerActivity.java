package com.timecardclient.conortoner.timecardclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.MediaRouteButton;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimerActivity extends AppCompatActivity {

    private static final String LOG_TAG = "TimerActivity";
    private static final String OUTPUT_FILENAME = "results.csv";
    private boolean timerStarted = false;
    private Handler timerHandler = new Handler();
    private long startTime;
    private TextView timer;
    private FloatingActionButton saveFab;
    private TimerActivity thisActivity;
    private long stopTime;
    private EditText carNumber;
    private NumberPicker penaltyPicker;
    private Switch wrongTest;
    private ArrayDeque<String> retryQue = new ArrayDeque<>();
    private Handler reQueHandler = new Handler();
    private String host = null;
    private String marshalName = null;
    private String curentLayout = null;





    Runnable reQue = new Runnable() {
        @Override
        public void run() {
            if(retryQue.size()>0){
                retryFailedSaves();
            }
            reQueHandler.postDelayed(this, 20000);
        }
    };


    private void retryFailedSaves() {
        Log.e(LOG_TAG, "retrying network calls, que size: "+retryQue.size());
        new HttpRequestTask(retryQue.pop()).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        thisActivity = this;

        timer = (TextView) findViewById(R.id.timer);
        saveFab = (FloatingActionButton) findViewById(R.id.saveFab);
        carNumber = (EditText) findViewById(R.id.carNumber);
        penaltyPicker= (NumberPicker) findViewById(R.id.penaltyPicker);
        wrongTest = (Switch)findViewById(R.id.wTSwitch);

        String[] nums = new String[21];
        for(int i=0; i<nums.length; i++)
            nums[i] = Integer.toString(i);

        penaltyPicker.setMinValue(1);
        penaltyPicker.setMaxValue(20);
        penaltyPicker.setWrapSelectorWheel(false);
        penaltyPicker.setDisplayedValues(nums);
        penaltyPicker.setValue(0);
        readFromFile();
        reQueHandler.postDelayed(reQue, 0);
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
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void openSettings() {
        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);

        i.putExtra("hostLocation",host);
        i.putExtra("marshalName",marshalName);
        i.putExtra("layout", curentLayout);

        startActivityForResult(i, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    host = data.getStringExtra("hostLocation");
                    marshalName = data.getStringExtra("marshalName");
                    curentLayout = data.getStringExtra("layout");
                }
                break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(getString(R.string.startTimeLable), startTime);
        savedInstanceState.putLong(getString(R.string.stopTimeLable), stopTime);
        savedInstanceState.putBoolean(getString(R.string.timerStateLable), timerStarted);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        startTime = savedInstanceState.getLong(getString(R.string.startTimeLable));
        stopTime = savedInstanceState.getLong(getString(R.string.stopTimeLable));
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
            stopTime = System.currentTimeMillis();
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
                .setTitle("Saving TimeCard")
                .setMessage("Do you really want to save this timecard?")
                .setIcon(android.R.drawable.ic_menu_save)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(TimerActivity.this, "Saving....", Toast.LENGTH_SHORT).show();
                        JSONObject JsonPayload = createPayload();
                        writeToFile(JsonPayload.toString());
                        new HttpRequestTask(JsonPayload.toString()).execute();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private JSONObject createPayload() {
        JSONObject payload = new JSONObject();
        try {
            payload.put("layout","1");
            payload.put("startTime",startTime);
            payload.put("endTime",stopTime);
            payload.put("wrongTest", wrongTest.isChecked());
            payload.put("penalty", penaltyPicker.getValue());
            payload.put("carNumber", carNumber.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payload;
    }


    private class HttpRequestTask extends AsyncTask<String, String, String> {

        private Exception exception;
        private Activity currentActivity;
        private String payload;

        public HttpRequestTask(String inPayload){
            payload = inPayload;
        }

        protected String doInBackground(String... urls) {
            try {
                return "" + testHttpPost();
            } catch (Exception e) {
                this.exception = e;
                Log.e(LOG_TAG, "network exception", e);
                return null;
            }
        }

        private int testHttpPost() throws IOException {
            URL url = new URL("http://192.168.224.236/:8080/result");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            OutputStream out = null;
            int responseCode = 0;
            String responseMessage;
            try {
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);

                out = new BufferedOutputStream(urlConnection.getOutputStream());

                out.write(payload.getBytes("UTF-8"));
                Log.e(LOG_TAG, "payload :" + payload);
                out.flush();

                responseMessage = urlConnection.getResponseMessage();
                responseCode = urlConnection.getResponseCode();

                Log.e(LOG_TAG, responseCode+":"+responseMessage);

            } finally {
                urlConnection.disconnect();
            }
            return responseCode;
        }

        protected void onPostExecute(String result) {
            thisActivity.saveCallback(result, payload);
        }
    }

    private void saveCallback(String result, String payload) {
        Log.d(LOG_TAG, "Result from save callback: " + result);
        if(result == null || result.isEmpty() || result.charAt(0) != '2'){
            addToRetryQue(payload);
        }else {
            Toast.makeText(TimerActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToRetryQue(String payload) {
        retryQue.push(payload);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private File getFileForStorage() {
        // Get the directory for the user's public pictures directory.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_");
        File extStore = Environment.getExternalStorageDirectory();
        File file = new File(String.format("%s/Download/%s%s",extStore.getAbsolutePath(), sdf.format(new Date()), OUTPUT_FILENAME));
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.d(LOG_TAG, String.format("Problem creating result file: %s", file.getName()), e);
            }
        }
        return file;
    }

    private void writeToFile(String result) {
        if (isExternalStorageWritable()) {
            File file = getFileForStorage();
            FileWriter fileWriter = null;
            try {
                Log.d(LOG_TAG,"Trying to write to file: " + file.getAbsolutePath());
                fileWriter = new FileWriter(file, true);
                fileWriter.write(result);
            } catch (IOException e) {
                Log.e(LOG_TAG, String.format("Something went wrong writing to the file: %s", file.getName()), e);
            } finally {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG,"Something went wrong writing file",e);
                }
            }
        } else {
            Log.e(LOG_TAG, "External storage isn't writable...:-(");
        }
    }

    private List<String> readFromFile(){
        List<String> result = new ArrayList<>();
        BufferedReader bufferedReader = null;
        if(isExternalStorageReadable()){
            File file = getFileForStorage();
            try {
                FileReader fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);
                for(String line = bufferedReader.readLine(); line!=null; line = bufferedReader.readLine()){
                    Log.d(LOG_TAG,"Read line from file: "+line);
                    result.add(line);
                }
            } catch (FileNotFoundException e) {
                Log.e(LOG_TAG,"Problem reading results from file: " + file.getAbsolutePath(),e);
            } catch (IOException e) {
                Log.e(LOG_TAG,"Problem reading results from file: " + file.getAbsolutePath(),e);
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG,"Problem closing file",e);
                }
            }
        }
        return result;
    }
}
