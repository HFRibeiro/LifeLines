package com.lifelines.code.lifelinesmm2;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import static android.widget.ImageView.ScaleType.MATRIX;

public class MainActivity extends AppCompatActivity  {

    final String AcceptSSID = "\"LifeLinesMM2\"";

    public static final String PREFS_NAME = "MyPrefsFile";

    String configs = "",urlImg = "http://10.42.0.1/cap/img.jpg";

    AsyncConnection socketConnection = null;
    ToggleButton tg_save;
    Button bt_requestImage;

    TimerTask updateConnection;
    Timer timer;

    MainActivity myMain = null;

    CheckBox rb_left,rb_leftRGB,rb_right,rb_rightRGB,rb_depth;

    SharedPreferences.Editor editor;

    ProgressBar pb_request,pb_check;

    TextView txt = null,txt_request = null,txt_system = null;

    ImageView im_hardware = null,im_software = null;

    boolean saving = false,connected = false,hardware_state = false,software_state = false,hardware_state_old = false,software_state_old = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mCallBack); // Start OpenCV

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        editor = settings.edit();

        myMain = this;

        txt = findViewById(R.id.txt);
        txt_request = findViewById(R.id.txt_request);
        txt_system = findViewById(R.id.txt_system);


        rb_left = findViewById(R.id.rb_left);
        rb_leftRGB = findViewById(R.id.rb_leftRGB);
        rb_right = findViewById(R.id.rb_right);
        rb_rightRGB = findViewById(R.id.rb_rightRGB);
        rb_depth = findViewById(R.id.rb_depth);
        tg_save = findViewById(R.id.tg_save);

        bt_requestImage = findViewById(R.id.bt_requestImage);
        pb_request = findViewById(R.id.pb_request);
        pb_check = findViewById(R.id.pb_check);


        im_hardware = findViewById(R.id.im_hardware);
        im_software = findViewById(R.id.im_software);

        setGray();

        /////////////////////////////// NETWORK CHECK /////////////////////////////////////////
        if(isNetworkConnected())
        {
            if(getNetworkName().equals(AcceptSSID))
            {
                configs = settings.getString("configs","00000");
                setChecks(configs);
                Log.e("configsRead:",configs);

                socketConnection = new  AsyncConnection("10.42.0.1",1234,1000,myMain);

                startTimer(5);
            }
            else
            {
                Log.e("Network: " , "is "+getNetworkName()+"\nPlease connect to "+AcceptSSID+" network");
                txt.setText("Network: " + "is "+getNetworkName()+"\nPlease connect to "+AcceptSSID+" network");
                setGray();
            }

        }
        else
        {
            Log.e("Network: " , "is not connected!\nPlease connect to "+AcceptSSID+" network");
            txt.setText("Network: " + "is not connected!\nPlease connect to "+AcceptSSID+" network");
            setGray();
        }
        /////////////////////////////////////////////////////////////////////////////////////




        tg_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(tg_save.isChecked())
                {
                    saving = true;
                    sendDataSocket("startSaving");
                }
                else
                {
                    saving = false;
                    sendDataSocket("stopSaving");
                }
            }
        });

        bt_requestImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                sendDataSocket("getImage");
                pb_request.setVisibility(View.VISIBLE);
                txt_request.setVisibility(View.VISIBLE);

                ProgressBarAnimation anim = new ProgressBarAnimation(pb_request,txt_request,"", 0, 100);
                anim.setDuration(3500);
                pb_request.startAnimation(anim);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        socketConnection.interrupted = true;
                        new DownloadImageFromInternet().execute(urlImg);
                    }
                }, 3000);
            }
        });

    }//Fim OnCreate

    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        public DownloadImageFromInternet() {
            Log.i("Download","Start");
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap binge = null;
            try {
                InputStream inputStream = new java.net.URL(imageURL).openStream();
                binge = BitmapFactory.decodeStream(inputStream);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }

            return binge;
        }

        protected void onPostExecute(Bitmap result) {
            showImage(result);
            pb_request.setVisibility(View.INVISIBLE);
            txt_request.setVisibility(View.INVISIBLE);
        }
    }

    public void showImage(Bitmap result) {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(result);
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        builder.show();
    }

    public void onCheckButtonClicked(View view)
    {
        String sendConfigs = "setVideo";

        if(rb_left.isChecked()) sendConfigs += "1";
        else sendConfigs += "0";

        if(rb_leftRGB.isChecked()) sendConfigs += "1";
        else sendConfigs += "0";

        if(rb_right.isChecked()) sendConfigs += "1";
        else sendConfigs += "0";

        if(rb_rightRGB.isChecked()) sendConfigs += "1";
        else sendConfigs += "0";

        if(rb_depth.isChecked()) sendConfigs += "1";
        else sendConfigs += "0";

        editor.putString("configs", sendConfigs);
        editor.commit();

        configs = sendConfigs;
        sendDataSocket(sendConfigs);
    }

    class UpdateConnectionTask extends TimerTask {

        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkSoftwareHardware();
                }
            });

            if(socketConnection==null)
            {
                socketConnection.execute();
                Log.i("Trying to connect","...");
            }
            else
            {
                if(!socketConnection.isConnected())
                {
                    socketConnection.disconnect();
                    socketConnection = new  AsyncConnection("10.42.0.1",1234,1000,myMain);
                    socketConnection.execute();
                    Log.i("Trying to connect","...");
                }
                else
                {

                    //Log.i("Connected","...");
                }
            }

        }
    }

    private void checkSoftwareHardware()
    {
        if(hardware_state && software_state){
            if(!saving) sendDataSocket("checkZED");
            else if(saving) tg_save.setChecked(true);
            if(hardware_state!=hardware_state_old || software_state!=software_state_old) setColor();
        }

        setImageViewDraw(im_hardware,hardware_state);

        setImageViewDraw(im_software,software_state);

        if(!hardware_state || !software_state) {
            setGray();
            sendDataSocket("checkZED");
            ProgressBarAnimation anim = new ProgressBarAnimation(pb_check,txt_system,"System Check: ", 0, 100);
            anim.setDuration(1000);
            pb_check.startAnimation(anim);
        }
    }

    public void didReceiveData(String data)
    {
        if(data.contains("ZED_OK"))
        {
            hardware_state = true;
        }
        else if(data.contains("ZED_KO"))
        {
            hardware_state = false;
        }
        else if(data.contains("SAVING_OK"))
        {
            hardware_state = true;
            saving = true;
        }
        Log.i("Received: ",data);
    }

    public void didDisconnect()
    {
        software_state = false;
        Log.i("didDisconnect: ","");
    }

    public void didConnect()
    {
        Log.i("didConnect: ","Connection ok");
        sendDataSocket(configs);
        sendDataSocket("checkSaving");
        connected = true;
        software_state = true;
    }

    private static final String TAG = "MainActivity";
    BaseLoaderCallback mCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    break;
                }
                default:
                {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    private void startTimer(int sec)
    {
        timer = new Timer();
        updateConnection = new UpdateConnectionTask();
        timer.scheduleAtFixedRate(updateConnection, 0, sec*1000);
    }

    private void sendDataSocket(String data)
    {
        if(socketConnection.isConnected()) socketConnection.write(data);
    }

    private void setChecks(String str)
    {
        str = str.replace("setVideo","");
        if(str.charAt(0)=='1') rb_left.setChecked(true);
        if(str.charAt(1)=='1') rb_leftRGB.setChecked(true);
        if(str.charAt(2)=='1') rb_right.setChecked(true);
        if(str.charAt(3)=='1') rb_rightRGB.setChecked(true);
        if(str.charAt(4)=='1') rb_depth.setChecked(true);
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
        rb_right.setEnabled(false);
        rb_depth.setEnabled(false);
        rb_leftRGB.setEnabled(false);
        rb_rightRGB.setEnabled(false);
        rb_left.setEnabled(false);
        tg_save.setEnabled(false);
        bt_requestImage.setEnabled(false);
    }

    private void setColor()
    {
        txt.setTextColor(Color.GRAY);
        rb_right.setEnabled(true);
        rb_depth.setEnabled(true);
        rb_leftRGB.setEnabled(true);
        rb_rightRGB.setEnabled(true);
        rb_left.setEnabled(true);
        tg_save.setEnabled(true);
        bt_requestImage.setEnabled(true);
    }

    private void setImageViewDraw(ImageView im,boolean state)
    {
        if(state) im.setImageDrawable(getResources().getDrawable(R.drawable.connected));
        else im.setImageDrawable(getResources().getDrawable(R.drawable.notconnected));
    }
}
