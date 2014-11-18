package com.novaember.xedroid;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class LocationsActivity extends ActionBarActivity
{
    LocationAdapter locations;
    LocationsActivity self;

    int organisationId;
    String organisationName;

	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
        self = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locations);

        Intent intent = getIntent();
        organisationId = intent.getIntExtra("organisationId", 0);
        organisationName = intent.getStringExtra("organisationName");

        ActionBar bar = getSupportActionBar();
        bar.setTitle(organisationName);
        bar.setDisplayHomeAsUpEnabled(true);

        locations = new LocationAdapter(this);
        new FetchLocationsTask().execute("http://xedule.novaember.com/locations." + organisationId + ".json");

        ListView locationsView = (ListView) findViewById(R.id.locations);
        locationsView.setAdapter(locations);

        locationsView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView listview, View view, int pos, long id)
            {
                try
                {
                    Location loc = (Location) listview.getAdapter().getItem(pos);
                    Intent intent = new Intent(self, AttendeesActivity.class);
                    intent.putExtra("locationId", loc.id);
                    intent.putExtra("locationName", loc.name);
                    startActivity(intent);
                }
                catch(Exception e)
                {
                    Log.e("Xedroid", "Error: " + e.getMessage());
                }
            }
        });
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

    private class FetchLocationsTask extends AsyncTask<String, Void, String>
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
            try
            {
                locations.addLocationsFromJSON(result);
            }
            catch (JSONException e)
            {
                Log.e("Xedroid", "Error: " + e.getMessage());
            }
        }
    }
}

class Location implements Comparable<Location>
{
    public int id;
    public String name;

    public Location(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    @Override
    public int compareTo(Location loc)
    {
        return this.name.compareTo(loc.name);
    }
}

class LocationAdapter extends BaseAdapter
{
    private Activity activity;
    private ArrayList<Location> data;
    private static LayoutInflater inflater = null;

    public LocationAdapter(Activity a)
    {
        activity = a;
        data = new ArrayList<Location>();

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addLocation(Location loc)
    {
        data.add(loc);
    }

    public void addLocationsFromJSON(String input) throws JSONException
    {
        try
        {
            JSONArray arr = new JSONArray(input);

            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject loc = arr.getJSONObject(i);
                this.addLocation(new Location(loc.getInt("id"), loc.getString("name")));
            }

            this.sort();
            this.notifyDataSetChanged();
        }
        catch (JSONException e)
        {
            Log.e("Xedroid", "Error! " + e.getMessage());
        }
    }

    public int getCount()
    {
        return data.size();
    }

    public Location getItem(int position)
    {
        return data.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public void sort()
    {
        Collections.sort(data);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        TextView view = (TextView) convertView;
        if(convertView == null)
            view = (TextView) inflater.inflate(R.layout.location_item, null);

        Location loc = data.get(position);

        view.setText(loc.name);

        return view;
    }
}
