package com.lifelines.code.lifelinesmm2;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressBarAnimation extends Animation {
    private ProgressBar progressBar;
    private TextView txt;
    private float from;
    private float  to;
    private String  extra;

    public ProgressBarAnimation(ProgressBar progressBar,TextView txt,String  extra, float from, float to) {
        super();
        this.progressBar = progressBar;
        this.txt = txt;
        this.from = from;
        this.to = to;
        this.extra = extra;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        progressBar.setProgress((int) value);
        txt.setText(extra+String.valueOf((int)value)+"%");
    }

}