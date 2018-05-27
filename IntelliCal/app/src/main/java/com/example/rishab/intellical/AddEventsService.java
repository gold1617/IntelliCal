package com.example.rishab.intellical;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.example.rishab.intellical.LoginActivity.CHANNEL_ID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class AddEventsService extends IntentService
{
    private final static int foregroundId = 1203;

    public AddEventsService()
    {
        super("AddEventsService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //Get intent extras needed for adding events
        HttpContext localContext = SelectCalendarActivity.getLocalContext();
        String serverauth = intent.getStringExtra("ServerAuth");
        String calID = intent.getStringExtra("CalID");
        String date = intent.getStringExtra("Date");
        String tz = intent.getStringExtra("TZ");
        String days = intent.getStringExtra("Days");
        ArrayList<ArrayList<String>> events = (ArrayList<ArrayList<String>>) intent.getSerializableExtra("Events");
        JSONArray eventsJSON = new JSONArray(events);

        //Builder for ongoing notification(required for long running bg process)
        NotificationCompat.Builder ongoingNotificationBuilder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.googleg_color)
                .setContentTitle(getString(R.string.add_events_service_running))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);


        startForeground(foregroundId,ongoingNotificationBuilder.build()); //Needed to continue running service if app is closed

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
            Log.d("NETWORK",String.valueOf(status));

            final String responseBody = EntityUtils.toString((response.getEntity()));
            JSONObject responseJSON = new JSONObject(responseBody);
            int failed = responseJSON.getInt("failed_to_add");

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setSmallIcon(R.drawable.googleg_color)
                    .setContentTitle(getString(R.string.add_events_service_complete))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            if(failed == 0)
                notificationBuilder.setContentText(getString(R.string.events_added));
            else
                notificationBuilder.setContentText(getString(R.string.events_added_failed,failed));

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            notificationManager.notify(0,notificationBuilder.build());

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
            Log.e("AUTH", "Error converting response to JSON", e);
        }
        finally
        {
            stopSelf();
        }
    }
}
