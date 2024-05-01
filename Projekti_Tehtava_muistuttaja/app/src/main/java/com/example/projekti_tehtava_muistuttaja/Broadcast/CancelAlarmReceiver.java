package com.example.projekti_tehtava_muistuttaja.Broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// Luokka perumaan hälytyksiä, joka perii BroadcastReceiver-luokan
public class CancelAlarmReceiver extends BroadcastReceiver {

    // Metodi suoritetaan, kun käyttäjä painaa hälytyksen ilmoitukssa "Peruuta"-nappia
    @Override
    public void onReceive(Context context, Intent intent) {
        // Haetaan hälytyksen id
        int alarmId = intent.getIntExtra(AlarmBroadcast.ALARM_ID, 0);

        // Haetaan hälytyslähetys ja perutaan se
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);

        // Pysäytetään hälytyspalvelu
        Intent serviceIntent = new Intent(context, AlarmService.class);
        context.stopService(serviceIntent);
    }
}