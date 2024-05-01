package com.example.projekti_tehtava_muistuttaja.Broadcast;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.projekti_tehtava_muistuttaja.Handler.AppHandler;
import com.example.projekti_tehtava_muistuttaja.MainActivity;
import com.example.projekti_tehtava_muistuttaja.R;
import com.example.projekti_tehtava_muistuttaja.SplashActivity;

import java.io.IOException;
import java.util.Random;

// Luokka hälytyspalvelulle, perii Service-luokan
public class AlarmService extends Service {

    // Muuttujat hälytyksen id:lle, audiomanagerille, värinälle,
    // mediasoittimelle ja alkuperäiselle äänenvoimakkuudelle
    private int alarmId;
    private AudioManager audioManager;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private int originalVolume;

    // Metodi hälytyspalvelun käynnistämiseen
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Haetaan tarvittavat tiedot, jotka lähetettiin AlarmBroadcast-luokassa
        String title = intent.getStringExtra(AlarmBroadcast.ALARM_TITLE);
        String info = intent.getStringExtra(AlarmBroadcast.ALARM_INFO);
        alarmId = intent.getIntExtra(AlarmBroadcast.ALARM_ID, 0);

        // Luodaan intent, jonka avulla ilmoitusta painettaessa siirrytään sovellukseen
        Intent i = new Intent(this, SplashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pI = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE);

        // Luodaan ilmoitus
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AppHandler.ALARM_CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo_notification)
                .setColor(getColor(R.color.holo_orange_dark))
                .setLights(Color.YELLOW, 500, 500)
                .setContentTitle(title)
                .setContentText(info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setAutoCancel(false)
                .addAction(R.drawable.app_logo_notification, "Peruuta", createPendingIntent())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pI);;

        Notification notification = builder.build();
        // Näytetään ilmoitus etualalla
        startForeground(alarmId, notification);

        // Asetetaan äänenvoimakkuus maksimiin, koska kyseessä tärkeä hälytys ja aloitetaan soitto
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Käynnistetään värinä
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{1000, 1000, 1000, 1000, 1000}, 0);

        Log.d(MainActivity.TAG, "Hälytys " + title + " näytetty");
        return START_STICKY;
    }

    // Metodi hälytyspalvelun tuhoamiseen, jossa lopetetaan kaikki aloitetut soitot ja värinät, sekä
    // lopetetaan ilmoitus ja laitetaan puhelimen hälytyksien äänenvoimakkuus alkuperäiselle tasolle
    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        vibrator.cancel();
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0);
        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Luo PendingIntentin, joka käynnistää CancelAlarmReceiver-luokan lähetyksen kun painiketta "Peruuta" painetaan ilmoituksessa
    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent createPendingIntent() {
        Log.d(MainActivity.TAG, "createPendingIntent()");
        Intent intent = new Intent(this, CancelAlarmReceiver.class);
        intent.putExtra(AlarmBroadcast.ALARM_ID, alarmId);
        int requestCode = new Random().nextInt(Integer.MAX_VALUE);
        intent.setAction(Long.toString(System.currentTimeMillis()));
        return PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
