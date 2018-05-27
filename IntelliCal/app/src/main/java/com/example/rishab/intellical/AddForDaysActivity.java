package com.example.rishab.intellical;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.protocol.HttpContext;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class AddForDaysActivity extends AppCompatActivity
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

        if(intent.getSerializableExtra(("Events")) != null)
            events = (ArrayList<ArrayList<String>>) intent.getSerializableExtra("Events");

        final EditText days = (EditText) findViewById(R.id.daysText);
        HttpContext localContext = SelectCalendarActivity.getLocalContext();

        Button addAll = (Button) findViewById(R.id.addAll);
        addAll.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(days.getText().toString().equals("") || Integer.parseInt(days.getText().toString()) <= 0)
                {
                    Toast.makeText(AddForDaysActivity.this, R.string.no_days, Toast.LENGTH_SHORT).show();
                    return;
                }

                Calendar calendar = Calendar.getInstance();
                TimeZone timeZone = calendar.getTimeZone();
                Long todayLong = System.currentTimeMillis()/1000;
                String today = todayLong.toString();

                Intent serviceIntent = new Intent(getBaseContext(),AddEventsService.class);
                serviceIntent.putExtra("ServerAuth", serverauth);
                serviceIntent.putExtra("CalID", calID);
                serviceIntent.putExtra("Events", events);
                serviceIntent.putExtra("TZ",timeZone.getID());
                serviceIntent.putExtra("Date",today);
                serviceIntent.putExtra("Days", days.getText().toString());
                startService(serviceIntent);

                Intent intent = new Intent(getApplication(),AddingActivity.class);
                startActivity(intent);
            }
        });

    }

}