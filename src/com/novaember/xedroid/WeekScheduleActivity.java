package com.novaember.xedroid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WeekScheduleActivity extends ActionBarActivity
{
    // WeekScheduleAdapter weekSchedule;
    private WeekScheduleActivity self;

    private int attendeeId;
    private String attendeeName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        self = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekschedule);

        Intent intent = getIntent();
        attendeeId = intent.getIntExtra("attendeeId", 0);
        attendeeName = intent.getStringExtra("attendeeName");

        ActionBar bar = getSupportActionBar();
        bar.setTitle(attendeeName);
        bar.setDisplayHomeAsUpEnabled(true);

        DateFormat yearFormat = new SimpleDateFormat("yyyy");
        DateFormat weekFormat = new SimpleDateFormat("w");

        Date date = new Date();

        new FetchWeekScheduleTask().execute("http://xedule.novaember.com/weekSchedule." + attendeeId + ".json?year=" + yearFormat.format(date) + "&week=" + weekFormat.format(date));

//        WeekScheduleView weekScheduleView = (WeekScheduleView) findViewById(R.id.weekSchedule);
//        weekScheduleView.setAdapter(weekSchedule);
//
//        weekScheduleView.setOnItemClickListener(new OnItemClickListener()
//        {
//            public void onItemClick(AdapterView listview, View view, int pos, long id)
//            {
//                try
//                {
//                    WeekSchedule org = (WeekSchedule) listview.getAdapter().getItem(pos);
//                    Intent intent = new Intent(self, LocationsActivity.class);
//                    intent.putExtra("weekScheduleId", org.id);
//                    intent.putExtra("weekScheduleName", org.name);
//                    startActivity(intent);
//                }
//                catch(Exception e)
//                {
//                    Log.e("Xedroid", "Error: " + e.getMessage());
//                }
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will automatically
        // handle clicks on the Home/Up button, so long as you specify a parent
        // activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class FetchWeekScheduleTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls)
        {
            // params comes from the execute() call: params[0] is the url.
            try
            {
                return Fetcher.downloadUrl(urls[0]);
            }
            catch (Exception e)
            {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            WeekScheduleView weekScheduleView = (WeekScheduleView) findViewById(R.id.weekSchedule);

            try
            {
                JSONArray arr = new JSONArray(result);

                WeekScheduleView.Event event;

                for (int i = 0; i < arr.length(); i++)
                {
                    JSONObject day = arr.getJSONObject(i);
                    JSONArray events = day.getJSONArray("events");

                    int weekday = "Mon Tue Wed Thu Fri Sat Sun".indexOf(day.getString("date").substring(0, 3)) / 4 + 1;

                    for (int j = 0; j < events.length(); j++)
                    {
                        JSONObject jsonEvent = events.getJSONObject(j);

                        event = new WeekScheduleView.Event();
                        event.day = weekday;
                        event.startHour = parseHour(jsonEvent.getString("start"));
                        event.endHour = parseHour(jsonEvent.getString("end"));
                        event.description = jsonEvent.getString("description");

                        try
                        {
                            event.color = hashColor(jsonEvent.getJSONArray("facilities").getString(0));
                        }
                        catch (JSONException e)
                        {
                            event.color = 0xff888888;
                        }

                        weekScheduleView.addEvent(event);
                    }
                }

                weekScheduleView.invalidate();
            }
            catch (JSONException e)
            {
                Log.e("Xedroid", "Error! " + e.getMessage());
            }
        }

        private float parseHour(String in)
        {
            String[] split = in.split(":");

            return (float) Integer.parseInt(split[0]) + (float) Integer.parseInt(split[1]) / 60.f;
        }

        private int hashColor(String in)
        {
            int sum = 0;

            for (int i = 0; i < in.length(); i++)
            {
                sum += in.charAt(i) * (i + 1);
            }

            int hue = (int) (sum * Math.PI * 1000) % 360;

            return Color.HSVToColor(new float[]{ hue, 0.95f, 0.95f });
        }
    }
}