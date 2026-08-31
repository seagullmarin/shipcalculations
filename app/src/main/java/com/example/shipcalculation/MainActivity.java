package com.example.shipcalculation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.shipcalculation.celestial.CelestialCalculationActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    CardView cardDraft, cardCargo, cardInterpolation, cardFreshWater,  cardBallast, cardGps, cardCelestial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View rootView = findViewById(android.R.id.content);
        rootView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        FloatingActionButton fabSettings = findViewById(R.id.fabSettings);
        cardDraft = findViewById(R.id.cardDraft);
        cardCargo = findViewById(R.id.cardCargo);
        cardInterpolation = findViewById(R.id.cardInterpolation);
        cardFreshWater = findViewById(R.id.cardFreshWater);
        cardBallast = findViewById(R.id.cardBallast);
        cardGps = findViewById(R.id.cardGps);
        cardCelestial = findViewById(R.id.cardCelestial);
        // cardDischarging = findViewById(R.id.cardDischarging); (ileride ekleyeceğiz)
        fabSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        cardDraft.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DraftActivity.class);
            startActivity(intent);
        });
        cardCargo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CargoCalculation.class);
            startActivity(intent);
        });
        cardInterpolation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Interpolation.class);
            startActivity(intent);
        });
        cardFreshWater.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FreshWaterTankActivity.class);
            startActivity(intent);
        });
        cardBallast.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BallastTankActivity.class);
            startActivity(intent);
        });
        cardGps.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
            startActivity(intent);
        });
        cardCelestial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CelestialCalculationActivity.class);
            startActivity(intent);
        });
        // Diğer kartlara da tıklama işlevi ekleyebilirsin
    }
}
