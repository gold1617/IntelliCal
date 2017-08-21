package com.example.rishab.intellical;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.apache.http.protocol.HttpContext;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AddForDays extends AppCompatActivity
{
    private static ArrayList<ArrayList<String>> events;
    private static String serverauth;
    private static String calID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_for_days);

        Intent intent = getIntent();

        if(intent.getStringExtra("ServerAuth") != null)
            serverauth = intent.getStringExtra("ServerAuth");

        if(intent.getStringExtra("CalID") != null)
            calID = intent.getStringExtra("CalID");

        if(intent.getStringExtra(("CalID")) != null)
            events = (ArrayList<ArrayList<String>>) intent.getSerializableExtra("Events");

        HttpContext localContext = SelectCalendarActivity.getLocalContext();

    }

}
