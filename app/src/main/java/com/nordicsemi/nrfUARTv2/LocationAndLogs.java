package com.nordicsemi.nrfUARTv2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class LocationAndLogs extends Activity {
    private Button showLogsButton;
    private Button showLocationButton;
    private ArrayAdapter<String> locAdapter;
    private ListView listView;
    private ArrayList<String> values;
    public  final String DEBUGGING = "Debuging";
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_location_and_logs);
        showLocationButton = (Button)findViewById(R.id.locationButton);
        showLogsButton = (Button)findViewById(R.id.logsButton);
        //locAdapter = new ArrayAdapter<String>(this,R.layout.message_detail);

        showLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"Show location click",Toast.LENGTH_SHORT).show();
                //Intent maps = new Intent(Intent.ACTION_VIEW), chooser = null;
                ShowLocation();

            }
        });
        showLogsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Show logs clicked",Toast.LENGTH_SHORT).show();
                showLogs();
            }
        });


    }

    private void showLogs() {
        try {
            // Get all latitudes and longitudes from the server
            URL fmdLogs = new URL("https://data.sparkfun.com/output/5JrdjYwNOvfGqbQEJqLE.json");
            FetchAll locLog = new FetchAll();
            listView = (ListView)findViewById(R.id.locationLV);
            locLog.execute(fmdLogs);
            JSONArray locArray = locLog.get();
            values = new ArrayList<String>();

            // Display latitudes and longitudes in the log
            final String JSONLOG = "JSONLOG";
            String str = "";

            if (locArray == null)
            {
                Toast.makeText(getApplicationContext(),"No data in current logs",Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < locArray.length(); i++)
            {
                JSONObject point = (JSONObject) locArray.get(i);
                //Log.i(JSONLOG, "Latitude: " + point.get("latitude"));
                //Log.i(JSONLOG, "Longitude: " + point.get("longitude"));
                //str = point.get("latitude");
                //values[i]=str;
                values.add(point.get("latitude") + "," + point.get("longitude"));
            }
            locAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,values);
            listView.setAdapter(locAdapter);

            // Allow user to go to location if clicked on the list
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Launch Google Maps
                    Intent maps = new Intent(Intent.ACTION_VIEW), chooser;
                    String label = "Past FMD Location";
                    String uriString = "http://maps.google.com/maps?q=" + parent.getItemAtPosition(position) + "(" + label + ")&z=15";
                    maps.setData(Uri.parse(uriString));
                    chooser = Intent.createChooser(maps, "Launch Maps");
                    startActivity(chooser);
                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void ShowLocation(){
        try {

            // Get all latitudes and longitudes from the server
            URL fmdLogs = new URL("https://data.sparkfun.com/output/5JrdjYwNOvfGqbQEJqLE.json");
            FetchAll locLog = new FetchAll();
            listView = (ListView)findViewById(R.id.locationLV);
            locLog.execute(fmdLogs);
            JSONArray locArray = locLog.get();
            values = new ArrayList<String>();

            // Display latitudes and longitudes in the log
            final String JSONLOG = "JSONLOG";
            String str = "";

            if (locArray == null)
            {
                Toast.makeText(getApplicationContext(),"No data in current logs",Toast.LENGTH_SHORT).show();
                return;
            }

            // Launch Google Maps
            Intent maps = new Intent(Intent.ACTION_VIEW), chooser;
            String label = "Most Recent FMD Location";
            JSONObject jObj = (JSONObject) locArray.get(0);
            String uriString = "http://maps.google.com/maps?q=" + jObj.get("latitude") +","+ jObj.get("longitude") + "("+ label +")&z=15";
            maps.setData(Uri.parse(uriString));
            chooser = Intent.createChooser(maps, "Launch Maps");
            startActivity(chooser);

        } catch (MalformedURLException me) {
            me.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (ExecutionException ee) {
            ee.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class FetchAll extends AsyncTask<URL, Void, JSONArray>
    {

        @Override
        protected JSONArray doInBackground(URL... params) {

            HttpURLConnection urlConnection;
            try {
                urlConnection = (HttpURLConnection) params[0].openConnection();

                urlConnection.setRequestMethod("GET");
                //Log.i(DEBUGGING, "setRequestMethod for all");

                int code = urlConnection.getResponseCode();
                //Log.i(DEBUGGING, "getResponsecode for all: " + code);

                String message = urlConnection.getResponseMessage();
                //Log.i(DEBUGGING, "getResponseMessage for all: " + message);

                //urlConnection.setRequestProperty("Content-length", "0");
                //urlConnection.setUseCaches(false);
                //urlConnection.setAllowUserInteraction(false);
                urlConnection.connect();

                InputStream iStream = urlConnection.getInputStream();
                String dataString = convertStreamToString(iStream);
                //Log.i(DEBUGGING, "log : " + dataString);

                iStream.close();

                return new JSONArray(dataString);

            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (JSONException je) {
                je.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    private String convertStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Stream Exception", Toast.LENGTH_SHORT).show();
        }
        return total.toString();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location_and_logs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
