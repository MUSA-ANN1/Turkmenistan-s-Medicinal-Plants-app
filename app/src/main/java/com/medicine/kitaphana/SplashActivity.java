package com.medicine.kitaphana;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View meshContainer = findViewById(R.id.meshContainer);
        CardView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.appName);

        // 1. Rotate the mesh slowly for the shifting color effect
        ObjectAnimator rotateMesh = ObjectAnimator.ofFloat(meshContainer, "rotation", 0f, 180f);
        rotateMesh.setDuration(15000); // 15 seconds for a very smooth, slow shift
        rotateMesh.setInterpolator(new LinearInterpolator());
        rotateMesh.setRepeatCount(ObjectAnimator.INFINITE);
        rotateMesh.start();

        // 2. Logo Scale-up and Fade-in animation
        logo.setAlpha(0f);
        logo.setScaleX(0.2f);
        logo.setScaleY(0.2f);
        
        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1500)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();

        // 3. App Name Fade-in
        appName.setAlpha(0f);
        appName.animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(800)
            .start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 4000); // 4 seconds delay to appreciate the mesh effect
    }
}