package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

public class MapWithPhoto extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_with_photo);
    }

    public void increaseValue(View v) {
        MyView.radius += 2;
        System.out.println(MyView.radius);
    }

    public void decreaseValue(View v) {
        MyView.radius -= 2;
        System.out.println(MyView.radius);
    }

    public void setRed(View v) {
        MyView.whatColor = 1;
        System.out.println(MyView.whatColor);
    }

    public void setBlue(View v) {
        MyView.whatColor = 2;
        System.out.println(MyView.whatColor);
    }

    public void setYellow(View v) {
        MyView.whatColor = 3;
        System.out.println(MyView.whatColor);
    }

    public void setGreen(View v) {
        MyView.whatColor = 4;
        System.out.println(MyView.whatColor);
    }

    public void setBlack(View v) {
        MyView.whatColor = 5;
        System.out.println(MyView.whatColor);
    }

    public void clearPaint(View v) {
        MyView.clearPaint();
    }

    public void savePaint(View v) {
//        MyView.savePaint();
    }

    public void loadPaint(View v) {
//        MyView.loadPaint();
    }


}
