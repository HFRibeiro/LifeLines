package com.lifelines.code.lifelinesmm2;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";

    AsyncConnection socketConnection = null;
    ToggleButton tg_save;

    TimerTask updateConnection;
    Timer timer;

    MainActivity myMain = null;

    CheckBox rb_left,rb_leftRGB,rb_right,rb_rightRGB,rb_depth;

    SharedPreferences.Editor editor;

    String configs = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mCallBack); // Start OpenCV

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        editor = settings.edit();

        myMain = this;
        socketConnection = new  AsyncConnection("10.42.0.1",1234,1000,myMain);

        rb_left = findViewById(R.id.rb_left);
        rb_leftRGB = findViewById(R.id.rb_leftRGB);
        rb_right = findViewById(R.id.rb_right);
        rb_rightRGB = findViewById(R.id.rb_rightRGB);
        rb_depth = findViewById(R.id.rb_depth);
        tg_save = findViewById(R.id.tg_save);

        configs = settings.getString("configs","00000");
        setChecks(configs);
        Log.e("configsRead:",configs);

        startTimer(5);


        tg_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(tg_save.isChecked())
                {
                    sendDataSocket("startSaving");
                }
                else
                {
                    sendDataSocket("stopSaving");
                }
            }
        });


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

    public void didReceiveData(String data)
    {
        Log.i("Received: ",data);
    }

    public void didDisconnect()
    {
        Log.i("didDisconnect: ","");
    }

    public void didConnect()
    {
        Log.i("didConnect: ","Connection ok");
        sendDataSocket(configs);
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
}
