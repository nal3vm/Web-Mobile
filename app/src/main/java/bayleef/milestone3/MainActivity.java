package bayleef.milestone3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
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

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
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

    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            if (mAccel < 5 && mAccel > 2) {
                System.out.println("SHAKE SHAKE SHAKE");
                shakeColor(0,0,255);
            } else if (mAccel < 8 && mAccel > 5) {
                shakeColor(0,255,255);
            } else if (mAccel < 11 && mAccel > 8) {
                shakeColor(0,255,0);
            } else if (mAccel < 14 && mAccel > 11) {
                shakeColor(255,255,0);
            } else if (mAccel > 14) {
                shakeColor(255,0,0);
            }

        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
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

    public void shakeColor(int red, int green, int blue) {
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