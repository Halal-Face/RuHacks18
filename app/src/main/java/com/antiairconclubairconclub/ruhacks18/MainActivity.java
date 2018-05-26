package com.antiairconclubairconclub.ruhacks18;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

//following from tutorial at https://www.androidhive.info/2012/01/android-json-parsing-tutorial/

public class MainActivity extends AppCompatActivity {
    double longitude;
    double latitude;

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get contacts JSON
    private static String url = "http://app.toronto.ca/opendata//ac_locations/locations.json?v=1.00";

    ArrayList<HashMap<String, String>> locationList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, 1);


        locationList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);

        new GetLocations().execute();
    }
    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Toast.makeText(getApplicationContext(),
                    "LON: " + longitude + " LAT: " +latitude,
                    Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };





    /**
     * Async task class to get json by making HTTP call
     */
    private class GetLocations extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {

                    // Getting JSON Array node
                    JSONArray locations = new JSONArray(jsonStr);

                    // looping through All Contacts
                    for (int i = 0; i < locations.length(); i++) {
                        JSONObject c = locations.getJSONObject(i);

                        String locationTypeCd = c.getString("locationTypeCd");
                        String locationTypeDesc = c.getString("locationTypeDesc");
                        String locationCode = c.getString("locationCode");
                        String locationDesc = c.getString("locationDesc");
                        String locationName = c.getString("locationName");
                        String address = c.getString("address");
                        String phone = c.getString("phone");
                        String notes = c.getString("notes");
                        String lat = c.getString("lat");
                        String lon = c.getString("lon");


                        // tmp hash map for single contact
                        HashMap<String, String> location = new HashMap<>();

                        // adding each child node to HashMap key => value
                        location.put("locationTypeCd",locationTypeCd);
                        location.put("locationTypeDesc",locationTypeDesc);
                        location.put("locationCode",locationCode);
                        location.put("locationDesc",locationDesc);
                        location.put("locationName", locationName);
                        location.put("address", address);
                        location.put("phone", phone);
                        location.put("notes", notes);
                        location.put("lat", lat);
                        location.put("lon", lon);

                        // adding contact to contact list
                        locationList.add(location);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, locationList, R.layout.list_item,
                    new String[]{ "locationTypeDesc","locationDesc","locationName","address","phone", "lat", "lon"},
                    new int[]{R.id.locationTypeDesc,R.id.locationDesc,R.id.locationName,R.id.address,R.id.phone, R.id.lat, R.id.lon});

            lv.setAdapter(adapter);

            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            while (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
            }

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000,
                    10000, mLocationListener);



        }

    }


}
