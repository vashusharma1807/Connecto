package com.example.connecto;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int SPLASH_TIME_OUT = 2000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
                startActivity(startIntent);
                finish();

            }
        }, SPLASH_TIME_OUT);

        ImageView imageView = findViewById(R.id.imageview);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom);
        imageView.startAnimation(animation);

    }
}