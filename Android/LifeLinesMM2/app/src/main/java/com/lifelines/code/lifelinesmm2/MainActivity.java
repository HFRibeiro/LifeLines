package com.lifelines.code.lifelinesmm2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import static android.widget.ImageView.ScaleType.MATRIX;

public class MainActivity extends AppCompatActivity  {

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

    ProgressBar pb_request;

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

        bt_requestImage = findViewById(R.id.bt_requestImage);
        pb_request = findViewById(R.id.pb_request);


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

        bt_requestImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                sendDataSocket("getImage");
                pb_request.setVisibility(View.VISIBLE);

                ProgressBarAnimation anim = new ProgressBarAnimation(pb_request, 0, 100);
                anim.setDuration(3500);
                pb_request.startAnimation(anim);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        socketConnection.disconnect();
                        new DownloadImageFromInternet().execute(urlImg);
                    }
                }, 3000);
            }
        });

    }


    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        public DownloadImageFromInternet() {
            Log.i("Download","Start");
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap binge = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                binge = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }

            return binge;
        }

        protected void onPostExecute(Bitmap result) {
            showImage(result);
            pb_request.setVisibility(View.INVISIBLE);
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
        imageView.setScaleType(MATRIX);
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
