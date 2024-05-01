package com.example.projekti_tehtava_muistuttaja;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

// Luokka, joka käynnistyy sovelluksen käynnistyessä ja näyttää aloitusnäytön,
// jossa on sovelluksen logo. Perii luokan AppCompatActivity
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asetetaan sovellusmuoto (night mode) pois päältä
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Lukitaan screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // Käytetään layoutia activity_splash
        setContentView(R.layout.activity_splash);

        // Näytetään aloitusruutu sekunnin ajan, jonka jälkeen siirrytään MainActivity-aktiviteettiin
        Intent intent = new Intent(this, MainActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}