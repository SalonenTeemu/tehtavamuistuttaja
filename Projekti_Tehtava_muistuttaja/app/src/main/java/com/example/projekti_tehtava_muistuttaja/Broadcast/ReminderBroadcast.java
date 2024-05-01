package com.example.projekti_tehtava_muistuttaja.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.example.projekti_tehtava_muistuttaja.MainActivity;

// Luokka vastaanottamaan ilmoituksia, joka perii BroadcastReceiver-luokan
public class ReminderBroadcast extends BroadcastReceiver {

    // Staattiset muuttujat ilmoituksen otsikolle, lisätiedoille ja id:lle
    public static final String REMINDER_TITLE = "REMINDER_TITLE";
    public static final String REMINDER_INFO = "REMINDER_INFO";
    public static final String REMINDER_ID = "REMINDER_ID";

    // Metodi suoritetaan kun ilmoitus vastaanotetaan
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(MainActivity.TAG, "ReminderBroadcast: onReceive()");

        // Luodaan uusi intent ja lisätään siihen ilmoituksen tiedot
        Intent serviceIntent = new Intent(context, ReminderService.class);
        serviceIntent.putExtra(REMINDER_TITLE, intent.getStringExtra(REMINDER_TITLE));
        serviceIntent.putExtra(REMINDER_INFO, intent.getStringExtra(REMINDER_INFO));
        serviceIntent.putExtra(REMINDER_ID, intent.getIntExtra(REMINDER_ID, 0));

        // Käynnistetään palvelu joko etualalla tai taustalla riippuen API-versiosta
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}