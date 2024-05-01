package com.example.projekti_tehtava_muistuttaja.Broadcast;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.projekti_tehtava_muistuttaja.Handler.AppHandler;
import com.example.projekti_tehtava_muistuttaja.MainActivity;
import com.example.projekti_tehtava_muistuttaja.R;
import com.example.projekti_tehtava_muistuttaja.SplashActivity;

// Luokka muistutuspalvelulle, perii Service-luokan
public class ReminderService extends Service {

    // Muuttujat ilmoituksen id:lle, audiomanagerille, värinälle,
    // mediasoittimelle ja puhelimen äänitilalle
    private int reminderId;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private AudioManager audioManager;
    private int ringerMode;

    // Metodi muistutuspalvelun käynnistämiseen
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Haetaan tarvittavat tiedot, jotka lähetettiin ReminderBroadcast-luokassa
        String title = intent.getStringExtra(ReminderBroadcast.REMINDER_TITLE);
        String info = intent.getStringExtra(ReminderBroadcast.REMINDER_INFO);
        reminderId = intent.getIntExtra(ReminderBroadcast.REMINDER_ID, 0);

        // Luodaan intent, jonka avulla ilmoitusta painettaessa siirrytään sovellukseen
        Intent i = new Intent(this, SplashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pI = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE);

        // Luodaan ilmoitus
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AppHandler.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo_notification)
                .setColor(getColor(R.color.holo_orange_dark))
                .setLights(Color.YELLOW, 500, 500)
                .setContentTitle(title)
                .setContentText(info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setOngoing(false)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pI);

        Notification notification = builder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(reminderId, notification);

        // Haetaan audiomanageri ja puhelimen äänitila
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ringerMode = audioManager.getRingerMode();

        // Tarkistetaan puhelimen äänitila ja soitetaan ääni ja värinä tämän perusteella
        // Jos puhelin on hiljaisella, ei suoriteta kumpaakaan
        if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            mediaPlayer = MediaPlayer.create(this, R.raw.notification);
            mediaPlayer.setLooping(false);
            mediaPlayer.start();

            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(1000);
        } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(1000);
        }
        Log.d(MainActivity.TAG, "Muistutus " + title + " näytetty");
        return START_STICKY;
    }

    // Metodi muistutuspalvelun tuhoamiseen, jossa lopetetaan aloitetut soitot ja värinät
    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
