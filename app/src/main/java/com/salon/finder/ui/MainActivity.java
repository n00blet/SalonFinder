package com.salon.finder.ui;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.salon.finder.R;
import com.salon.finder.adapter.CustomAdapter;
import com.salon.finder.model.SalonObjects;
import com.salon.finder.utils.AppConstants;
import com.salon.finder.utils.SalonRestClient;

import org.apache.http.Header;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private DateTime dateTime;
    private ArrayList<SalonObjects> salons;
    private ListView listView;
    private CustomAdapter adapter;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dateTime = DateTime.now();
        salons = new ArrayList<>();
        listView = (ListView) findViewById(R.id.salon_list);

        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Position", ":" + position);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    private void makeApiRequests(String ll){
        dateTime = DateTime.now();
        String date = dateTime.toLocalDate().toString("-");
        Log.d("Date","::" + date);
        getNearByFourSquareSalon("20150608",ll);
    }

    private void getNearByFourSquareSalon(String version, String location) {
        Log.d("Api call", "true");
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id", AppConstants.FOURSQUARE_CLIENT);
        paramMap.put("client_secret", AppConstants.FOURSQUARE_CLIENT_TOKEN);
        paramMap.put("query", AppConstants.SEARCH_QUERY);
        paramMap.put("v", version);
        paramMap.put("ll", location);
        RequestParams params = new RequestParams(paramMap);
        Log.d("Params", ":" + params);
        SalonRestClient.post(AppConstants.FOURSQUARE_BASE_URL, params, new JsonHttpResponseHandler() {

            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

            @Override
            public void onStart() {
                super.onStart();
                progressDialog.setTitle("Please wait,while we fetch salons nearby");
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Response", ":" + response);
                try {
                    JSONObject re = response.getJSONObject("response");
                    JSONArray ar = re.getJSONArray("venues");
                    int len = ar.length();
                    for (int i = 0; i < len; i++) {
                        SalonObjects salon = new SalonObjects();
                        JSONObject location = ar.getJSONObject(i).getJSONObject("location");
                        JSONArray address = location.getJSONArray("formattedAddress");
                        Log.d("Address", ":" + address);
                        Log.d("Location", ":" + location);
                        salon.setLocation(location.toString());
                        salon.setName(address.toString());
                        salons.add(salon);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (salons.size() > 0) {
                    adapter = new CustomAdapter(MainActivity.this, salons);
                    listView.setAdapter(adapter);
                }

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("Response failure", ":" + responseString);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    private String getLattLong() {
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        String ll = null;
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            ll = latitude + "," + longitude;
        } else {
            ll = getString(R.string.no_address_found);
        }
        Log.d("LL", ":::" + ll);
        return ll;
    }

    @Override
    public void onConnected(Bundle bundle) {
        String location_response = getLattLong();
        if (location_response.equals(getString(R.string.no_address_found)))
            Toast.makeText(getApplicationContext(), getString(R.string.no_address_found), Toast.LENGTH_SHORT).show();
        else
            makeApiRequests(location_response);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Failure", ":" + connectionResult.getErrorCode());
    }
}
