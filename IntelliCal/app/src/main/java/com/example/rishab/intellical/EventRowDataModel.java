package com.example.rishab.intellical;

import android.widget.EditText;

import java.sql.Time;

/**
 * Created by rishab on 8/15/17.
 */

public class EventRowDataModel
{
    EditText name;
    EditText duration;
    EditText start;

    public void setName(EditText n)
    {
        name = n;
    }

    public void setDuration(EditText d)
    {
        duration = d;
    }

    public void setStart(EditText s)
    {
        start = s;
    }

    public EditText getName()
    {
        return name;
    }

    public EditText getDuration()
    {
        return duration;
    }

    public EditText getStart()
    {
        return start;
    }
}
