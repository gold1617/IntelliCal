package com.example.rishab.intellical;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.CookieStore;
import org.apache.http.protocol.HttpContext;

import java.sql.Time;
import java.util.ArrayList;

public class AddEventActivity extends AppCompatActivity
{
    private static ArrayList<EventRowDataModel> events;
    private static EventRowAdapter adapter;
    private static String serverauth;
    private static String calID;

    private ListView eventListView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Intent intent = getIntent();
        if(intent.getStringExtra("ServerAuth") != null)
            serverauth = intent.getStringExtra("ServerAuth");

        if(intent.getStringExtra("CalID") != null)
            calID = intent.getStringExtra("CalID");

        TextView head = (TextView) findViewById(R.id.addPrompt);
        head.setText(getString(R.string.event_prompt));

        if(events == null || adapter == null)
        {
            events = new ArrayList<EventRowDataModel>();
            events.add(new EventRowDataModel());
            adapter = new EventRowAdapter(events,getApplicationContext());
        }

        adapter.setTimeContext(AddEventActivity.this);
        eventListView = (ListView) findViewById(R.id.listView);
        eventListView.setAdapter(adapter);

        ImageButton addEvent =(ImageButton) findViewById(R.id.addEventButton);
        addEvent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                events.add(new EventRowDataModel());
                adapter.notifyDataSetChanged();
            }
        });

        Button next = (Button) findViewById(R.id.nextButton);
        next.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ArrayList<ArrayList<String>> eventsStrings = new ArrayList<ArrayList<String>>();

                if(events.isEmpty())
                {
                    Toast.makeText(AddEventActivity.this, R.string.no_events, Toast.LENGTH_SHORT).show();
                    return;
                }

                for(EventRowDataModel event: events)
                {
                    if(event.getName().getText().toString().equals("") || event.getDuration().getText().toString().equals("") || event.getStart().getText().toString().equals(""))
                    {
                        Toast.makeText(AddEventActivity.this, R.string.missing_field, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ArrayList<String> eventString = new ArrayList<String>();
                    eventString.add(event.getName().getText().toString());
                    eventString.add(event.getDuration().getText().toString());
                    eventString.add(event.getStart().getText().toString());
                    eventsStrings.add(eventString);
                }
                Intent intent = new Intent(getApplication(),AddForDays.class);
                intent.putExtra("Events",eventsStrings);
                startActivity(intent);
            }
        });

    }
}
