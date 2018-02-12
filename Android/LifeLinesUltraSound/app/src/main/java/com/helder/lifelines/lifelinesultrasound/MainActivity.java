package com.helder.lifelines.lifelinesultrasound;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    final String AcepptSSID = "\"lifelines\"";

    String ipSend = "192.168.4.1";

    TextView txt = null,txt_fq = null,txt_tm = null,txt_ds = null;

    SeekBar sk_fq = null,sk_tm = null,sk_ds = null;

    Button bt_send = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = findViewById(R.id.txt);
        txt_fq = findViewById(R.id.txt_fq);
        txt_tm = findViewById(R.id.txt_tm);
        txt_ds = findViewById(R.id.txt_distance);
        sk_fq= findViewById(R.id.sk_fq);
        sk_tm = findViewById(R.id.sk_tm);
        bt_send = findViewById(R.id.bt_send);
        sk_ds = findViewById(R.id.sk_ds);

        /////////////////////////////// NETWORK CHECK /////////////////////////////////////////
        if(isNetworkConnected())
        {
            if(getNetworkName().equals(AcepptSSID))
            {
                get_configs();
            }
            else
            {
                Log.e("Network: " , "is "+getNetworkName()+"\nPlease connect to "+AcepptSSID+" network");
                txt.setText("Network: " + "is "+getNetworkName()+"\nPlease connect to "+AcepptSSID+" network");
                setGray();
            }

        }
        else
        {
            Log.e("Network: " , "is not connected!\nPlease connect to "+AcepptSSID+" network");
            txt.setText("Network: " + "is not connected!\nPlease connect to "+AcepptSSID+" network");
            setGray();
        }
        /////////////////////////////////////////////////////////////////////////////////////



        sk_fq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                txt_fq.setText("Frequency: "+String.valueOf(progress)+" Hz");

            }
        });

        sk_ds.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                txt_ds.setText("Distance: "+String.valueOf(progress)+" m");

            }
        });


        sk_tm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                txt_tm.setText("Time Interval: "+String.valueOf(progress)+" seconds");

            }
        });


        bt_send.setOnClickListener( new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                sendFreq();

                final Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 500ms
                        sendInterval();
                    }
                }, 200);

            }
        });

        //End OnCreate
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private String getNetworkName()
    {
        final WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        return info.getSSID();
    }

    private void setGray()
    {
        txt.setTextColor(Color.RED);
        sk_fq.setEnabled(false);
        sk_tm.setEnabled(false);
        sk_ds.setEnabled(false);
        bt_send.setEnabled(false);
        bt_send.setBackgroundColor(Color.GRAY);
    }

    private void get_configs()
    {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://"+ipSend+"?data";
        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            Log.i("fq: ",response.getString("fq"));
                            Log.i("tm: ",response.getString("tm"));

                            txt_fq.setText("Frequency: "+response.getString("fq"));
                            txt_tm.setText("Time Interval: "+response.getString("tm"));

                            sk_fq.setProgress(Integer.valueOf(response.getString("fq")));
                            sk_tm.setProgress(Integer.valueOf(response.getString("tm"))/1000);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("Response: " , e.toString());
                            txt.setText("Response: " + e.toString());
                            setGray();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        // TODO Auto-generated method stub
                        Log.e("Response: " , e.toString());
                        txt.setText("Response: " + e.toString());
                        setGray();
                    }
                });
        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);

    }

    private void sendFreq()
    {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://"+ipSend+"?fq="+String.valueOf(sk_fq.getProgress());

        Log.i("url: ",url);

        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            Log.i("freq: ",response.getString("fq"));
                            Log.i("time: ",response.getString("tm"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("Response: " , e.toString());
                            txt.setText("Response: " + e.toString());
                            setGray();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        // TODO Auto-generated method stub
                        Log.e("Response: " , e.toString());
                        txt.setText("Response: " + e.toString());
                        setGray();
                    }
                });
        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);

    }

    private void sendInterval()
    {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://"+ipSend+"?tm="+String.valueOf(sk_tm.getProgress()*1000);

        Log.i("url: ",url);

        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            Log.i("fq: ",response.getString("fq"));
                            Log.i("tm: ",response.getString("tm"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("Response: " , e.toString());
                            txt.setText("Response: " + e.toString());
                            setGray();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        // TODO Auto-generated method stub
                        Log.e("Response: " , e.toString());
                        txt.setText("Response: " + e.toString());
                        setGray();
                    }
                });
        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);

    }


}
