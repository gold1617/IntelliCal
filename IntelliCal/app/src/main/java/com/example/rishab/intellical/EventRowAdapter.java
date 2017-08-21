package com.example.rishab.intellical;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by rishab on 8/15/17.
 */

public class EventRowAdapter extends ArrayAdapter<EventRowDataModel> implements View.OnClickListener
{
    private ArrayList<EventRowDataModel> dataSet;
    Context mContext;
    private AddEventActivity timeContext;

    //    private static class ViewHolder
//    {
//        EditText name;
//        EditText duration;
//        EditText start;
//    }

    public EventRowAdapter(ArrayList<EventRowDataModel> data, Context context)
    {
        super(context, R.layout.event_row, data);
        dataSet = data;
        mContext = context;
    }

    @Override
    public void onClick(View v)
    {
        final int position = (Integer) v.getTag();
        Log.d("position", "" + position);

        switch (v.getId())
        {
            case R.id.EventDelete:
                remove(getItem(position));
                break;
            case R.id.EventStartText:
                Log.d("Time","Hm");

                break;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        final EventRowDataModel dataModel = getItem(position);

        if(convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.event_row,parent, false);
            dataModel.setName((EditText) convertView.findViewById(R.id.EventNameText));
            dataModel.setDuration((EditText)convertView.findViewById(R.id.EventDurationText));
            dataModel.setStart((EditText) convertView.findViewById(R.id.EventStartText));
            dataModel.getStart().setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v)
                {
//                    if(hasFocus)
//                    {
                        Calendar mcurrentTime = Calendar.getInstance();
                        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                        int minute = mcurrentTime.get(Calendar.MINUTE);
                        TimePickerDialog mTimePicker;
                        mTimePicker = new TimePickerDialog(timeContext, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                dataModel.start.setText( selectedHour + ":" + selectedMinute);
                            }
                        }, hour, minute,false);
                        mTimePicker.setTitle("Select Time");
                        mTimePicker.show();
//                    }
                }
            });

        }
        ImageView delete = (ImageView) convertView.findViewById(R.id.EventDelete);
        delete.setOnClickListener(this);
        delete.setTag(position);

        return convertView;
    }

    public void setTimeContext(AddEventActivity tc)
    {
        timeContext = tc;
    }
}
