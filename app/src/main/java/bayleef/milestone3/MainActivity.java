package bayleef.milestone3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.view.View;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.BasicResponseHandler;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void submitColor(View view) {
        ColorPicker picker = (ColorPicker) findViewById(R.id.picker);
        int red = (picker.getColor() >> 16) & 255;
        int green = (picker.getColor() >> 8) & 255;
        int blue = (picker.getColor()) & 255;

//        DefaultHttpClient client = new DefaultHttpClient();
//        HttpPost httppost = new HttpPost("http://192.168.1.5:5000/rpi");
        String str = "{ \"lights\": [{" +
                "\"lightId\":1," +
                "\"red\": " + Integer.toString(red) + "," +
                "\"green\": " + Integer.toString(green) + "," +
                "\"blue\": " + Integer.toString(blue) + "," +
                "\"intensity\" : 0.5 }], \"propagate\": true}";
//        System.out.println(str);
//        try {
//            StringEntity stringent = new StringEntity(str);
//            httppost.setEntity(stringent);
//            httppost.setHeader("Content-type", "application/json");
//            client.execute(httppost);
//        } catch (UnsupportedEncodingException e) {
//            System.out.println(e);
//        } catch (java.io.IOException e) {
//            System.out.println(e);
//        }


        JSONObject params = new JSONObject();
        JSONArray lightArray = new JSONArray();
        try {
            JSONObject redObj = new JSONObject().put("lightId", new Integer(1)).put("red", new Integer(red)).put("green", new Integer(green)).put("blue", new Integer(blue)).put("intensity", new Double(0.3));
            lightArray.put(redObj);
            params.put("lights", lightArray);
            params.put("propagate", true);
        } catch(JSONException e) {
            System.out.println(e);
        }

        try {
            AsyncHttpClient client = new AsyncHttpClient();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String address = prefs.getString("URL", "");
            System.out.println(address);
            client.post(getApplicationContext() ,"http://"+prefs.getString("URL", "")+"/rpi", new StringEntity(params.toString()), "application/json", new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    // called before request is started
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    System.out.println("Success");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    System.out.println(e);
                }

                @Override
                public void onRetry(int retryNo) {
                    // called when request is retried
                }
            });
        } catch (UnsupportedEncodingException e) {
            System.out.println(3);
        }

    }
}

