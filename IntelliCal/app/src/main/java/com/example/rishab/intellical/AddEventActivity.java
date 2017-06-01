package com.example.rishab.intellical;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AddEventActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Intent intent = getIntent();

        TextView head = (TextView) findViewById(R.id.AddPrompt);
        head.setText(intent.getStringExtra("CalID"));
    }
}
