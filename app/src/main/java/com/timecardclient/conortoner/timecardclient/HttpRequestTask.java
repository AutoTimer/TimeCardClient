package com.timecardclient.conortoner.timecardclient;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestTask extends AsyncTask<String, String, String> {

    private Exception exception;

    protected String doInBackground(String... urls) {
        try {
            return "" + testHttpGet(urls[0]);
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }


    public int testHttpGet(String urlAsString) throws IOException {
        URL url = new URL(urlAsString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        int responseCode = urlConnection.getResponseCode();
        urlConnection.disconnect();
        return responseCode;
    }
    //new MakeHttpCallTask().execute("http://www.google.co.uk");
}

