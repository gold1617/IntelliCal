package com.example.rishab.intellical;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AddEventsService extends IntentService
{
    public AddEventsService()
    {
        super("AddEventsService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        HttpContext localContext = SelectCalendarActivity.getLocalContext();
        String serverauth = intent.getStringExtra("ServerAuth");
        String calID = intent.getStringExtra("CalID");
        String date = intent.getStringExtra("Date");
        String tz = intent.getStringExtra("TZ");
        String days = intent.getStringExtra("Days");
        ArrayList<ArrayList<String>> events = (ArrayList<ArrayList<String>>) intent.getSerializableExtra("Events");
        JSONArray eventsJSON = new JSONArray(events);

        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(getString(R.string.add_events_url));
        try
        {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("ServerAuth", serverauth));
            nameValuePairs.add(new BasicNameValuePair("CalID", calID));
            nameValuePairs.add(new BasicNameValuePair("Date", date));
            nameValuePairs.add(new BasicNameValuePair("TZ", tz));
            nameValuePairs.add(new BasicNameValuePair("Events", eventsJSON.toString()));
            nameValuePairs.add(new BasicNameValuePair("Days", days));
            Log.d("POST",nameValuePairs.toString());
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = client.execute(httpPost,localContext);
            int status = response.getStatusLine().getStatusCode();
            Log.d("NETWORK",response.getStatusLine().getReasonPhrase());
            Log.d("NETWORK",String.valueOf(status));
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("AUTH", "Error sending to server", e);
        }
        catch (IOException e)
        {
            Log.e("AUTH", "Error sending to server", e);
        }
    }
}
