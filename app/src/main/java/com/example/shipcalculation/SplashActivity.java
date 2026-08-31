package com.example.shipcalculation;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;



public class SplashActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageView logoImage;
    private TextView textAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoImage = findViewById(R.id.logoImage);
        textAppName = findViewById(R.id.textAppName);

        mediaPlayer = MediaPlayer.create(this, R.raw.splash_sound);
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.start();

        startEnterAnimation();
    }

    private void startEnterAnimation() {
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                startExitAnimation(); // Giriş biter bitmez çıkış
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        logoImage.startAnimation(slideIn);
        textAppName.startAnimation(slideIn);
    }

    private void startExitAnimation() {
        Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Görselleri tamamen görünmez yap
                logoImage.setVisibility(View.GONE);
                textAppName.setVisibility(View.GONE);

                // MainActivity'ye geç
                goToMain();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        logoImage.startAnimation(slideOut);
        textAppName.startAnimation(slideOut);
    }


    private void goToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
