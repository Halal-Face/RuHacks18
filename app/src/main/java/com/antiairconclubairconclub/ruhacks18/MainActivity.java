package com.antiairconclubairconclub.ruhacks18;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

//following from tutorial at https://www.androidhive.info/2012/01/android-json-parsing-tutorial/

public class MainActivity extends AppCompatActivity{
    double longitude;
    double latitude;


    TextView lat;
    TextView lon;
    TextView shortest_dist;
    TextView view_all;
    TextView your_loc;

    Boolean toggle = false;

    int shortest_dist_height;
    int your_loc_height;
    int lat_lon_height;

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;


    LocationManager lm;
    Location location;
    GPSTracker gps;

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
        lat = findViewById(R.id.lat);
        lon = findViewById(R.id.lon);
        shortest_dist = findViewById(R.id.shortest_dist);
        view_all = findViewById(R.id.view_all);
        your_loc = findViewById(R.id.your_loc);

        shortest_dist_height = shortest_dist.getHeight();
        your_loc_height = your_loc.getHeight();
        lat_lon_height = lat.getHeight();

        view_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(toggle){
//                    shortest_dist.setHeight(shortest_dist_height);
//                    your_loc.setHeight(your_loc_height);
//                    lat.setHeight(lat_lon_height);
//                    lon.setHeight(lat_lon_height);
//                }
//                else{
//                    shortest_dist.setHeight(0);
//                    your_loc.setHeight(0);
//                    lat.setHeight(0);
//                    lon.setHeight(0);
//
//                }
//                toggle = !toggle;
            }
        });

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Boolean permission_check = (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        while (!permission_check) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
            permission_check = (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        }
        // create class object
        gps = new GPSTracker(MainActivity.this);

        //start the other processes
        new GetLocations().execute();
    }


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

                        //store the info from teh Json File
                        String locationTypeCd = c.getString("locationTypeCd");
                        String locationTypeDesc = c.getString("locationTypeDesc");
                        String locationCode = c.getString("locationCode");
                        String locationDesc = c.getString("locationDesc");
                        String locationName = c.getString("locationName");
                        String address = c.getString("address");
                        String phone = c.getString("phone");
                        String notes = c.getString("notes");
                        double lat = c.getDouble("lat");
                        double lon = c.getDouble("lon");


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
                        location.put("lat", String.valueOf(lat));
                        location.put("lon", String.valueOf(lon));

                        // adding location to locationList
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

            // check if GPS enabled
            if(gps.canGetLocation()) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                lat.setText("Lat: " + latitude);
                lon.setText("Lon: " + longitude);

                /**
                    Code for getting address from https://stackoverflow.com/questions/6172451/given-a-latitude-and-longitude-get-the-location-name?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                 **/
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                String address="";
                try {

                    List<Address> addresses = geocoder.getFromLocation(gps.getLatitude(), gps.getLongitude(), 1);
                    Address obj = addresses.get(0);
                    String  add = obj.getAddressLine(0);

                    Log.e("Location", "Address" + add);
                    address=add;

                } catch (IOException e) {

                    e.printStackTrace();

                }
                your_loc.setText("Your Location: "+address);
            }

            //Used to get an inital distance and index
            double shortestDistance = -1f;
            int index_of_shortest_distance = -1;

            for(int i=0; i <locationList.size();i++){
                //get the hashmap objects and then get value from key lat and lon
                String lat_string = locationList.get(i).get("lat");
                String lon_string = locationList.get(i).get("lon");
                //convert to double
                double lat = Double.parseDouble(lat_string);
                double lon = Double.parseDouble(lon_string);
                //find shortest distance
                if(shortestDistance == -1 || index_of_shortest_distance==-1){
                    shortestDistance = distance(lat, lon, latitude, longitude, "K");
                    index_of_shortest_distance = i;
                }
                else if(shortestDistance> distance(lat, lon,latitude, longitude, "K")){
                    shortestDistance = distance(lat, lon, latitude, longitude, "K");
                    index_of_shortest_distance = i;
                }
            }

            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            String address_cool_location="";
            try {
                String lat_string = locationList.get(index_of_shortest_distance).get("lat");
                String lon_string = locationList.get(index_of_shortest_distance).get("lon");
                //convert to double
                double lat = Double.parseDouble(lat_string);
                double lon = Double.parseDouble(lon_string);
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                Address obj = addresses.get(0);
                String  add = obj.getAddressLine(0);

                Log.e("Location", "Address" + add);
                address_cool_location=add;

            } catch (IOException e) {

                e.printStackTrace();

            }

            //update the textview
            shortest_dist.setText("Location: " + locationList.get(index_of_shortest_distance).get("locationName")
                    +"\nLat: "+locationList.get(index_of_shortest_distance).get("lat")
                    +"  Lon: "+locationList.get(index_of_shortest_distance).get("lon")
                    +"\n" + address_cool_location);


        }

    }

    /*
        Functions below from https://www.geodatasource.com/developers/java
        for
     */
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts decimal degrees to radians						 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts radians to decimal degrees						 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


}
