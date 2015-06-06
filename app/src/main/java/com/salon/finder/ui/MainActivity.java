package com.salon.finder.ui;

import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.salon.finder.R;
import com.salon.finder.utils.AppConstants;
import com.salon.finder.utils.SalonRestClient;

import org.apache.http.Header;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends ActionBarActivity {

    private DateTime dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dateTime = DateTime.now();
        String data[] = dateTime.toLocalDate().toString().split("-");
        Log.d("Data", ":" + data);
        getNearByFourSquareSalon("20150607","12.96,77.56");
    }


    private void getNearByFourSquareSalon(String version,String location){
        HashMap<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id",AppConstants.FOURSQUARE_CLIENT);
        paramMap.put("client_secret",AppConstants.FOURSQUARE_CLIENT_TOKEN);
        paramMap.put("query",AppConstants.SEARCH_QUERY);
        paramMap.put("v",version);
        paramMap.put("ll", location);
        RequestParams params = new RequestParams(paramMap);
        Log.d("Params",":" + params);
        SalonRestClient.post(AppConstants.FOURSQUARE_BASE_URL,params,new JsonHttpResponseHandler(){

            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            @Override
            public void onStart() {
                super.onStart();
                progressDialog.setTitle("Please wait,while we fetch salons nearby");
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Respinse",":" + response);
                try {
                     JSONObject re = response.getJSONObject("response");
                     JSONArray ar = re.getJSONArray("venues");
                     int len = ar.length();
                    for(int i=0;i<len;i++){
                        JSONObject location = ar.getJSONObject(i).getJSONObject("location");
                        JSONArray address = location.getJSONArray("formattedAddress");
                        Log.d("Location",":" + location);
                        Log.d("Address",":" + address);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("Response failure", ":" + responseString);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if(progressDialog!=null && progressDialog.isShowing())
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
}