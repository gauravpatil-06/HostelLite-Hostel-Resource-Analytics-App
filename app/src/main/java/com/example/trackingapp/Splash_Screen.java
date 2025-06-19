package com.example.trackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Splash_Screen extends AppCompatActivity {

    private ImageView ivLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ivLogo = findViewById(R.id.ivSplashLogo);

        // ⭐ 1. Scale Animation (Start small, scale up)
        ScaleAnimation scaleAnim = new ScaleAnimation(
                0.8f, 1.0f, // Start X/Y scale: 80%
                0.8f, 1.0f, // End X/Y scale: 100%
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot X: Center
                Animation.RELATIVE_TO_SELF, 0.5f  // Pivot Y: Center
        );
        scaleAnim.setDuration(800); // 0.8 second duration for scale

        // ⭐ 2. Alpha Animation (Fade in from slight transparency)
        AlphaAnimation alphaAnim = new AlphaAnimation(0.2f, 1.0f);
        alphaAnim.setDuration(800); // 0.8 second duration for fade

        // ⭐ 3. Animation Set (Combine both for smooth, professional effect)
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scaleAnim);
        set.addAnimation(alphaAnim);
        set.setFillAfter(true); // Keep the final state of the animation

        ivLogo.startAnimation(set);

        // Delay then move to next screen (2 seconds total display time)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Splash_Screen.this, LoginActivity.class);
                startActivity(i);
                finish(); // closes splash screen so user can't go back
            }
        }, 2000); // 2 sec delay
    }
}