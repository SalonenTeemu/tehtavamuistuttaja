import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.projekti_tehtava_muistuttaja.R;

public class R extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, SplashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pI = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE);

        String title = intent.getStringExtra("ReminderTitle");
        String info = intent.getStringExtra("ReminderInfo");
        int id = intent.getIntExtra("alarmID", 0);
        Log.d(MainActivity.TAG, "testiä:" + id);
        Log.d(MainActivity.TAG, title + info);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channelMuistuttaja")
                .setSmallIcon(R.drawable.app_logo_notification)
                .setColor(context.getColor(R.color.holo_orange_dark))
                .setContentTitle(title)
                .setContentText(info)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pI);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, builder.build());
        Log.d(MainActivity.TAG, "Ilmoitus näytetty");
    }
}