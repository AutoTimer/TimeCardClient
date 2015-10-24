package com.timecardclient.conortoner.timecardclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;


public class SettingsActivity extends AppCompatActivity {

    private EditText host;
    private EditText marshal;
    private EditText layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        host = (EditText) findViewById(R.id.hostEditText);
        marshal = (EditText) findViewById(R.id.marshalEditText);
        layout = (EditText) findViewById(R.id.layoutEditText);

        host.setText(intent.getStringExtra("hostLocation"));
        marshal.setText(intent.getStringExtra("marshalName"));
        layout.setText(intent.getStringExtra("layout"));
    }

    public void onSaveSettings(View view){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("hostLocation",host.getText().toString());
        resultIntent.putExtra("marshalName",marshal.getText().toString());
        resultIntent.putExtra("layout", layout.getText().toString());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
