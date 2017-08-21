package com.example.rishab.intellical;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SelectCalendarActivity extends AppCompatActivity
{
    private static ArrayList<String> calendarNames;
    private static ArrayList<String> calendarIds;
    private static ArrayAdapter<String>  adapter;
    private static int load_status = 0;//0 = loading,1 = loaded, 2 = load failure
    private static String serverauth;
    private static String name;
    private static SelectCalendarActivity calActivity;
    private static CookieStore cookieStore;
    private static HttpContext localContext;

    private TextView text;
    private ListView calendarListView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_calendar);
        calActivity = this;

        Intent intent = getIntent();
        if(cookieStore == null || localContext == null)
        {
            cookieStore = new BasicCookieStore();
            localContext = new BasicHttpContext();
            localContext.setAttribute(ClientContext.COOKIE_STORE,cookieStore);
        }

        calendarListView = (ListView) findViewById(R.id.Calendars);

        if(intent.getStringExtra("ServerAuth") != null && !intent.getStringExtra("ServerAuth").equals(serverauth))
        {
            serverauth = intent.getStringExtra("ServerAuth");
            name = intent.getStringExtra("Name");
            calendarIds = new ArrayList<String>();
            calendarNames = new ArrayList<String>();
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, calendarNames);
            calendarListView.setAdapter(adapter);

            text = (TextView) findViewById(R.id.Header);
            text.setText(getString(R.string.cal_pick_loading, name));
            RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(getFragmentManager());
            retainFragment.loadAsync(serverauth,getString(R.string.calendar_url));
        }
        else
        {
            Log.v("CREATE",calendarNames.toString());
            calendarListView.setAdapter(adapter);

            text = (TextView) findViewById(R.id.Header);
            updateText();
        }

        calendarListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(getApplication(), AddEventActivity.class);
                intent.putExtra("ServerAuth",serverauth);
                intent.putExtra("CalID",calendarIds.get(position));
                startActivity(intent);
            }
        });
    }

    public static HttpContext getLocalContext()
    {
        return localContext;
    }

    public static void populateCalendars(JSONArray calendars)
    {
        if (calendars == null)
        {
            load_status = 2;
        }
        else
        {
            for (int i = 0; i < calendars.length(); i++)
            {
                JSONObject cal = null;
                try
                {
                    cal = calendars.getJSONObject(i);
                    calendarNames.add(cal.getString("summary"));
                    calendarIds.add(cal.getString("id"));
                    load_status = 1;
                }
                catch (JSONException e)
                {
                    Log.e("CAL",e.toString());
                    load_status = 2;
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    public static void updateText()
    {

        switch (load_status)
        {
            case 0: calActivity.text.setText(calActivity.getString(R.string.cal_pick_loading,name));
                break;
            case 1: calActivity.text.setText(calActivity.getString(R.string.cal_pick_loaded, name));
                break;
            case 2: calActivity.text.setText(calActivity.getString(R.string.cal_load_error));
                break;
        }


    }

    public static class RetainFragment extends Fragment
    {
        private static final String TAG ="RetainFragment";


        public RetainFragment() {}

        public static RetainFragment findOrCreateRetainFragment(FragmentManager fragmentManager)
        {
            RetainFragment fragment = (RetainFragment)fragmentManager.findFragmentByTag(TAG);
            if(fragment == null)
            {
                fragment = new RetainFragment();
                fragmentManager.beginTransaction().add(fragment,TAG).commit();
            }
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        private void loadAsync(String auth,String calendar_url)
        {
            new AsyncTask<String, Void, JSONObject>()
            {

                @Override
                protected JSONObject doInBackground(String... params)
                {
                    JSONObject calendarList = null;

                    HttpClient client = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(params[1]);
                    try
                    {
                        String serverauth = params[0];
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        nameValuePairs.add(new BasicNameValuePair("ServerAuth", serverauth));
                        Log.v("POST",nameValuePairs.toString());
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                        HttpResponse response = client.execute(httpPost,localContext);
                        int status = response.getStatusLine().getStatusCode();
                        Log.v("NETWORK",String.valueOf(status));
                        final String responseBody = EntityUtils.toString(response.getEntity());
                        calendarList = new JSONObject(responseBody);
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        Log.e("AUTH", "Error sending to server", e);
                    }
                    catch (IOException e)
                    {
                        Log.e("AUTH", "Error sending to server", e);
                    }
                    catch (JSONException e)
                    {
                        Log.e("AUTH", "Error sending to server", e);
                    }
                    finally
                    {
                        return calendarList;
                    }
                }

                @Override
                protected void onPostExecute(JSONObject calendarList)
                {
                    SelectCalendarActivity activity = (SelectCalendarActivity) getActivity();
                    if (calendarList == null)
                    {
                        load_status = 2;
                    }
                    else
                    {
                        try
                        {
                            populateCalendars(calendarList.getJSONArray("items"));
                        }
                        catch (JSONException e)
                        {
                            load_status = 2;
                        }
                    }
                    updateText();
                }
            }.execute(auth,calendar_url);
        }
    }

}
