package com.example.projekti_tehtava_muistuttaja.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.content.ContextCompat;

import com.example.projekti_tehtava_muistuttaja.MainActivity;

// Luokka vastaanottamaan hälytyksiä, joka perii BroadcastReceiver-luokan
public class AlarmBroadcast extends BroadcastReceiver {

    // Staattiset muuttujat hälytyksen otsikolle, lisätiedoille ja id:lle
    public static final String ALARM_TITLE = "ALARM_TITLE";
    public static final String ALARM_INFO = "ALARM_INFO";
    public static final String ALARM_ID = "ALARM_ID";

    // Metodi suoritetaan kun hälytys vastaanotetaan
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(MainActivity.TAG, "AlarmBroadcast: onReceive()");

        // Luodaan uusi intent ja lisätään siihen hälytyksen tiedot
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra(ALARM_TITLE, intent.getStringExtra(ALARM_TITLE));
        serviceIntent.putExtra(ALARM_INFO, intent.getStringExtra(ALARM_INFO));
        serviceIntent.putExtra(ALARM_ID, intent.getIntExtra(ALARM_ID, 0));

        // Käynnistetään palvelu joko etualalla tai taustalla riippuen API-versiosta
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
